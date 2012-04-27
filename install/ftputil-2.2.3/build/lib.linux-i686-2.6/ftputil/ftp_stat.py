# Copyright (C) 2002-2006, Stefan Schwarzer
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
# - Redistributions of source code must retain the above copyright
#   notice, this list of conditions and the following disclaimer.
#
# - Redistributions in binary form must reproduce the above copyright
#   notice, this list of conditions and the following disclaimer in the
#   documentation and/or other materials provided with the distribution.
#
# - Neither the name of the above author nor the names of the
#   contributors to the software may be used to endorse or promote
#   products derived from this software without specific prior written
#   permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

"""
ftp_stat.py - stat result, parsers, and FTP stat'ing for `ftputil`
"""

# $Id: ftp_stat.py 671 2006-11-24 23:35:32Z schwa $

import re
import stat
import sys
import time

import ftp_error
import ftp_stat_cache


class StatResult(tuple):
    """
    Support class resembling a tuple like that returned from
    `os.(l)stat`.
    """
    _index_mapping = {
      'st_mode':  0, 'st_ino':   1, 'st_dev':    2, 'st_nlink':    3,
      'st_uid':   4, 'st_gid':   5, 'st_size':   6, 'st_atime':    7,
      'st_mtime': 8, 'st_ctime': 9, '_st_name': 10, '_st_target': 11}

    def __getattr__(self, attr_name):
        if self._index_mapping.has_key(attr_name):
            return self[self._index_mapping[attr_name]]
        else:
            raise AttributeError("'StatResult' object has no attribute '%s'" %
                                 attr_name)

#
# FTP directory parsers
#
class Parser(object):
    # map month abbreviations to month numbers
    _month_numbers = {
      'jan':  1, 'feb':  2, 'mar':  3, 'apr':  4,
      'may':  5, 'jun':  6, 'jul':  7, 'aug':  8,
      'sep':  9, 'oct': 10, 'nov': 11, 'dec': 12}

    _total_regex = re.compile(r"^total\s+\d+")

    def ignores_line(self, line):
        """
        Return a true value if the line should be ignored, i. e. is
        assumed to _not_ contain actual directory/file/link data.
        A typical example are summary lines like "total 23" which
        are emitted by some FTP servers.

        If the line should be used to extract stat data from it,
        return a false value.
        """
        # either a `_SRE_Match` instance or `None`
        match = self._total_regex.search(line)
        return bool(match)

    def parse_line(self, line, time_shift=0.0):
        """
        Return a `StatResult` object as derived from the string
        `line`. The parser code to use depends on the directory format
        the FTP server delivers (also see examples at end of file).

        If the given text line can't be parsed, raise a `ParserError`.

        For the definition of `time_shift` see the docstring of
        `FTPHost.set_time_shift` in `ftputil.py`. Not all parsers
        use the `time_shift` parameter.
        """
        raise NotImplementedError("must be defined by subclass")

    #
    # helper methods for parts of a directory listing line
    #
    def parse_unix_mode(self, mode_string):
        """
        Return an integer from the `mode_string`, compatible with
        the `st_mode` value in stat results. Such a mode string
        may look like "drwxr-xr-x".

        If the mode string can't be parsed, raise an
        `ftp_error.ParserError`.
        """
        st_mode = 0
        if len(mode_string) != 10:
            raise ftp_error.ParserError("invalid mode string '%s'" %
                                        mode_string)
        for bit in mode_string[1:10]:
            bit = (bit != '-')
            st_mode = (st_mode << 1) + bit
        if mode_string[3] == 's':
            st_mode = st_mode | stat.S_ISUID
        if mode_string[6] == 's':
            st_mode = st_mode | stat.S_ISGID
        file_type_to_mode = {'d': stat.S_IFDIR, 'l': stat.S_IFLNK,
                             'c': stat.S_IFCHR, '-': stat.S_IFREG}
        file_type = mode_string[0]
        if file_type in file_type_to_mode:
            st_mode = st_mode | file_type_to_mode[file_type]
        else:
            raise ftp_error.ParserError(
                  "unknown file type character '%s'" % file_type)
        return st_mode

    def parse_unix_time(self, month_abbreviation, day, year_or_time,
                        time_shift):
        """
        Return a floating point number, like from `time.mktime`, by
        parsing the string arguments `month_abbreviation`, `day` and
        `year_or_time`. The parameter `time_shift` is the difference
        "time on server" - "time on client" and is available as the
        `time_shift` parameter in the `parse_line` interface.

        Times in Unix-style directory listings typically have one of
        these formats:

        - "Nov 23 02:33" (month name, day of month, time)

        - "May 26  2005" (month name, day of month, year)

        If this method can not make sense of the given arguments, it
        raises an `ftp_error.ParserError`.
        """
        try:
            month = self._month_numbers[month_abbreviation.lower()]
        except KeyError:
            raise ftp_error.ParserError("invalid month name '%s'" % month)
        day = int(day)
        if ":" not in year_or_time:
            # `year_or_time` is really a year
            year, hour, minute = int(year_or_time), 0, 0
            st_mtime = time.mktime( (year, month, day,
                                     hour, minute, 0, 0, 0, -1) )
        else:
            # `year_or_time` is a time hh:mm
            hour, minute = year_or_time.split(':')
            year, hour, minute = None, int(hour), int(minute)
            # try the current year
            year = time.localtime()[0]
            st_mtime = time.mktime( (year, month, day,
                                     hour, minute, 0, 0, 0, -1) )
            # rhs of comparison: transform client time to server time
            #  (as on the lhs), so both can be compared with respect
            #  to the set time shift (see the definition of the time
            #  shift in `FTPHost.set_time_shift`'s docstring); the
            #  last addend allows for small deviations between the
            #  supposed (rounded) and the actual time shift
            # #XXX the downside of this "correction" is that there is
            #  a one-minute time interval exactly one year ago that
            #  may cause that datetime to be recognized as the current
            #  datetime, but after all the datetime from the server
            #  can only be exact up to a minute
            if st_mtime > time.time() + time_shift + 60.0:
                # if it's in the future, use previous year
                st_mtime = time.mktime( (year-1, month, day,
                                         hour, minute, 0, 0, 0, -1) )
        return st_mtime

    def parse_ms_time(self, date, time_, time_shift):
        """
        Return a floating point number, like from `time.mktime`, by
        parsing the string arguments `date` and `time_`. The parameter
        `time_shift` is the difference
        "time on server" - "time on client"
        and is available as the `time_shift` parameter in the
        `parse_line` interface.

        Times in MS-style directory listings typically have the
        format "10-23-01 03:25PM" (month-day_of_month-two_digit_year,
        hour:minute, am/pm).

        If this method can not make sense of the given arguments, it
        raises an `ftp_error.ParserError`.
        """
        try:
            month, day, year = map(int, date.split('-'))
            if year >= 70:
                year = 1900 + year
            else:
                year = 2000 + year
            hour, minute, am_pm = time_[0:2], time_[3:5], time_[5]
            hour, minute = int(hour), int(minute)
        except (ValueError, IndexError):
            raise ftp_error.ParserError("invalid time string '%s'" % time_)
        if am_pm == 'P':
            hour = hour + 12
        st_mtime = time.mktime( (year, month, day,
                                 hour, minute, 0, 0, 0, -1) )
        return st_mtime


class UnixParser(Parser):
    """`Parser` class for Unix-specific directory format."""
    def _split_line(self, line):
        """
        Split a line in metadata, nlink, user, group, size, month,
        day, year_or_time and name and return the result as an
        nine-element list of these values.
        """
        # This method encapsulates the recognition of an unusual
        #  Unix format variant (see ticket
        #  http://ftputil.sschwarzer.net/trac/ticket/12 )
        parts = line.split(None, 8)
        if len(parts) == 9:
            if parts[-1].startswith("-> "):
                # for the alternative format, the last part will not be
                #  "link_name -> link_target" but "-> link_target" and the
                #  link name will be in the previous field;
                # this heuristic will fail for names starting with "-> "
                #  which should be _quite_ rare
                # insert `None` for the user field
                parts.insert(2, None)
                parts[-2] = "%s %s" % tuple(parts[-2:])
                del parts[-1]
            return parts
        elif len(parts) == 8:
            # alternative unusual format, insert `None` for the user field
            parts.insert(2, None)
            return parts
        else:
            # no known Unix-style format
            raise ftp_error.ParserError("line '%s' can't be parsed" % line)

    def parse_line(self, line, time_shift=0.0):
        """
        Return a `StatResult` instance corresponding to the given
        text line. The `time_shift` value is needed to determine
        to which year a datetime without an explicit year belongs.

        If the line can't be parsed, raise a `ParserError`.
        """
        mode_string, nlink, user, group, size, month, day, \
          year_or_time, name = self._split_line(line)
        # st_mode
        st_mode = self.parse_unix_mode(mode_string)
        # st_ino, st_dev, st_nlink, st_uid, st_gid, st_size, st_atime
        st_ino = None
        st_dev = None
        st_nlink = int(nlink)
        st_uid = user
        st_gid = group
        st_size = int(size)
        st_atime = None
        # st_mtime
        st_mtime = self.parse_unix_time(month, day, year_or_time, time_shift)
        # st_ctime
        st_ctime = None
        # st_name
        if " -> " in name:
            st_name, st_target = name.split(' -> ')
        else:
            st_name, st_target = name, None
        stat_result = StatResult(
                      (st_mode, st_ino, st_dev, st_nlink, st_uid,
                       st_gid, st_size, st_atime, st_mtime, st_ctime) )
        stat_result._st_name = st_name
        stat_result._st_target = st_target
        return stat_result


class MSParser(Parser):
    """`Parser` class for MS-specific directory format."""
    def parse_line(self, line, time_shift=0.0):
        """
        Return a `StatResult` instance corresponding to the given
        text line from a FTP server which emits "Microsoft format"
        (see end of file).

        If the line can't be parsed, raise a `ParserError`.

        The parameter `time_shift` isn't used in this method but is
        listed for compatibilty with the base class.
        """
        try:
            date, time_, dir_or_size, name = line.split(None, 3)
        except ValueError:
            # "unpack list of wrong size"
            raise ftp_error.ParserError("line '%s' can't be parsed" % line )
        # st_mode
        #  default to read access only; in fact, we can't tell
        st_mode = 0400
        if dir_or_size == "<DIR>":
            st_mode = st_mode | stat.S_IFDIR
        else:
            st_mode = st_mode | stat.S_IFREG
        # st_ino, st_dev, st_nlink, st_uid, st_gid
        st_ino = None
        st_dev = None
        st_nlink = None
        st_uid = None
        st_gid = None
        # st_size
        if dir_or_size != "<DIR>":
            try:
                st_size = int(dir_or_size)
            except ValueError:
                raise ftp_error.ParserError("invalid size %s" % dir_or_size)
        else:
            st_size = None
        # st_atime
        st_atime = None
        # st_mtime
        st_mtime = self.parse_ms_time(date, time_, time_shift)
        # st_ctime
        st_ctime = None
        stat_result = StatResult(
                      (st_mode, st_ino, st_dev, st_nlink, st_uid,
                       st_gid, st_size, st_atime, st_mtime, st_ctime) )
        # _st_name and _st_target
        stat_result._st_name = name
        stat_result._st_target = None
        return stat_result

#
# Stat'ing operations for files on an FTP server
#
class _Stat(object):
    """Methods for stat'ing directories, links and regular files."""
    def __init__(self, host):
        self._host = host
        self._path = host.path
        # use the Unix directory parser by default
        self._parser = UnixParser()
        # allow one chance to switch to another parser if the default
        #  doesn't work
        self._allow_parser_switching = True
        # cache only lstat results; `stat` works locally on `lstat` results
        self._lstat_cache = ftp_stat_cache.StatCache()

    def _host_dir(self, path):
        """
        Return a list of lines, as fetched by FTP's `DIR` command,
        when applied to `path`.
        """
        return self._host._dir(path)

    def _real_listdir(self, path):
        """
        Return a list of directories, files etc. in the directory
        named `path`.

        If the directory listing from the server can't be parsed
        raise a `ParserError`.
        """
        # we _can't_ put this check into `FTPHost._dir`; see its docstring
        path = self._path.abspath(path)
        if not self._path.isdir(path):
            raise ftp_error.PermanentError(
                  "550 %s: no such directory or wrong directory parser used" %
                  path)
        # set up for loop
        lines = self._host_dir(path)
        # exit the method now if there aren't any files
        if lines == ['']:
            return []
        names = []
        for line in lines:
            if self._parser.ignores_line(line):
                continue
            # for `listdir`, we are interested in just the names,
            #  but we use the `time_shift` parameter to have the
            #  correct timestamp values in the cache
            stat_result = self._parser.parse_line(line,
                                                  self._host.time_shift())
            loop_path = self._path.join(path, stat_result._st_name)
            self._lstat_cache[loop_path] = stat_result
            st_name = stat_result._st_name
            if st_name not in (self._host.curdir, self._host.pardir):
                names.append(st_name)
        return names

    def _real_lstat(self, path, _exception_for_missing_path=True):
        """
        Return an object similar to that returned by `os.lstat`.

        If the directory listing from the server can't be parsed,
        raise a `ParserError`. If the directory can be parsed and the
        `path` is not found, raise a `PermanentError`. That means that
        if the directory containing `path` can't be parsed we get a
        `ParserError`, independent on the presence of `path` on the
        server.

        (`_exception_for_missing_path` is an implementation aid and
        _not_ intended for use by ftputil clients.)
        """
        path = self._path.abspath(path)
        # if the path is in the cache, return the lstat result
        if path in self._lstat_cache:
            return self._lstat_cache[path]
        # get output from FTP's `DIR` command
        lines = []
        # Note: (l)stat works by going one directory up and parsing
        #  the output of an FTP `DIR` command. Unfortunately, it is
        #  not possible to do this for the root directory `/`.
        if path == '/':
            raise ftp_error.RootDirError(
                  "can't stat remote root directory")
        dirname, basename = self._path.split(path)
        lstat_result_for_path = None
        # loop through all lines of the directory listing; we
        #  probably won't need all lines for the particular path but
        #  we want to collect as many stat results in the cache as
        #  possible
        lines = self._host_dir(dirname)
        for line in lines:
            if self._parser.ignores_line(line):
                continue
            stat_result = self._parser.parse_line(line,
                          self._host.time_shift())
            loop_path = self._path.join(dirname, stat_result._st_name)
            self._lstat_cache[loop_path] = stat_result
            # needed to work without cache or with disabled cache
            if stat_result._st_name == basename:
                lstat_result_for_path = stat_result
        if lstat_result_for_path:
            return lstat_result_for_path
        # path was not found
        if _exception_for_missing_path:
            raise ftp_error.PermanentError(
                  "550 %s: no such file or directory" % path)
        else:
            # be explicit; returning `None` is a signal for
            #  `_Path.exists/isfile/isdir/islink` that the path was
            #  not found; if we would raise an exception, there would
            #  be no distinction between a missing path or a more
            #  severe error in the code above
            return None

    def _real_stat(self, path, _exception_for_missing_path=True):
        """
        Return info from a "stat" call on `path`.

        If the directory containing `path` can't be parsed, raise
        a `ParserError`. If the listing can be parsed but the
        `path` can't be found, raise a `PermanentError`. Also raise
        a `PermanentError` if there's an endless (cyclic) chain of
        symbolic links "behind" the `path`.

        (`_exception_for_missing_path` is an implementation aid and
        _not_ intended for use by ftputil clients.)
        """
        # save for error message
        original_path = path
        # most code in this method is used to detect recursive
        #  link structures
        visited_paths = {}
        while True:
            # stat the link if it is one, else the file/directory
            lstat_result = self._real_lstat(path, _exception_for_missing_path)
            if lstat_result is None:
                return None
            # if the file is not a link, the `stat` result is the
            #  same as the `lstat` result
            if not stat.S_ISLNK(lstat_result.st_mode):
                return lstat_result
            # if we stat'ed a link, calculate a normalized path for
            #  the file the link points to
            dirname, basename = self._path.split(path)
            path = self._path.join(dirname, lstat_result._st_target)
            path = self._path.normpath(path)
            # check for cyclic structure
            if path in visited_paths:
                # we had this path already
                raise ftp_error.PermanentError(
                      "recursive link structure detected for remote path '%s'" %
                      original_path)
            # remember the path we have encountered
            visited_paths[path] = True

    def __call_with_parser_retry(self, method, *args, **kwargs):
        """
        Call `method` with the `args` and `kwargs` once. If that
        results in a `ParserError` and only one parser has been
        used yet, try the other parser. If that still fails,
        propagate the `ParserError`.
        """
        # Do _not_ set `_allow_parser_switching` in a `finally` clause!
        #  This would cause a `PermanentError` due to a not-found
        #  file in an empty directory to finally establish the
        #  parser - which is wrong.
        try:
            result = method(*args, **kwargs)
            # if a `listdir` call didn't find anything, we can't
            #  say anything about the usefulness of the parser
            if (method is not self._real_listdir) and result:
                self._allow_parser_switching = False
            return result
        except ftp_error.ParserError:
            if self._allow_parser_switching:
                self._allow_parser_switching = False
                self._parser = MSParser()
                return method(*args, **kwargs)
            else:
                raise

    def listdir(self, path):
        return self.__call_with_parser_retry(self._real_listdir, path)

    def lstat(self, path, _exception_for_missing_path=True):
        return self.__call_with_parser_retry(self._real_lstat, path,
                                             _exception_for_missing_path)

    def stat(self, path, _exception_for_missing_path=True):
        return self.__call_with_parser_retry(self._real_stat, path,
                                             _exception_for_missing_path)


# Copyright (C) 2003-2006, Stefan Schwarzer
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
ftp_path.py - simulate `os.path` for FTP servers
"""

# $Id: ftp_path.py 647 2006-11-23 01:33:53Z schwa $

import posixpath
import stat

import ftp_error


class _Path(object):
    """
    Support class resembling `os.path`, accessible from the `FTPHost`
    object, e. g. as `FTPHost().path.abspath(path)`.

    Hint: substitute `os` with the `FTPHost` object.
    """
    def __init__(self, host):
        self._host = host
        # delegate these to the `posixpath` module
        pp = posixpath
        self.dirname      = pp.dirname
        self.basename     = pp.basename
        self.isabs        = pp.isabs
        self.commonprefix = pp.commonprefix
        self.join         = pp.join
        self.split        = pp.split
        self.splitdrive   = pp.splitdrive
        self.splitext     = pp.splitext
        self.normcase     = pp.normcase
        self.normpath     = pp.normpath

    def abspath(self, path):
        """Return an absolute path."""
        if not self.isabs(path):
            path = self.join(self._host.getcwd(), path)
        return self.normpath(path)

    def exists(self, path):
        try:
            lstat_result = self._host.lstat(
                           path, _exception_for_missing_path=False)
            return lstat_result is not None
        except ftp_error.RootDirError:
            return True

    def getmtime(self, path):
        return self._host.stat(path).st_mtime

    def getsize(self, path):
        return self._host.stat(path).st_size

    # check whether a path is a regular file/dir/link;
    #  for the first two cases follow links (like in `os.path`)
    #
    # Implementation note: The previous implementations simply called
    # `stat` or `lstat` and returned `False` if they ended with
    # raising a `PermanentError`. That exception usually used to
    # signal a missing path. This approach has the problem, however,
    # that exceptions caused by code earlier in `lstat` are obscured
    # by the exception handling in `isfile`, `isdir` and `islink`.

    def isfile(self, path):
        # workaround if we can't go up from the current directory
        if path == self._host.getcwd():
            return False
        try:
            stat_result = self._host.stat(
                          path, _exception_for_missing_path=False)
            if stat_result is None:
                return False
            else:
                return stat.S_ISREG(stat_result.st_mode)
        except ftp_error.RootDirError:
            return False

    def isdir(self, path):
        # workaround if we can't go up from the current directory
        if path == self._host.getcwd():
            return True
        try:
            stat_result = self._host.stat(
                          path, _exception_for_missing_path=False)
            if stat_result is None:
                return False
            else:
                return stat.S_ISDIR(stat_result.st_mode)
        except ftp_error.RootDirError:
            return True

    def islink(self, path):
        try:
            lstat_result = self._host.lstat(
                           path, _exception_for_missing_path=False)
            if lstat_result is None:
                return False
            else:
                return stat.S_ISLNK(lstat_result.st_mode)
        except ftp_error.RootDirError:
            return False

    def walk(self, top, func, arg):
        """
        Directory tree walk with callback function.

        For each directory in the directory tree rooted at top
        (including top itself, but excluding '.' and '..'), call
        func(arg, dirname, fnames). dirname is the name of the
        directory, and fnames a list of the names of the files and
        subdirectories in dirname (excluding '.' and '..').  func may
        modify the fnames list in-place (e.g. via del or slice
        assignment), and walk will only recurse into the
        subdirectories whose names remain in fnames; this can be used
        to implement a filter, or to impose a specific order of
        visiting.  No semantics are defined for, or required of, arg,
        beyond that arg is always passed to func.  It can be used,
        e.g., to pass a filename pattern, or a mutable object designed
        to accumulate statistics.  Passing None for arg is common.
        """
        # This code (and the above documentation) is taken from
        #  posixpath.py, with slight modifications
        try:
            names = self._host.listdir(top)
        except OSError:
            return
        func(arg, top, names)
        for name in names:
            name = self.join(top, name)
            try:
                st = self._host.lstat(name)
            except OSError:
                continue
            if stat.S_ISDIR(st[stat.ST_MODE]):
                self.walk(name, func, arg)


# Copyright (C) 2003-2006, Stefan Schwarzer <sschwarzer@sschwarzer.net>
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
ftp_file.py - support for file-like objects on FTP servers
"""

# $Id: ftp_file.py 679 2007-01-26 08:04:51Z schwa $

import ftp_error


# converter for `\r\n` line ends to normalized ones in Python. RFC 959
#  states that the server will send `\r\n` on text mode transfers, so
#  this conversion should be safe. I still use text mode transfers
#  (mode 'r', not 'rb') in `socket.makefile` (below) because the
#  server may do charset conversions on text transfers.
#
# Note that the "obvious" implementation of replacing "\r\n" with
#  "\n" would fail, if "\r" (without "\n") occured at the end of the
#  string `text`
_crlf_to_python_linesep = lambda text: text.replace('\r', '')

# converter for Python line ends into `\r\n`
_python_to_crlf_linesep = lambda text: text.replace('\n', '\r\n')


# helper class for xreadline protocol for ASCII transfers
#XXX maybe we can use the `xreadlines` module instead of this?
class _XReadlines(object):
    """Represents `xreadline` objects for ASCII transfers."""
    def __init__(self, ftp_file):
        self._ftp_file = ftp_file
        self._next_index = 0

    def __getitem__(self, index):
        """Return next line with specified index."""
        if index != self._next_index:
            raise RuntimeError( "_XReadline access index "
                  "out of order (expected %s but got %s)" %
                  (self._next_index, index) )
        line = self._ftp_file.readline()
        if not line:
            raise IndexError("_XReadline object out of data")
        self._next_index += 1
        return line


class _FTPFile(object):
    """
    Represents a file-like object connected to an FTP host. File and
    socket are closed appropriately if the `close` operation is
    requested.
    """
    def __init__(self, host):
        """Construct the file(-like) object."""
        self._host = host
        self._session = host._session
        # the file is closed yet
        self.closed = True

    def _open(self, path, mode):
        """Open the remote file with given path name and mode."""
        # check mode
        if 'a' in mode:
            raise ftp_error.FTPIOError("append mode not supported")
        if mode not in ('r', 'rb', 'w', 'wb'):
            raise ftp_error.FTPIOError("invalid mode '%s'" % mode)
        # remember convenience variables instead of mode
        self._bin_mode = 'b' in mode
        self._read_mode = 'r' in mode
        # select ASCII or binary mode
        transfer_type = ('A', 'I')[self._bin_mode]
        command = 'TYPE %s' % transfer_type
        ftp_error._try_with_ioerror(self._session.voidcmd, command)
        # make transfer command
        command_type = ('STOR', 'RETR')[self._read_mode]
        command = '%s %s' % (command_type, path)
        # ensure we can process the raw line separators;
        #  force to binary regardless of transfer type
        if not 'b' in mode:
            mode = mode + 'b'
        # get connection and file object
        self._conn = ftp_error._try_with_ioerror(
                     self._session.transfercmd, command)
        self._fo = self._conn.makefile(mode)
        # this comes last so that `close` does not try to
        #  close `_FTPFile` objects without `_conn` and `_fo`
        #  attributes
        self.closed = False

    #
    # Read and write operations with support for line separator
    # conversion for text modes.
    #
    # Note that we must convert line endings because the FTP server
    # expects `\r\n` to be sent on text transfers.
    #
    def read(self, *args):
        """Return read bytes, normalized if in text transfer mode."""
        data = self._fo.read(*args)
        if self._bin_mode:
            return data
        data = _crlf_to_python_linesep(data)
        if args == ():
            return data
        # If the read data contains `\r` characters the number of read
        #  characters will be too small! Thus we (would) have to
        #  continue to read until we have fetched the requested number
        #  of bytes (or run out of source data).
        #
        # The algorithm below avoids repetitive string concatanations
        #  in the style of
        #      data = data + more_data
        #  and so should also work relatively well if there are many
        #  short lines in the file.
        wanted_size = args[0]
        chunks = [data]
        current_size = len(data)
        while current_size < wanted_size:
            # print 'not enough bytes (now %s, wanting %s)' % \
            #       (current_size, wanted_size)
            more_data = self._fo.read(wanted_size - current_size)
            if not more_data:
                break
            more_data = _crlf_to_python_linesep(more_data)
            # print '-> new (normalized) data:', repr(more_data)
            chunks.append(more_data)
            current_size += len(more_data)
        return ''.join(chunks)

    def readline(self, *args):
        """Return one read line, normalized if in text transfer mode."""
        data = self._fo.readline(*args)
        if self._bin_mode:
            return data
        # if necessary, complete begun newline
        if data.endswith('\r'):
            data = data + self.read(1)
        return _crlf_to_python_linesep(data)

    def readlines(self, *args):
        """Return read lines, normalized if in text transfer mode."""
        lines = self._fo.readlines(*args)
        if self._bin_mode:
            return lines
        # more memory-friendly than `return [... for line in lines]`
        for index, line in enumerate(lines):
            lines[index] = _crlf_to_python_linesep(line)
        return lines

    def xreadlines(self):
        """
        Return an appropriate `xreadlines` object with built-in line
        separator conversion support.
        """
        if self._bin_mode:
            return self._fo.xreadlines()
        return _XReadlines(self)

    def __iter__(self):
        """Return a file iterator."""
        return self

    def next(self):
        """
        Return the next line or raise `StopIteration`, if there are
        no more.
        """
        # apply implicit line ending conversion
        line = self.readline()
        if line:
            return line
        else:
            raise StopIteration

    def write(self, data):
        """Write data to file. Do linesep conversion for text mode."""
        if not self._bin_mode:
            data = _python_to_crlf_linesep(data)
        self._fo.write(data)

    def writelines(self, lines):
        """Write lines to file. Do linesep conversion for text mode."""
        if self._bin_mode:
            self._fo.writelines(lines)
            return
        # we can't modify the list of lines in-place, as in the
        #  `readlines` method; that would modify the original list,
        #  given as argument `lines`
        for line in lines:
            self._fo.write(_python_to_crlf_linesep(line))

    #
    # other attributes
    #
    def __getattr__(self, attr_name):
        """
        Handle requests for attributes unknown to `_FTPFile` objects:
        delegate the requests to the contained file object.
        """
        if attr_name in ('flush isatty fileno seek tell '
                         'truncate name softspace'.split()):
            return getattr(self._fo, attr_name)
        raise AttributeError(
              "'FTPFile' object has no attribute '%s'" % attr_name)

    def close(self):
        """Close the `FTPFile`."""
        if not self.closed:
            self._fo.close()
            ftp_error._try_with_ioerror(self._conn.close)
            try:
                ftp_error._try_with_ioerror(self._session.voidresp)
            except ftp_error.FTPIOError, exception:
                # ignore some errors, see ticket #17 at
                #  http://ftputil.sschwarzer.net/trac/ticket/17
                error_code = str(exception).split()[0]
                if error_code not in ("426", "450", "451"):
                    raise
            self.closed = True


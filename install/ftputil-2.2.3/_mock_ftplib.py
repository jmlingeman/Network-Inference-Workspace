# Copyright (C) 2003-2007, Stefan Schwarzer
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

# $Id: _mock_ftplib.py 689 2007-04-16 01:07:10Z schwa $

"""
This module implements a mock version of the standard library's
`ftplib.py` module. Some code is taken from there.

Not all functionality is implemented, only that what is used to
run the unit tests.
"""

import ftplib
import StringIO

DEBUG = 0

# Use a global dictionary of the form `{path: mock_file, ...}` to
#  make "volatile" mock files accessible. This is used for testing
#  the contents of a file after an `FTPHost.upload` call.
mock_files = {}

def content_of(path):
    return mock_files[path].getvalue()


class MockFile(StringIO.StringIO):
    """
    Mock class for the file objects _contained in_ `_FTPFile` objects
    (not `_FTPFile` objects themselves!).

    Unless `StringIO.StringIO` instances, `MockFile` objects can be
    queried for their contents after they have been closed.
    """
    def __init__(self, path, content=''):
        global mock_files
        mock_files[path] = self
        StringIO.StringIO.__init__(self, content)

    def getvalue(self):
        if not self.closed:
            return StringIO.StringIO.getvalue(self)
        else:
            return self._value_after_close

    def close(self):
        if not self.closed:
            self._value_after_close = StringIO.StringIO.getvalue(self)
        StringIO.StringIO.close(self)


class MockSocket(object):
    """
    Mock class which is used to return something from
    `MockSession.transfercmd`.
    """
    def __init__(self, path, mock_file_content=''):
        if DEBUG:
            print 'File content: *%s*' % mock_file_content
        self.file_path = path
        self.mock_file_content = mock_file_content

    def makefile(self, mode):
        return MockFile(self.file_path, self.mock_file_content)

    def close(self):
        pass


class MockSession(object):
    """
    Mock class which works like `ftplib.FTP` for the purpose of the
    unit tests.
    """
    # used by MockSession.cwd and MockSession.pwd
    current_dir = '/home/sschwarzer'

    # used by MockSession.dir
    dir_contents = {
      '/': """\
drwxr-xr-x   2 45854    200           512 May  4  2000 home""",

      '/home': """\
drwxr-sr-x   2 45854    200           512 May  4  2000 sschwarzer
-rw-r--r--   1 45854    200          4605 Jan 19  1970 older
-rw-r--r--   1 45854    200          4605 Jan 19  2020 newer
lrwxrwxrwx   1 45854    200            21 Jan 19  2002 link -> sschwarzer/index.html
lrwxrwxrwx   1 45854    200            15 Jan 19  2002 bad_link -> python/bad_link""",

      '/home/python': """\
lrwxrwxrwx   1 45854    200             7 Jan 19  2002 link_link -> ../link
lrwxrwxrwx   1 45854    200            14 Jan 19  2002 bad_link -> /home/bad_link""",

      '/home/sschwarzer': """\
total 14
drwxr-sr-x   2 45854    200           512 May  4  2000 chemeng
drwxr-sr-x   2 45854    200           512 Jan  3 17:17 download
drwxr-sr-x   2 45854    200           512 Jul 30 17:14 image
-rw-r--r--   1 45854    200          4604 Jan 19 23:11 index.html
drwxr-sr-x   2 45854    200           512 May 29  2000 os2
lrwxrwxrwx   2 45854    200             6 May 29  2000 osup -> ../os2
drwxr-sr-x   2 45854    200           512 May 25  2000 publications
drwxr-sr-x   2 45854    200           512 Jan 20 16:12 python
drwxr-sr-x   6 45854    200           512 Sep 20  1999 scios2""",

      # = /home/dir with spaces
      '.': """\
total 1
-rw-r--r--   1 45854    200          4604 Jan 19 23:11 file with spaces""",

      # fail when trying to write to this directory (the content isn't
      #  relevant)
      'sschwarzer': "",

      '/home/msformat': """\
10-23-01  03:25PM       <DIR>          WindowsXP
12-07-01  02:05PM       <DIR>          XPLaunch
07-17-00  02:08PM             12266720 abcd.exe
07-17-00  02:08PM                89264 O2KKeys.exe""",

      '/home/msformat/XPLaunch': """\
10-23-01  03:25PM       <DIR>          WindowsXP
12-07-01  02:05PM       <DIR>          XPLaunch
12-07-01  02:05PM       <DIR>          empty
07-17-00  02:08PM             12266720 abcd.exe
07-17-00  02:08PM                89264 O2KKeys.exe""",

      '/home/msformat/XPLaunch/empty': "total 0",
    }

    # file content to be used (indirectly) with transfercmd
    mock_file_content = ''

    def __init__(self, host='', user='', password=''):
        self.closed = 0
        # count successful `transfercmd` invocations to ensure that
        #  each has a corresponding `voidresp`
        self._transfercmds = 0

    def _remove_trailing_slash(self, path):
        if path != '/' and path.endswith('/'):
            path = path[:-1]
        return path

    def voidcmd(self, cmd):
        if DEBUG:
            print cmd
        if cmd == 'STAT':
            return 'MockSession server awaiting your commands ;-)'
        elif cmd.startswith('TYPE '):
            return
        else:
            raise ftplib.error_perm

    def pwd(self):
        return self.current_dir

    def cwd(self, path):
        path = self._remove_trailing_slash(path)
        self.current_dir = path

    def dir(self, path, callback=None):
        "Provide a callback function with each line of a directory listing."
        if DEBUG:
            print 'dir: %s' % path
        path = self._remove_trailing_slash(path)
        if not self.dir_contents.has_key(path):
            raise ftplib.error_perm
        dir_lines = self.dir_contents[path].split('\n')
        for line in dir_lines:
            if callback is None:
                print line
            else:
                callback(line)

    def voidresp(self):
        assert self._transfercmds == 1
        self._transfercmds = self._transfercmds - 1
        return '2xx'

    def transfercmd(self, cmd):
        """
        Return a `MockSocket` object whose `makefile` method will
        return a mock file object.
        """
        if DEBUG:
            print cmd
        # fail if attempting to read from/write to a directory
        cmd, path = cmd.split()
        path = self._remove_trailing_slash(path)
        if self.dir_contents.has_key(path):
            raise ftplib.error_perm
        # fail if path isn't available (this name is hard-coded here
        #  and has to be used for the corresponding tests)
        if (cmd, path) == ('RETR', 'notthere'):
            raise ftplib.error_perm
        assert self._transfercmds == 0
        self._transfercmds = self._transfercmds + 1
        return MockSocket(path, self.mock_file_content)

    def close(self):
        if not self.closed:
            self.closed = 1
            assert self._transfercmds == 0


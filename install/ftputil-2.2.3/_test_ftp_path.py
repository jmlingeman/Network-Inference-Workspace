# Copyright (C) 2003-2004, Stefan Schwarzer
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

# $Id: _test_ftp_path.py 689 2007-04-16 01:07:10Z schwa $

import ftplib
import unittest

import _mock_ftplib
import _test_base
import ftp_error
import ftputil


class FailingFTPHost(ftputil.FTPHost):
    def _dir(self, path):
        raise ftp_error.FTPOSError("simulate a failure, e. g. timeout")


# mock session, used for testing an inaccessible login directory
class SessionWithInaccessibleLoginDirectory(_mock_ftplib.MockSession):
    def cwd(self, dir):
        # assume that `dir` is the inaccessible login directory
        raise ftplib.error_perm("can't change into this directory")


class TestPath(unittest.TestCase):
    """Test operations in `FTPHost.path`."""
    def test_regular_isdir_isfile_islink(self):
        """Test regular `FTPHost._Path.isdir/isfile/islink`."""
        testdir = '/home/sschwarzer'
        host = _test_base.ftp_host_factory()
        host.chdir(testdir)
        # test a path which isn't there
        self.failIf(host.path.isdir('notthere'))
        self.failIf(host.path.isfile('notthere'))
        self.failIf(host.path.islink('notthere'))
        # test a directory
        self.failUnless(host.path.isdir(testdir))
        self.failIf(host.path.isfile(testdir))
        self.failIf(host.path.islink(testdir))
        # test a file
        testfile = '/home/sschwarzer/index.html'
        self.failIf(host.path.isdir(testfile))
        self.failUnless(host.path.isfile(testfile))
        self.failIf(host.path.islink(testfile))
        # test a link
        testlink = '/home/sschwarzer/osup'
        self.failIf(host.path.isdir(testlink))
        self.failIf(host.path.isfile(testlink))
        self.failUnless(host.path.islink(testlink))

    def test_workaround_for_spaces(self):
        """Test whether the workaround for space-containing paths is used."""
        testdir = '/home/sschwarzer'
        host = _test_base.ftp_host_factory()
        host.chdir(testdir)
        # test a file containing spaces
        testfile = '/home/dir with spaces/file with spaces'
        self.failIf(host.path.isdir(testfile))
        self.failUnless(host.path.isfile(testfile))
        self.failIf(host.path.islink(testfile))

    def test_inaccessible_home_directory_and_whitespace_workaround(self):
        "Test combination of inaccessible home directory + whitespace in path."
        host = _test_base.ftp_host_factory(
               session_factory=SessionWithInaccessibleLoginDirectory)
        self.assertRaises(ftp_error.InaccessibleLoginDirError,
                          host._dir, '/home dir')

    def test_abnormal_isdir_isfile_islink(self):
        """Test abnormal `FTPHost._Path.isdir/isfile/islink`."""
        testdir = '/home/sschwarzer'
        host = _test_base.ftp_host_factory(ftp_host_class=FailingFTPHost)
        host.chdir(testdir)
        # test a path which isn't there
        self.assertRaises(ftp_error.FTPOSError, host.path.isdir, "index.html")
        self.assertRaises(ftp_error.FTPOSError, host.path.isfile, "index.html")
        self.assertRaises(ftp_error.FTPOSError, host.path.islink, "index.html")

    def test_exists(self):
        """Test if "abnormal" FTP errors come through `path.exists`."""
        # regular use of `exists`
        testdir = '/home/sschwarzer'
        host = _test_base.ftp_host_factory()
        host.chdir(testdir)
        self.assertEqual(host.path.exists("index.html"), True)
        self.assertEqual(host.path.exists("notthere"), False)
        # "abnormal" failure
        host = _test_base.ftp_host_factory(ftp_host_class=FailingFTPHost)
        self.assertRaises(ftp_error.FTPOSError, host.path.exists, "index.html")


if __name__ == '__main__':
    unittest.main()


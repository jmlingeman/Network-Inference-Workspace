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

# $Id: _test_ftputil.py 689 2007-04-16 01:07:10Z schwa $

import ftplib
import operator
import os
import posixpath
import random
import stat
import time
import unittest

import _mock_ftplib
import _test_base
import ftp_error
import ftp_file
import ftp_stat
import ftputil

#
# helper functions to generate random data
#
def random_data(pool, size=10000):
    """
    Return a sequence of characters consisting of those from
    the pool of integer numbers.
    """
    character_list = []
    for i in range(size):
        ordinal = random.choice(pool)
        character_list.append(chr(ordinal))
    result = ''.join(character_list)
    return result

def ascii_data():
    """Return an ASCII character string."""
    pool = range(32, 128)
    pool.append(ord('\n'))
    return random_data(pool)

def binary_data():
    """Return a binary character string."""
    pool = range(0, 256)
    return random_data(pool)


#
# several customized `MockSession` classes
#
class FailOnLoginSession(_mock_ftplib.MockSession):
    def __init__(self, host='', user='', password=''):
        raise ftplib.error_perm

class ReadMockSession(_mock_ftplib.MockSession):
    mock_file_content = 'line 1\r\nanother line\r\nyet another line'

class AsciiReadMockSession(_mock_ftplib.MockSession):
    mock_file_content = '\r\n'.join(map(str, range(20)))

class BinaryDownloadMockSession(_mock_ftplib.MockSession):
    mock_file_content = binary_data()

class TimeShiftMockSession(_mock_ftplib.MockSession):
    def delete(self, file_name):
        pass

class InaccessibleDirSession(_mock_ftplib.MockSession):
    _login_dir = '/inaccessible'

    def pwd(self):
        return self._login_dir

    def cwd(self, dir):
        if dir in (self._login_dir, self._login_dir + '/'):
            raise ftplib.error_perm
        else:
            _mock_ftplib.MockSession.cwd(self, dir)

#
# customized `FTPHost` class for conditional upload/download tests
#  and time shift tests
#
class FailingUploadAndDownloadFTPHost(ftputil.FTPHost):
    def upload(self, source, target, mode=''):
        assert False, "`FTPHost.upload` should not have been called"

    def download(self, source, target, mode=''):
        assert False, "`FTPHost.download` should not have been called"

class TimeShiftFTPHost(ftputil.FTPHost):
    class _Path:
        def split(self, path):
            return posixpath.split(path)
        def set_mtime(self, mtime):
            self._mtime = mtime
        def getmtime(self, file_name):
            return self._mtime
        def abspath(self, path):
            return "/home/sschwarzer/_ftputil_sync_"
        # needed for `isdir` in `FTPHost.remove`
        def isfile(self, path):
            return True

    def __init__(self, *args, **kwargs):
        ftputil.FTPHost.__init__(self, *args, **kwargs)
        self.path = self._Path()

#
# test cases
#
class TestOpenAndClose(unittest.TestCase):
    """Test opening and closing of `FTPHost` objects."""
    def test_open_and_close(self):
        """Test closing of `FTPHost`."""
        host = _test_base.ftp_host_factory()
        host.close()
        self.assertEqual(host.closed, 1)
        self.assertEqual(host._children, [])


class TestLogin(unittest.TestCase):
    def test_invalid_login(self):
        """Login to invalid host must fail."""
        self.assertRaises(ftp_error.FTPOSError, _test_base.ftp_host_factory,
                          FailOnLoginSession)


class TestSetParser(unittest.TestCase):
    def test_set_parser(self):
        """Test if the selected parser is used."""
        # this test isn't very practical but should help at least a bit ...
        host = _test_base.ftp_host_factory()
        # implicitly fix at Unix format
        files = host.listdir("/home/sschwarzer")
        self.assertEqual(files, ['chemeng', 'download', 'image', 'index.html',
          'os2', 'osup', 'publications', 'python', 'scios2'])
        host.set_parser(ftp_stat.MSParser())
        files = host.listdir("/home/msformat/XPLaunch")
        self.assertEqual(files, ['WindowsXP', 'XPLaunch', 'empty',
          'abcd.exe', 'O2KKeys.exe'])
        self.assertEqual(host._stat._allow_parser_switching, False)


class TestFileOperations(unittest.TestCase):
    """Test operations with file-like objects."""
    def test_inaccessible_dir(self):
        """Test whether opening a file at an invalid location fails."""
        host = _test_base.ftp_host_factory(
               session_factory=InaccessibleDirSession)
        self.assertRaises(ftp_error.FTPIOError, host.file,
                          '/inaccessible/new_file', 'w')

    def test_caching(self):
        """Test whether `_FTPFile` cache of `FTPHost` object works."""
        host = _test_base.ftp_host_factory()
        self.assertEqual(len(host._children), 0)
        path1 = 'path1'
        path2 = 'path2'
        # open one file and inspect cache
        file1 = host.file(path1, 'w')
        child1 = host._children[0]
        self.assertEqual(len(host._children), 1)
        self.failIf(child1._file.closed)
        # open another file
        file2 = host.file(path2, 'w')
        child2 = host._children[1]
        self.assertEqual(len(host._children), 2)
        self.failIf(child2._file.closed)
        # close first file
        file1.close()
        self.assertEqual(len(host._children), 2)
        self.failUnless(child1._file.closed)
        self.failIf(child2._file.closed)
        # re-open first child's file
        file1 = host.file(path1, 'w')
        child1_1 = file1._host
        # check if it's reused
        self.failUnless(child1 is child1_1)
        self.failIf(child1._file.closed)
        self.failIf(child2._file.closed)
        # close second file
        file2.close()
        self.failUnless(child2._file.closed)

    def test_write_to_directory(self):
        """Test whether attempting to write to a directory fails."""
        host = _test_base.ftp_host_factory()
        self.assertRaises(ftp_error.FTPIOError, host.file,
                          '/home/sschwarzer', 'w')

    def test_binary_write(self):
        """Write binary data with `write`."""
        host = _test_base.ftp_host_factory()
        data = '\000a\001b\r\n\002c\003\n\004\r\005'
        output = host.file('dummy', 'wb')
        output.write(data)
        output.close()
        child_data = _mock_ftplib.content_of('dummy')
        expected_data = data
        self.assertEqual(child_data, expected_data)

    def test_ascii_write(self):
        """Write ASCII text with `write`."""
        host = _test_base.ftp_host_factory()
        data = ' \nline 2\nline 3'
        output = host.file('dummy', 'w')
        output.write(data)
        output.close()
        child_data = _mock_ftplib.content_of('dummy')
        expected_data = ' \r\nline 2\r\nline 3'
        self.assertEqual(child_data, expected_data)

    def test_ascii_writelines(self):
        """Write ASCII text with `writelines`."""
        host = _test_base.ftp_host_factory()
        data = [' \n', 'line 2\n', 'line 3']
        backup_data = data[:]
        output = host.file('dummy', 'w')
        output.writelines(data)
        output.close()
        child_data = _mock_ftplib.content_of('dummy')
        expected_data = ' \r\nline 2\r\nline 3'
        self.assertEqual(child_data, expected_data)
        # ensure that the original data was not modified
        self.assertEqual(data, backup_data)

    def test_ascii_read(self):
        """Read ASCII text with plain `read`."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy', 'r')
        data = input_.read(0)
        self.assertEqual(data, '')
        data = input_.read(3)
        self.assertEqual(data, 'lin')
        data = input_.read(7)
        self.assertEqual(data, 'e 1\nano')
        data = input_.read()
        self.assertEqual(data, 'ther line\nyet another line')
        data = input_.read()
        self.assertEqual(data, '')
        input_.close()
        # try it again with a more "problematic" string which
        #  makes several reads in the `read` method necessary
        host = _test_base.ftp_host_factory(session_factory=AsciiReadMockSession)
        expected_data = AsciiReadMockSession.mock_file_content.\
                        replace('\r\n', '\n')
        input_ = host.file('dummy', 'r')
        data = input_.read(len(expected_data))
        self.assertEqual(data, expected_data)

    def test_binary_readline(self):
        """Read binary data with `readline`."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy', 'rb')
        data = input_.readline(3)
        self.assertEqual(data, 'lin')
        data = input_.readline(10)
        self.assertEqual(data, 'e 1\r\n')
        data = input_.readline(13)
        self.assertEqual(data, 'another line\r')
        data = input_.readline()
        self.assertEqual(data, '\n')
        data = input_.readline()
        self.assertEqual(data, 'yet another line')
        data = input_.readline()
        self.assertEqual(data, '')
        input_.close()

    def test_ascii_readline(self):
        """Read ASCII text with `readline`."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy', 'r')
        data = input_.readline(3)
        self.assertEqual(data, 'lin')
        data = input_.readline(10)
        self.assertEqual(data, 'e 1\n')
        data = input_.readline(13)
        self.assertEqual(data, 'another line\n')
        data = input_.readline()
        self.assertEqual(data, 'yet another line')
        data = input_.readline()
        self.assertEqual(data, '')
        input_.close()

    def test_ascii_readlines(self):
        """Read ASCII text with `readlines`."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy', 'r')
        data = input_.read(3)
        self.assertEqual(data, 'lin')
        data = input_.readlines()
        self.assertEqual(data, ['e 1\n', 'another line\n',
                                'yet another line'])
        input_.close()

    def test_ascii_xreadlines(self):
        """Read ASCII text with `xreadlines`."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        # open file, skip some bytes
        input_ = host.file('dummy', 'r')
        data = input_.read(3)
        xrl_obj = input_.xreadlines()
        self.failUnless(xrl_obj.__class__ is ftp_file._XReadlines)
        self.failUnless(xrl_obj._ftp_file.__class__ is ftp_file._FTPFile)
        data = xrl_obj[0]
        self.assertEqual(data, 'e 1\n')
        # try to skip an index
        self.assertRaises(RuntimeError, operator.__getitem__, xrl_obj, 2)
        # continue reading
        data = xrl_obj[1]
        self.assertEqual(data, 'another line\n')
        data = xrl_obj[2]
        self.assertEqual(data, 'yet another line')
        # try to read beyond EOF
        self.assertRaises(IndexError, operator.__getitem__, xrl_obj, 3)

    def test_binary_iterator(self):
        """Test the iterator interface of `FTPFile` objects."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy')
        input_iterator = iter(input_)
        self.assertEqual(input_iterator.next(), "line 1\n")
        self.assertEqual(input_iterator.next(), "another line\n")
        self.assertEqual(input_iterator.next(), "yet another line")
        self.assertRaises(StopIteration, input_iterator.next)
        input_.close()

    def test_ascii_iterator(self):
        """Test the iterator interface of `FTPFile` objects."""
        host = _test_base.ftp_host_factory(session_factory=ReadMockSession)
        input_ = host.file('dummy', 'rb')
        input_iterator = iter(input_)
        self.assertEqual(input_iterator.next(), "line 1\r\n")
        self.assertEqual(input_iterator.next(), "another line\r\n")
        self.assertEqual(input_iterator.next(), "yet another line")
        self.assertRaises(StopIteration, input_iterator.next)
        input_.close()

    def test_read_unknown_file(self):
        """Test whether reading a file which isn't there fails."""
        host = _test_base.ftp_host_factory()
        self.assertRaises(ftp_error.FTPIOError, host.file, 'notthere', 'r')


class TestUploadAndDownload(unittest.TestCase):
    """Test ASCII upload and binary download as examples."""
    def generate_ascii_file(self, data, filename):
        """Generate an ASCII data file."""
        source_file = open(filename, 'w')
        source_file.write(data)
        source_file.close()

    def test_ascii_upload(self):
        """Test ASCII mode upload."""
        local_source = '__test_source'
        data = ascii_data()
        self.generate_ascii_file(data, local_source)
        # upload
        host = _test_base.ftp_host_factory()
        host.upload(local_source, 'dummy')
        # check uploaded content
        # the data which was uploaded has its line endings converted
        #  so the conversion must also be applied to `data`
        data = data.replace('\n', '\r\n')
        remote_file_content = _mock_ftplib.content_of('dummy')
        self.assertEqual(data, remote_file_content)
        # clean up
        os.unlink(local_source)

    def test_binary_download(self):
        """Test binary mode download."""
        local_target = '__test_target'
        host = _test_base.ftp_host_factory(
               session_factory=BinaryDownloadMockSession)
        # download
        host.download('dummy', local_target, 'b')
        # read file and compare
        data = open(local_target, 'rb').read()
        remote_file_content = _mock_ftplib.content_of('dummy')
        self.assertEqual(data, remote_file_content)
        # clean up
        os.unlink(local_target)

    def test_conditional_upload(self):
        """Test conditional ASCII mode upload."""
        local_source = '__test_source'
        data = ascii_data()
        self.generate_ascii_file(data, local_source)
        # target is newer, so don't upload
        host = _test_base.ftp_host_factory(
               ftp_host_class=FailingUploadAndDownloadFTPHost)
        flag = host.upload_if_newer(local_source, '/home/newer')
        self.assertEqual(flag, False)
        # target is older, so upload
        host = _test_base.ftp_host_factory()
        flag = host.upload_if_newer(local_source, '/home/older')
        self.assertEqual(flag, True)
        # check uploaded content
        # the data which was uploaded has its line endings converted
        #  so the conversion must also be applied to 'data'
        data = data.replace('\n', '\r\n')
        remote_file_content = _mock_ftplib.content_of('older')
        self.assertEqual(data, remote_file_content)
        # target doesn't exist, so upload
        host = _test_base.ftp_host_factory()
        flag = host.upload_if_newer(local_source, '/home/notthere')
        self.assertEqual(flag, True)
        remote_file_content = _mock_ftplib.content_of('notthere')
        self.assertEqual(data, remote_file_content)
        # clean up
        os.unlink(local_source)

    def compare_and_delete_downloaded_data(self, filename):
        """Compare content of downloaded file with its source, then
        delete the local target file."""
        data = open(filename, 'rb').read()
        remote_file_content = _mock_ftplib.content_of('newer')
        self.assertEqual(data, remote_file_content)
        # clean up
        os.unlink(filename)

    def test_conditional_download_without_target(self):
        "Test conditional binary mode download when no target file exists."
        local_target = '__test_target'
        # target does not exist, so download
        host = _test_base.ftp_host_factory(
               session_factory=BinaryDownloadMockSession)
        flag = host.download_if_newer('/home/newer', local_target, 'b')
        self.assertEqual(flag, True)
        self.compare_and_delete_downloaded_data(local_target)

    def test_conditional_download_with_older_target(self):
        """Test conditional binary mode download with newer source file."""
        local_target = '__test_target'
        # make target file
        open(local_target, 'w').close()
        # source is newer, so download
        host = _test_base.ftp_host_factory(
               session_factory=BinaryDownloadMockSession)
        flag = host.download_if_newer('/home/newer', local_target, 'b')
        self.assertEqual(flag, True)
        self.compare_and_delete_downloaded_data(local_target)

    def test_conditional_download_with_newer_target(self):
        """Test conditional binary mode download with older source file."""
        local_target = '__test_target'
        # make target file
        open(local_target, 'w').close()
        # source is older, so don't download
        host = _test_base.ftp_host_factory(
               session_factory=BinaryDownloadMockSession)
        host = _test_base.ftp_host_factory(
               ftp_host_class=FailingUploadAndDownloadFTPHost,
               session_factory=BinaryDownloadMockSession)
        flag = host.download_if_newer('/home/older', local_target, 'b')
        self.assertEqual(flag, False)
        # remove target file
        os.unlink(local_target)


class TestTimeShift(unittest.TestCase):
    def test_rounded_time_shift(self):
        """Test if time shift is rounded correctly."""
        host = _test_base.ftp_host_factory(session_factory=TimeShiftMockSession)
        # use private bound method
        rounded_time_shift = host._FTPHost__rounded_time_shift
        # original value, expected result
        test_data = [
          (0, 0), (0.1, 0), (-0.1, 0), (1500, 0), (-1500, 0),
          (1800, 3600), (-1800, -3600), (2000, 3600), (-2000, -3600),
          (5*3600-100, 5*3600), (-5*3600+100, -5*3600)]
        for time_shift, expected_time_shift in test_data:
            calculated_time_shift = rounded_time_shift(time_shift)
            self.assertEqual(calculated_time_shift, expected_time_shift)

    def test_assert_valid_time_shift(self):
        """Test time shift sanity checks."""
        host = _test_base.ftp_host_factory(session_factory=TimeShiftMockSession)
        # use private bound method
        assert_time_shift = host._FTPHost__assert_valid_time_shift
        # valid time shifts
        test_data = [23*3600, -23*3600, 3600+30, -3600+30]
        for time_shift in test_data:
            self.failUnless(assert_time_shift(time_shift) is None)
        # invalid time shift (exceeds one day)
        self.assertRaises(ftp_error.TimeShiftError, assert_time_shift, 25*3600)
        self.assertRaises(ftp_error.TimeShiftError, assert_time_shift, -25*3600)
        # invalid time shift (deviation from full hours unacceptable)
        self.assertRaises(ftp_error.TimeShiftError, assert_time_shift, 10*60)
        self.assertRaises(ftp_error.TimeShiftError, assert_time_shift,
                          -3600-10*60)

    def test_synchronize_times(self):
        """Test time synchronization with server."""
        host = _test_base.ftp_host_factory(ftp_host_class=TimeShiftFTPHost,
               session_factory=TimeShiftMockSession)
        # valid time shift
        host.path.set_mtime(time.time() + 3630)
        host.synchronize_times()
        self.assertEqual(host.time_shift(), 3600)
        # invalid time shift
        host.path.set_mtime(time.time() + 3600+10*60)
        self.assertRaises(ftp_error.TimeShiftError, host.synchronize_times)


if __name__ == '__main__':
    unittest.main()


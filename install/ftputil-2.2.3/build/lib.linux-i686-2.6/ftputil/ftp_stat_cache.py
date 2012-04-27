# Copyright (C) 2006, Stefan Schwarzer
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

# $Id: ftp_stat_cache.py 636 2006-11-22 22:38:16Z schwa $

"""
ftp_stat_cache.py - cache for (l)stat data
"""

import time

import lrucache


class CacheMissError(Exception):
    pass


class StatCache(object):
    """
    Implement an LRU (least-recently-used) cache.

    `StatCache` objects have an attribute `max_age`. After this
    duration after _setting_ it a cache entry will expire. For
    example, if you code

    my_cache = StatCache()
    my_cache.max_age = 10
    my_cache["/home"] = ...

    the value my_cache["/home"] can be retrieved for 10 seconds. After
    that, the entry will be treated as if it had never been in the
    cache and should be fetched again from the remote host.

    Note that the `__len__` method does no age tests and thus may
    include some or many already expired entries.
    """
    # default number of cache entries
    _DEFAULT_CACHE_SIZE = 1000

    def __init__(self):
        # can be reset with method `resize`
        self._cache = lrucache.LRUCache(self._DEFAULT_CACHE_SIZE)
        # never expire
        self.max_age = None
        self.enable()

    def enable(self):
        """Enable storage of stat results."""
        self._enabled = True

    def disable(self):
        """
        Disable the cache. Further storage attempts with `__setitem__`
        won't have any visible effect.

        Disabling the cache only effects new storage attempts. Values
        stored before calling `disable` can still be retrieved unless
        disturbed by a `resize` command or normal cache expiration.
        """
        self._enabled = False

    def resize(self, new_size):
        """
        Set number of cache entries to the integer `new_size`.
        If the new size is greater than the current cache size,
        relatively long-unused elements will be removed.
        """
        self._cache.size = new_size

    def _age(self, path):
        """
        Return the age of a cache entry for `path` in seconds. If
        the path isn't in the cache, raise a `CacheMissError`.
        """
        try:
            return time.time() - self._cache.mtime(path)
        except lrucache.CacheKeyError:
            raise CacheMissError("no entry for path %s in cache" % path)

    def clear(self):
        """Clear (invalidate) all cache entries."""
        old_size = self._cache.size
        try:
            # implicitly clear the cache by setting the size to zero
            self.resize(0)
        finally:
            self.resize(old_size)

    def invalidate(self, path):
        """
        Invalidate the cache entry for the absolute `path` if present.
        After that, the stat result data for `path` can no longer be
        retrieved, as if it had never been stored.

        If no stat result for `path` is in the cache, do _not_
        raise an exception.
        """
        #XXX to be 100 % sure, this should be `host.sep`
        assert path.startswith("/"), "%s must be an absolute path" % path
        try:
            del self._cache[path]
        except lrucache.CacheKeyError:
            pass

    def __getitem__(self, path):
        """
        Return the stat entry for the `path`. If there's no stored
        stat entry or the cache is disabled, raise `CacheMissError`.
        """
        if not self._enabled:
            raise CacheMissError("cache is disabled")
        # possibly raise a `CacheMissError` in `_age`
        if (self.max_age is not None) and (self._age(path) > self.max_age):
            self.invalidate(path)
            raise CacheMissError("entry for path %s has expired" % path)
        else:
            #XXX I don't know if this may raise a `CacheMissError` in
            #  case of race conditions; I'll prefer robust code
            try:
                return self._cache[path]
            except lrucache.CacheKeyError:
                raise CacheMissError("entry for path %s not found" % path)

    def __setitem__(self, path, stat_result):
        """
        Put the stat data for `path` into the cache, unless it's
        disabled.
        """
        if not self._enabled:
            return
        self._cache[path] = stat_result

    def __contains__(self, path):
        """
        Support for the `in` operator. Return a true value, if data
        for `path` is in the cache, else return a false value.
        """
        try:
            # implicitly do an age test which may raise `CacheMissError`;
            #  deliberately ignore the return value `stat_result`
            stat_result = self[path]
            return True
        except CacheMissError:
            return False

    #
    # the following methods are only intended for debugging!
    #
    def __len__(self):
        """
        Return the number of entries in the cache. Note that this
        may include some (or many) expired entries.
        """
        return len(self._cache)

    def __str__(self):
        """Return a string representation of the cache contents."""
        lines = []
        for key in sorted(self._cache):
            lines.append("%s: %s" % (key, self[key]))
        return "\n".join(lines)


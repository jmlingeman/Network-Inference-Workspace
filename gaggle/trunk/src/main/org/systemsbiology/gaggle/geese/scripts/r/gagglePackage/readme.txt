Gaggle R Goose
==============

An R package that enables data exchange between the R environment and the Gaggle framework.


Known bugs
----------

On Snow Leopard, I got the following error:

Error in untar2(tarfile, files, list, exdir) : unsupported entry type ‘x’

The 'x' refers to extended non-standard block types. R uses its own internal
tar to decompress packages, which is the source of the error. The non-standard
block types are not handled by that code. Maybe those are some Apple specific
thing?

One fix is the following:

sudo export R_INSTALL_TAR=/usr/bin/tar; R --no-init-file CMD INSTALL *.gz


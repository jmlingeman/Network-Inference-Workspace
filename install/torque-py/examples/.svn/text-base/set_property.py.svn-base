#!/usr/bin/env python
#
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 17 Aug 2001
# Desc. : Set a node property
#
# CVS info:
# $Id$
#
#
#
import sys
import pbs

def main():
  pbs_server = pbs.pbs_default()
  if not pbs_server:
    print 'No default server'
    sys.exit(1)

  if len(sys.argv) < 2:
  	print "Usage: set_property.py <hostname>"
	sys.exit(1)

  hostname = sys.argv[1]

  con = pbs.pbs_connect(pbs_server)

  attrop_l = pbs.new_attropl(1)
  attrop_l[0].name  = 'note'
  attrop_l[0].value = 'set_something_useful'
  attrop_l[0].op    = pbs.SET

  r =  pbs.pbs_manager(con, pbs.MGR_CMD_SET, pbs.MGR_OBJ_NODE, 
                    hostname, attrop_l, 'NULL')

  if r > 0:
    print r, ";"
    errno, text = pbs.error() 
    print errno, text

main()

#!/usr/bin/env python
#
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 17 Aug 2001 
# Desc. : Simple pbsnodes -a
#
# CVS info:
# $Id: pbsnodes-a.py 70 2005-01-28 10:19:04Z bas $
#
#
#


import pbs
import sys

def main():

  pbs_server = pbs.pbs_default()
  if not pbs_server:
    print "No default pbs server"
    sys.exit(1)

  con = pbs.pbs_connect(pbs_server)
  nodes = pbs.pbs_statnode(con, "", "NULL", "NULL")


  for node in nodes:
    print node.name
    for attrib in node.attribs:
      print '\t', attrib.name, '=', attrib.value

main()

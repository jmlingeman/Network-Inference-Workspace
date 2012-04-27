#!/usr/bin/env python
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 17 Aug 2001 
# Desc. : print version of pbs_server
#    
# CVS info:
# $Id: pbs_version.py 37 2003-07-11 11:40:34Z bas $
#
#
import sys
import pbs

def main():

  pbs_server = pbs.pbs_default()
  if not pbs_server:
    print 'No default server'
    sys.exit(1)

  con = pbs.pbs_connect(pbs_server)

  attr_l = pbs.new_attrl(1)
  attr_l[0].name = 'pbs_version'

  server_info = pbs.pbs_statserver(con, attr_l, 'NULL')
  for entry in server_info:
    print entry.name
    for attrib in entry.attribs:
       print '\t', attrib.name, ' = ', attrib.value

main()

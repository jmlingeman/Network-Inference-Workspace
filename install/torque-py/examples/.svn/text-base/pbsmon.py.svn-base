#!/usr/bin/env python
#
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 17 Aug 2001
# Desc. : This script displays the status of the PBS batch.
#         It display the status of a node. The ideas are
#         based on the awk-script of Willem Vermin
#
# CVS info:
# $Id$
#
import sys
import string
import re

import pbs

pbs_ND_free_and_job = 'not_all_procs_used'

translate_state = {
    pbs.ND_free             : '_',
    pbs.ND_down             : 'X',
    pbs.ND_offline          : '.',
    pbs.ND_reserve          : 'R',
    pbs.ND_job_exclusive    : 'J',
    pbs.ND_job_sharing      : 'S',
    pbs.ND_busy             : '*',
    pbs.ND_state_unknown    : '?',   
    pbs.ND_timeshared       : 'T',
    pbs.ND_cluster          : 'C',
    pbs_ND_free_and_job     : 'j'
}



def display_cluster_status(nl, sl):

  # Thanks to Daniel Olson, we have now code that can handle
  # 2 and 3 digit hostname numbers
  #
  if len(nl) == 1: 
    width = len( nl[0] )
  else:
    width = len( nl[-1] )

  # Determine what format we have to use
  #
  if width == 3:
    step = end = 19
    format = '%3s'
  else:
    step  = end = 25
    format = '%2s'

  start = 0 

  items = len(nl)

  # Sanity check 
  if end > items:
    end = items

  while start < items:

    print ' ',
    for j in range(start,end):
      print format %(nl[j]) ,

    print '\n ',
    for j in range(start,end):
      print format %(sl[j]) ,
   
    print '\n'

    start = end
    end = end + step 

    if end > items:
      end = items
   
  # Now some statistics
  #
  n = 0
  for key in translate_state.keys():
    value = translate_state[key]
    print "%3s %-21s : %d\t |" %( value, key, sl.count(value) ),
    if n%2:
      print ''
    n = n + 1

def main():
  state_list = []
  node_list  = []
  node_nr    = 0

  if len(sys.argv) > 1:
    pbs_server = sys.argv[1]
  else:
    pbs_server = pbs.pbs_default()
    if not pbs_server:
      print "No default pbs server, usage: pbsmon [server] "
      sys.exit(1)

  con = pbs.pbs_connect(pbs_server)
  if con < 0:
     errno, text = pbs.error()
     print errno, text
     sys.exit(1)

  # We are only interested in the state and jobs of a node
  #
  attrl = pbs.new_attrl(2);
  attrl[0].name='state'
  attrl[1].name = 'jobs'


  nodes = pbs.pbs_statnode(con, "", attrl, "NULL")

  # Some is het None dan weer NULL, beats me
  #
  for node in nodes:

    # display_node_status(batch_info)
    node_attr = node.attribs

    # A node can have serveral states, huh. We are only
    # interested in first entry.
    #
    temp = string.splitfields(node_attr[0].value, ',')
    state = temp[0]

    # look if on a free node a job is scheduled, then mark it
    # as other state
    #
    if state == pbs.ND_free:
       if len(node_attr) > 1:
          state_list.append(translate_state[pbs_ND_free_and_job])
       else:
          state_list.append(translate_state[state])
    else:
          state_list.append(translate_state[state])
		
		
    re_host = re.compile(r"""

      (?P<name>\d+)

      """, re.VERBOSE)

    result = re_host.search(node.name)
    if result:
      node_list.append( result.group('name') )
    else:
      node_nr = node_nr + 1
      node_list.append( str(node_nr) )

  display_cluster_status(node_list, state_list)

main()

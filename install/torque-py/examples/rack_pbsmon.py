#! /usr/bin/env python
#
#	pbsmon	WJ104
#
#	Hint: set ts=4
#

import os
import sys
import string

import pbs


NODES_PER_RACK = 19
N_RACKS = 15

pbs_ND_single = 'job (single)'


PBS_STATES = {
	pbs.ND_free				: '_',
	pbs.ND_down				: 'X',
	pbs.ND_offline			: '.',
	pbs.ND_reserve			: 'R',
	pbs.ND_job_exclusive	: 'J',
	pbs.ND_job_sharing		: 'S',
	pbs.ND_busy				: '*',
	pbs.ND_state_unknown	: '?',
	pbs.ND_timeshared		: 'T',
	pbs.ND_cluster			: 'C',
	pbs_ND_single			: 'j'
}



def pbsmon():
	global NODES_PER_RACK, N_RACKS, PBS_STATES

	if len(sys.argv) > 1:
		pbs_server = sys.argv[1]
	else:
		pbs_server = pbs.pbs_default()

	if not pbs_server:
		print 'No default pbs server, usage: %s [server]' % os.path.basename(sys.argv[0])
		sys.exit(1)

	con = pbs.pbs_connect(pbs_server)
	if con < 0:
		errno, text = pbs.error()
		print errno, text
		sys.exit(1)

# get the state of the nodes
	attrl = pbs.new_attrl(2)
	attrl[0].name = 'state'
	attrl[1].name = 'jobs'
	nodes = pbs.pbs_statnode(con, '', attrl, 'NULL')

	node_dict = {}

	count_states = {}
	for key in PBS_STATES.keys():
		count_states[key] = 0

	for node in nodes:
		node_attr = node.attribs
		temp = string.split(node_attr[0].value, ',')
		state = temp[0]
		state_char = PBS_STATES[state]
		count_states[state] = count_states[state] + 1

		if state == pbs.ND_free:
			if len(node_attr) > 1:
#				print 'TD: %s' % node.name, node_attr[1]
				state_char = PBS_STATES[pbs_ND_single]
				count_states[pbs.ND_free] = count_states[pbs.ND_free] - 1
				count_states[pbs_ND_single] = count_states[pbs_ND_single] + 1

#		print 'TD: %s %s' % (node.name, state_char)
		node_dict[node.name] = state_char

	legend = PBS_STATES.keys()
	legend.sort()

# print nodes with gb-r%dn%d naming scheme
	print '  ',
	for rack in xrange(1, N_RACKS+1):
		print '%2d' % rack,
	print

	for node_nr in xrange(1, NODES_PER_RACK+1):
		print '%2d' % node_nr,

		for rack in xrange(1, N_RACKS+1):
			node_name = 'gb-r%dn%d' % (rack, node_nr)

			if node_dict.has_key(node_name):
				print ' %s' % node_dict[node_name],

				del node_dict[node_name]
			else:
				print '  ',

		if node_nr-1 < len(legend):
			state = legend[node_nr-1]
			print '  %s  %-13s : %d' % (PBS_STATES[state], state, count_states[state])
		else:
			print

	print

# any other nodes?
	arr = node_dict.keys()
	if arr:
		arr.sort()

		for node in arr:
			print '%s %s' % (node, node_dict[node])

		print

#	n = 0
#	for state in legend:
#		print '%s  %-13s : %-3d     ' % (PBS_STATES[state], state, count_states[state]),
#		n = n + 1
#		if n > 1:
#			n = 0
#			print


if __name__ == '__main__':
	pbsmon()


# EOB


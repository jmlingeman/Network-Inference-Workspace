#!/usr/bin/env python
#
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 15 Apr 2009
# Desc. : Usage of the High Availabilty functions in torque
#
# SVN info:
# $Id: new_interface.py 74 2005-03-04 09:11:10Z bas $
#
#
#
import sys

import pbs

def method1():
	pbs_server = pbs.pbs_default() 
	if not pbs_server:
		print "No default pbs server"
		sys.exit(1)

	con = pbs.pbs_connect(pbs_server)
	if con == -1:
		print "Default pbs server connection failed"
		pbs_server = pbs.pbs_fbserver()
		if not pbs_server:
			print "No pbs fallback server"
			sys.exit(1)
		else:
			con = pbs.pbs_connect(pbs_server)
			if con == -1:
				print "pbs fallback server connection failed"
				sys.exit(1)

	print "Connected to %s" %(pbs_server)

def method2():
	try:
		server_list = pbs.pbs_get_server_list().split(',')
	except AttributeError, detail:
		print 'The installed torque version does not support pbs_get_server_list function'
		sys.exit(1)
	for server in server_list:
		pbs_server = server
		con = pbs.pbs_connect(server)
		if con != -1:
			break

	if con == -1:
		print 'Could not connect to a server (%s)' %('.'.join(server_list))
		sys.exit(1)

	print "Connected to %s" %(pbs_server)


method1()
method2()



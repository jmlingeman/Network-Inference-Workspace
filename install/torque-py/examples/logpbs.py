#!/usr/bin/env python
#
# Author: Bas van der Vlies <basv@sara.nl>
# Date  : 21 Oct 2002
# Desc. : How to use the logging facility of Openpbs
#
# $Id: logpbs.py 110 2006-04-20 12:28:20Z bas $
#
import pbs
import sys

def main():
  
   # Open log in current directory with current date
   #
   if pbs.log_open('', '.'):
     print 'Could not open log file'
     sys.exit(1)

   pbs.log_record(pbs.PBSEVENT_ERROR, pbs.PBS_EVENTCLASS_REQUEST, 'test1', 'this a test message')

   # Give a verbose message of closing the file
   #
   pbs.log_close(1)

   # Open the sched log file
   #
   if pbs.log_open('', '/var/spool/torque/sched_logs'):
     print 'Could not open log file in /var/spool/torque/sched_logs'
     sys.exit(1)

   pbs.log_record(pbs.PBSEVENT_JOB, pbs.PBS_EVENTCLASS_JOB, 'test1', 'this a test message')

   # Give a NON verbose message of closing the file
   #
   pbs.log_close(0)

main()

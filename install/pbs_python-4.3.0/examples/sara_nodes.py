#!/usr/bin/env python
#
# Author: Dennis Stam
# Date  : 14-05-2009
# Desc. : This program/module allows you to change
#         the state of a node to offline or down
#
# SVN Info:
#       $Id: sara_nodes 4552 2010-04-29 12:15:42Z dennis $
#       $URL: https://subtrac.sara.nl/hpcv/svn/beowulf/trunk/torque/utils/sara_nodes $
#

try:
    # import from the sara_python_modules
    import AdvancedParser
except ImportError:
    from sara import AdvancedParser

# imports of the pbs_python module
import PBSQuery
import pbs

# python core modules
from optparse import make_option
import types
import sys
import re
import os
import time
import string

# GINA also uses python2.3
if sys.version_info < ( 2,4 ):
    from set import Set as set

__author__ = 'Dennis Stam'

# Specify here your BATCH name pattern, this is
# used for sorting when you are using basenames
RE_PATTERN = '(r\d+n\d+)'

# Cfegine uses : for lists, so a override is needed
TIME_SEPARATOR = ':'

class sara_nodesException( Exception ):

        def __init__(self, msg='' ):
                self.msg = msg
                Exception.__init__( self, msg )

        def __repr__(self):
                return self.msg

        def islist(self):
                if type(self.msg) is types.ListType:
                        return True

                return False

        def getlist(self):
                return self.msg

class sara_nodesCli:
        '''
        This class is the Command Line Interface from here we call the sara_nodes class / module
        '''
        option_list = [
                make_option( '-v', '--verbose', dest='verbose', action='store_true', help='enables verbose mode' ),
                make_option( '-n', '--dry-run', dest='dryrun', action='store_true', help='enables dry-run mode' ),
                make_option( '-q', '--quiet', dest='quiet', action='store_true', help='enable this function to supress all feedback'),
                make_option( '-o', '--offline', dest='offline', help='change state to offline', metavar='NOTE' ),
                make_option( '-c', '--clear', dest='clear', action='store_true', help='change state to down' ),
                make_option( '-N', '--clearnote', dest='note', action='store_true', help='clear note of node' ),
                make_option( '-m', '--modify', dest='modify', help='use this option the modify the note, it will replace the current one!', metavar='NOTE' ),
                make_option( '-f', '--format', dest='format', nargs=2, help='specify how to display the information, also sets quiet to True' ),
                make_option( '-t', '--ticket', dest='ticket', help='add/change/remove ticket number, removing use -t c' ),
        ]

        def __init__(self):
                '''
  sara_nodes [ <options> <nodenames> | [nodenames] ]

  -f/--format needs 2 arguments, first the pattern secondly the variables
	the pattern you specify must be the string format pattern of Python
	fields: node, state, date_add, date_edit, user, ticket, remark'''

                self.obj_sara_nodes = sara_nodes()
                self.parser = AdvancedParser.AdvancedParser( 
                option_list=self.option_list, 
				version=pbs.version,
                usage=self.__init__.__doc__
                )

                self.parser.set_default('verbose', False)
                self.parser.set_default('dryrun', False)
                self.parser.set_default('offline', False)
                self.parser.set_default('clear', False)
                self.parser.set_default('note', False)
                self.parser.set_default('quiet', False)
                self.parser.set_default('ticket', None)
                self.parser.set_default('modify', False)
                self.parser.set_default('format', 'default' )

                options, args = self.parser.parse_args()

                if options.format == 'default':
                    options.format = (' %-10s | %-19s | %-11s, %-11s %-6s %-5s: %s','node,state,date_add,date_edit,user,ticket,remark' )
                else:
                    options.quiet = True

                if not options.quiet:
                        self.obj_sara_nodes.dryrun = options.dryrun

                        if options.dryrun or options.verbose:
                                self.obj_sara_nodes.verbose = True

                if str( options.offline ).rstrip() == '' or str( options.modify ).rstrip() == '':
                        sys.stderr.write( 'sara_nodes: error: option requires an argument\n' )

                        sys.exit( 1 )
                
                try:
                        if options.offline and not options.clear:
                                if args:
                                        self.obj_sara_nodes.pbs_change_state_offline( args, options.offline, options.ticket )
                                else:
                                        raise sara_nodesException, 'No hostnames given'
                        elif options.clear and not options.offline:
                                if args:
                                        self.obj_sara_nodes.pbs_change_state_down( args )
                                else:
                                        raise sara_nodesException, 'No hostnames given'
                        elif options.note:
                                if args:
                                        self.obj_sara_nodes.pbs_change_note_clear( args )
                                else:
                                        raise sara_nodesException, 'No hostnames given'
                        elif options.modify:
                                if args:
                                        self.obj_sara_nodes.pbs_change_note( args, options.modify, options.ticket )
                                else:
                                        raise sara_nodesException, 'No hostnames given'
                        elif options.ticket:
                                if args:
                                        self.obj_sara_nodes.pbs_change_note_ticket( args, options.ticket )
                                else:
                                        raise sara_nodesException, 'No hostnames given'
                        else:
                                if not options.quiet:
                                        print '\n Use option --help for help'
                                self.print_list(args, options)

                except sara_nodesException, msg:
                        if msg.islist():
                        	for item in msg.getlist():
                                	sys.stderr.write( 'sara_nodes: error: %s\n' % item )
			else:
                        	sys.stderr.write( 'sara_nodes: error: %s\n' % str( msg ) )

                        sys.exit( 1 )

        def return_note( self, pre_parts ):

                if type(pre_parts) is types.ListType and len( pre_parts) >= 5:
                        return { 
				'date_add': pre_parts[0].strip(), 
				'date_edit': pre_parts[1].strip(), 
				'user': pre_parts[2].strip(), 
				'ticket': pre_parts[3].strip(),
				'remark': ','.join( pre_parts[4:] )
			}
                else:
                        return { 
				'date_add': '', 
				'date_edit': '', 
				'user': '', 
				'ticket': '',
				'remark': str( pre_parts )
			}

        def convert_format( self, format_options ):
            pattern = r'\%([-|+]){0,1}([0-9]{0,2})([a-z]{1})'
            parts = re.findall( pattern, format_options[0], re.VERBOSE )
            line = re.sub( pattern, '%s', format_options[0], re.VERBOSE )

            rlist = list()
            counter = 0
            for variable in format_options[1].split( ',' ):
                rlist.append( '%s(%s)%s%s%s' % (
                    '%',
                    variable, 
                    parts[ counter ][0], 
                    parts[ counter ][1],
                    parts[ counter ][2],
                ) )
                counter += 1
            return line % tuple( rlist )

        def print_table( self, node_list, pbs_nodes, format_options ):
            '''
            This method prints the rows of a table
            '''

            try:
                line_format = self.convert_format( format_options )
                for node in node_list:
                    note = '' 
                    if pbs_nodes[ node ].has_key('note'):
                        note = pbs_nodes[ node ]['note']
                                
                    if self.allowed_state( pbs_nodes[ node ]['state'] ) or note:
                        node_note = self.return_note( note )

                        fields = { 
                            'node': node,
                            'state': ', '.join( pbs_nodes[ node ]['state'] ),
                            'date_add': node_note['date_add'],
                            'date_edit': node_note['date_edit'],
                            'user': node_note['user'],
                            'ticket': node_note['ticket'],
                            'remark': node_note['remark'],
                        }

                        
                        print line_format % fields
            except KeyError, e:
                raise sara_nodesException, 'Given host does not exist'

        def print_list(self, args, options):
                '''
                A method that is used for collecting all nodes with the state down, offline or unknown
                '''

                header = ' %-10s | %-19s | %s' % ( 'Nodename', 'State', 'Note' )
		if not options.quiet:
                	print '\n%s\n%s' % ( header, ( '-' * 80 ) )
                
                p = PBSQuery.PBSQuery()
                if pbs.version_info >= ( 4,0,0 ):
                        if self.obj_sara_nodes.verbose:
                                print "Enabling new_data_structure for PBSQuery"
                        p.new_data_structure()

                nodes = p.getnodes( ['state', 'note'] )

                if args:
                        args = self.sort_nodes( args )
                        self.print_table( args[0], nodes, options.format )
                else:
                        sorted_nodes, sorted_other = self.sort_nodes( nodes )

                        self.print_table( sorted_other, nodes, options.format )
                        self.print_table( sorted_nodes, nodes, options.format )

        def real_sort( self, inlist ):
                '''
                Use this method instead of the x.sort(), because with x.sort()
                numeric values in a string are not correctly sorted!
                '''
                indices = map(self._generate_index, inlist )
                decorated = zip( indices, inlist )
                decorated.sort()

                return [ item for index, item in decorated ]

        def _generate_index( self, str ):
                '''
                Spliting a string in aplha and numeric elements
                '''

                index = []

                def _append( fragment, alist=index ):
                        if fragment.isdigit():
                                fragment = int( fragment )
                        alist.append( fragment )

                prev_isdigit = str[0].isdigit()
                current_fragment = ''

                for char in str:
                        curr_isdigit = char.isdigit()

                        if curr_isdigit == prev_isdigit:
                                current_fragment += char
                        else:
                                _append( current_fragment )
                                current_fragment = char
                                prev_isdigit = curr_isdigit

                _append( current_fragment )

                return tuple( index )

        def sort_nodes(self, nodes):
                '''
                Sorts the nodes list and returns two lists
                the first the nodes secondly the other machines

                When RE_PATTERN is not supplied then all names
                will be sorted the same way.
                '''

                if not globals().has_key('RE_PATTERN'):
                        global RE_PATTERN 
                        RE_PATTERN = ''

                pattern = re.compile( RE_PATTERN, re.VERBOSE )

                tmplist = list()
                tmplist_other = list()

                for node in nodes:
                        match = pattern.findall( node )

                        if match and len( match ) == 1:
                                tmplist.append( node )
                        else:
                                tmplist_other.append( node )

                tmplist = self.real_sort( tmplist )
                tmplist_other.sort()

                return tmplist, tmplist_other

        def allowed_state(self, state):
                '''
                This method checks is a node complies with the following states: 
                down, offline and or unknown
                '''
                allowed_list = set( ['down', 'offline', 'unknown'] )

                return bool( allowed_list.intersection( set( state ) ) )

class sara_nodes:

        def __init__(self):
                '''
                Just initialize two optional variables
                '''
                self.dryrun = False
                self.verbose = False

        def note_check_ticket( self, ticketno, oldticket ):
                
                if ticketno:
                        try:
                            return '#%d' % int( ticketno )
                        except ValueError:
                                if ticketno == 'c':
                                        return ''

                return oldticket

        def note_return_username( self, old_username ):
		try:
	                username = os.getlogin()

        	        if username != 'root':
                	        return username
	                else:
        	                return old_username
		except OSError, err:
			return 'root'

        def note_create( self, new_note, mode = 'a', old_note = None ):
                if mode == 'w':
                        return new_note
                else:
                        if old_note and old_note.find( new_note ) < 0:
                                return '%s, %s' % ( old_note, new_note )
                        else:
                                return new_note

        def create_date( self ):
                if not globals().has_key('TIME_SEPARATOR'):
                        global TIME_SEPARATOR
                        TIME_SEPARATOR = ':'

                curtime = time.localtime()
                day = time.strftime( '%d-%m', curtime )
                hour = time.strftime( '%H', curtime )
                minutes = time.strftime( '%M', curtime )
                return '%s %s%s%s' % ( day, hour, TIME_SEPARATOR, minutes )

        def note_init( self ):
                current_date = self.create_date()
		try:
	                current_username = os.getlogin()
		except OSError, err:
			current_username = 'root'

                return [ current_date, current_date, current_username, '' ]

        def note( self, node, note_attr ):
                '''
                This method combines all note methods and returns the new note
                '''
                p = PBSQuery.PBSQuery()
                p.new_data_structure()
                pbs_info = p.getnode( node )
                
                pre_parts = list()
                old_note = None
                new_note = None

                if pbs_info.has_key( 'note' ):
                        pbs_note = pbs_info[ 'note' ]
                        if len( pbs_note ) > 4:
                                pre_parts = pbs_note[:4]
                                old_note = ', '.join( pbs_note[4:] )

                                pre_parts[1] = self.create_date()
                                pre_parts[2] = self.note_return_username( pre_parts[2] )

                else:
                        pre_parts = self.note_init()

		if note_attr.has_key( 'ticket' ):
                	pre_parts[3] = self.note_check_ticket( note_attr['ticket'], pre_parts[3] )

                if note_attr.has_key( 'note' ) and note_attr.has_key( 'mode' ):
                        if note_attr[ 'note' ] and note_attr[ 'mode' ] in [ 'a','w' ]:
                                if old_note:
                                        new_note = self.note_create( note_attr[ 'note' ], note_attr[ 'mode' ], old_note )
                                else:
                                        new_note = self.note_create( note_attr[ 'note' ], note_attr[ 'mode' ] )
                        else:
                                new_note = old_note

                return '%s,%s' % ( ','.join( pre_parts ), new_note )

        def verbose_print( self, msg ):
                if self.verbose:
                        print msg

        def pbs_change_note_clear( self, nodes ):
                attributes = pbs.new_attropl(1)
                attributes[0].name = pbs.ATTR_NODE_note
                attributes[0].value = ''
                attributes[0].op = pbs.SET

                self.verbose_print( '%*s: cleared' % ( 7, 'Note') )
                self.pbs_batch( nodes, attributes )

        def pbs_change_note_ticket( self, nodes, ticket ):
                note_attributes = { 'note': None, 'ticket': ticket, 'mode': 'a' }
                self.verbose_print( '%*s: %s' % ( 7, 'Ticket', ticket  ) )
                self.pbs_batch( nodes, None, note_attributes)

        def pbs_change_note( self, nodes, note, ticket=None ):
                note_attributes = { 'note': note, 'ticket': ticket, 'mode': 'w' }

                self.verbose_print( '%*s: %s' % ( 7, 'Note', note ) )
                if ticket:
                        self.verbose_print( '%*s: %s' % ( 7, 'Ticket', ticket ) )
                self.pbs_batch( nodes, None, note_attributes)

        def pbs_change_state_offline( self, nodes, note, ticket=None ):
                attributes = pbs.new_attropl(1)
                attributes[0].name = pbs.ATTR_NODE_state
                attributes[0].value = 'offline'
                attributes[0].op = pbs.SET

                note_attributes = { 'note': note, 'ticket': ticket, 'mode': 'a' }

                self.verbose_print( '%*s: offline' % ( 7, 'State') )
                self.verbose_print( '%*s: %s' % ( 7, 'Note', note ) )
                if ticket:
                        self.verbose_print( '%*s: %s' % ( 7, 'Ticket', ticket ) )
                self.pbs_batch( nodes, attributes, note_attributes )

        def pbs_change_state_down( self, nodes ):
                attributes = pbs.new_attropl(2)
                attributes[0].name = pbs.ATTR_NODE_state
                attributes[0].value = 'down'
                attributes[0].op = pbs.SET

                attributes[1].name = 'note'
                attributes[1].value = ''

                self.verbose_print( '%*s: down' % ( 7, 'State') )
                self.verbose_print( '%*s: cleared' % ( 7, 'Note' ) )
                self.pbs_batch( nodes, attributes )

        def pbs_batch( self, nodes, attrs=None, note_attributes=None ):
                nodeserror = list()
                if not attrs and not note_attributes:
                        raise sara_nodesException, 'attrs and note_attributes can not be empty together!'

                if not self.dryrun:
                        if note_attributes and len( note_attributes ) == 3:
                                if attrs:
                                        attributes = attrs + pbs.new_attropl(1)
                                        attributes[1].name = pbs.ATTR_NODE_note
                                        attributes[1].op = pbs.SET
                                else:
                                        attributes = pbs.new_attropl(1)
                                        attributes[0].name = pbs.ATTR_NODE_note
                                        attributes[0].op = pbs.SET
                        else:
                                attributes = attrs
                        # Some hacking here because some limitation in the Torque 2.4 version
                        # fetching note data first for all nodes!
                        tmp_node_note = dict()

                        for node in nodes:
                                if note_attributes and len( note_attributes ) == 3:
	                                    tmp_node_note[ node ] = self.note( node, note_attributes )

                        pbs_server = pbs.pbs_default()

                        if not pbs_server:
                                raise sara_nodesException, 'Default pbs server not found!'

                        pbs_connection = pbs.pbs_connect( pbs_server )
                        for node in nodes:
                                if note_attributes and len( note_attributes ) == 3:
                                        try:
                                                if attrs:
                                                        attributes[1].value = tmp_node_note[ node ]
                                                else:
                                                        attributes[0].value = tmp_node_note[ node ]
                                        except KeyError:
                                                pass
                                rcode = pbs.pbs_manager( pbs_connection, pbs.MGR_CMD_SET, pbs.MGR_OBJ_NODE, node, attributes, 'NULL' )
                                if rcode > 0:
                                        errno, text = pbs.error()
                                        nodeserror.append( '%s: %s (%s)' % ( node, text, errno ) )
                else:
                        p = PBSQuery.PBSQuery()
                        pbsnodes = p.getnodes().keys()

                        print '%*s:' % ( 7, 'Nodes' ),
			firstitem = True

                        for node in nodes:
                                if node in pbsnodes:
					if firstitem:
	                                        print '%s' % node
						firstitem = False
					else:
						print '%*s' % ( 17, node )
                                else:
                                        nodeserror.append( '%s: does not exist' % node )

                if len( nodeserror ) > 0:
                        raise sara_nodesException, nodeserror

if __name__ == '__main__':
        sara_nodesCli()

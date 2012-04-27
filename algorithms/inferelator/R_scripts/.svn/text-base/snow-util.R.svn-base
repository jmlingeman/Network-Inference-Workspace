
#library( snow )
#library( rpvm )
require( snow )
require( rpvm )


try( detach( snow.util ) ); rm( snow.util )

snow.util <- list(
                  
## NOTE THIS ONLY WORKS ON LINUX WORKSTATIONS/NODES!!!
get.bad.good.nodes = function( nodes=snow.nodes, ping.count=1, max.load=2.1, min.mem=0 ) {
  out <- character()
  okay <- character()
  min.mem <- min.mem * 1024
  n.processors <- 1 # Get the # of processors
  for ( node in nodes ) { # See if node is responding
    if ( node %in% out || node %in% okay ) next
    cat( "Testing:", node, "...\n" )
    ##sig <- system( paste( "ping -c", ping.count, node ), intern=F )
    sig <- system( paste( "/usr/sbin/rpcinfo -p", node, "1>/dev/null 2>/dev/null" ), intern=F )
    if ( sig != 0 ) { # Not responding
      cat( "Node", node, "is NOT OKAY\n" )
      out <- append( out, node )
      next
    } else {
      if ( max.load > 0 ) { # Check the cpu load, skip if overloaded
        uname <- try( system( paste( rsh.cmd, node, "uname -a" ), intern=T ) ) # First see if its SMP
        if ( is.null( uname ) || class( uname ) == "try-error" ) {
          cat( "Node", node, "is NOT OKAY\n" )
          out <- append( out, node )
          next
        }
        ## USE 2 PROCS PER MACHINE?
        if ( length( grep( "SMP", uname ) > 0 ) ) n.processors <- 2
        
        ld <- try( as.numeric( strsplit( system( paste( rsh.cmd, node, "cat /proc/loadavg" ),
                                                intern=T ), " " )[[ 1 ]][ 1 ] ) )
        if ( class( ld ) == "try-error" ) {
          cat( "Node", node, "is NOT OKAY\n" )
          out <- append( out, node )
          next
        }
        if ( ld > max.load * n.processors ) {
          cat( "Node", node, "is OVERLOADED (", ld, ")\n" )
          out <- append( out, node )
          next
        } else if ( ld > max.load && n.processors > 1 ) n.processors <- 1
      }
      if ( min.mem > 0 ) { # Check the memory, skip if not enough
        mem.str <- try( system( paste( rsh.cmd, node, "cat /proc/meminfo" ), intern=T ) )
        if ( class( mem.str ) == "try-error" ) {
          cat( "Node", node, "doesnt have enough memory (", mem.free, ")\n" )
          out <- append( out, node )
          next
        }
        mem.tot <- as.numeric( strsplit( mem.str[ grep( "MemTotal", mem.str ) ], " +",
                                        extended=T, perl=T )[[ 1 ]][ 2 ] )
        mem.free <- as.numeric( strsplit( mem.str[ grep( "MemFree", mem.str ) ], " +",
                                         extended=T, perl=T )[[ 1 ]][ 2 ] )
        if ( mem.free < min.mem * n.processors ) n.processors <- 1
        if ( mem.free < min.mem ) {
          cat( "Node", node, "doesnt have enough memory (", mem.free, ")\n" )
          out <- append( out, node )
          next
        }
      }
    }
    cat( "Node", node, "is OKAY (", n.processors, "procs)\n" )
    for ( i in 1:n.processors ) okay <- append( okay, node )
  }
  cat( "Bad nodes:", out, "\n" )
  cat( "Good nodes:", okay, "\n" )
  list( bad=out, good=okay )
},

get.good.nodes = function( type="PVM" ) {
  if ( substring( Sys.getenv( "HOST" )[[ 1 ]], 1, 5 ) == "alice" ) {
    ##if ( type == "SOCK" ) {
    ##bad.snow.util$alice.nodes <- get.bad.nodes( snow.util$alice.nodes )
    ##out.nodes <- snow.util$alice.nodes[ ! ( snow.util$alice.nodes %in% bad.snow.util$alice.nodes ) ]
    out.nodes <- get.bad.good.nodes( snow.util$alice.nodes )$good
    return( out.nodes )
    ##} else if ( type == "PVM" ) return( snow.util$alice.nodes )
  }
  return( snow.nodes )
},

##snow.nodes <- get.good.nodes()

init.snow.cluster = function( type="PVM", nodes=snow.nodes, find.nodes=T ) {
  cat("Initializing", type, "SNOW cluster...\n")
  if ( find.nodes ) nodes <- get.good.nodes()
  ##nodes <- unique( nodes ) # Don't bother w/ multiple cpus -- seems to slow it down
  if ( length( nodes ) <= 1 ) return( NULL ) # Using one node -- don't use any snow stuff.
  cat( "Using", length( nodes ), "nodes:", nodes, "\n" )
  cl <- NULL
  tries <- 1
  while( tries < 3 && is.null( cl ) || class( cl ) == "try-error" ) {
    if ( type == "PVM" ) {
      start.PVM.nodes( nodes )
      if ( length( cl ) > 10 ) cl <- try( makePVMcluster( as.integer( length( nodes ) * 8 / 10 ) ) )
      else cl <- try( makePVMcluster( length( nodes ) ) )
    } else if ( type == "SOCK" ) {
      cl <- try( makeSOCKcluster( nodes ) )
    }
    if( is.null( cl ) || class( cl ) == "try-error" ) Sys.sleep( 5 )
    tries <- tries + 1
  }
  ## Initialize the cluster nodes so they're in the right wd, etc.
  ## This won't work if the cwd doesn't exist on the node disk
  ## But it will work if the "wd=" option in the pvmhosts file exists, and the
  ## initialize.R script is in that directory.
  cat( "Initializing cluster nodes...\n" )
  tmp <- unlist( try( clusterCall( cl, function( x ) {
    try( setwd( x ) )
    try( source("R_scripts/initialize.R" ) )
    initialize()
  }, getwd() ) ) )
  cat( "Got back:", tmp, "\n" )
  cat( "...done\n" )
  cl
},

start.PVM.nodes = function( nodes ) {
  ##if ( .PVM.mstats( nodes )[[ 1 ]] != "Unknown" ) try( .PVM.delhosts( nodes ) )
  nodes <- unique( nodes )
  try( .PVM.start.pvmd( block=F ) )
  if ( file.exists( "pvmhosts" ) ) {
    lines <- readLines( "pvmhosts" )
    for ( node in unique( nodes ) ) {
      if ( length( grep( node, lines ) ) > 0 ) {
        line <- lines[ grep( node, lines ) ]
        if ( substr( line, 1, 2 ) == "##" ) { # Skip the commented-out nodes and remove them from node list
          nodes <- nodes[ nodes != node ]
          next
        }
        cat( "Starting PVM on ", node, ":", line, "\n" )
        try( .PVM.addhosts( line ) )
      } else {
        cat( "No PVM parameters in pvmhosts file for node ", node, "\n" )
      }
    }
  } else {
    for ( node in unique( nodes ) ) {
      if ( .PVM.mstats( node )[[ 1 ]] == "Unknown" ) {
        cat( "Starting PVM on ", node, "\n" )
        try( .PVM.addhosts( node ) )
      }
    }
  }
},

kill.snow.cluster = function( cl, type="PVM" ) {
  if ( is.null( cl ) ) return()
  cat( "Killing SNOW cluster.\n" )
  try( stopCluster( cl ) )
  if ( type == "PVM" ) try( .PVM.delhosts( unique( snow.nodes ) ) )
},

kill.pvm = function() {
  try( .PVM.halt() ) # This seems to kill the current master R process, too
  for ( node in unique( snow.nodes ) )
    system( paste( rsh.cmd, node, "'rm -rvf /tmp/pvm* /tmp/Rtmp*'" ) )
},

snow.lapply = function( cl, l, FUN, ... ) {
  ll <- NULL
  if ( ! is.null( cl ) && length( cl ) > 1 ) {
    ll <- try( parLapply( cl, l, FUN, ... ) )
    while( class( ll ) == "try-error" || class( cl ) == "try-error" ) {
      if ( ! is.null( cl ) ) kill.snow.cluster( cl )
      cl <- init.snow.cluster()
      ll <- try( parLapply( cl, l, FUN, ... ) )
      Sys.sleep( 5 )
    }
  } else {
    ll <- lapply( l, FUN, ... )
  }
  ll
},

## F*cking snow cant send big objects. Screw it, lets just save 'em to a file
## and get the nodes to read them in.
snow.export = function( cl, names ) {
  tempf <- tempfile( "temp_export.", getwd() ) ## tempdir() isn't shared among nodes; use local dir.
  cat( "Saving objects:", names, "(temp file is", tempf, ").\n" )
  save( list=names, envir=.GlobalEnv, file=tempf, compress=T )
  cat( "Starting sending.\n" )
  clusterCall( cl, function( f ) { load( f, .GlobalEnv ); return( f ) }, tempf )
  cat( "Done sending.\n" )
  unlink( tempf )
}
)

init.snow <- function( snow.util ) {
  ## USE 2 PROCS PER MACHINE? (set the "1" to "2" in the following 2 lines:)
  
  ## Default on alice to run on nodes 22 to 67
  snow.util$alice.nodes <- rep( paste( "alice", 22:67, sep="" ), 2 )

  ## Temporary -- run nodes 9 - 21 for hpy
  ##snow.util$alice.nodes <- rep( c( "alice09", paste( "alice", 10:21, sep="" ) ), 1 )
  
  if ( file.exists( "pvmhosts" ) ) {
    lines <- readLines( "pvmhosts" )
    for ( node in unique( snow.util$alice.nodes ) ) {
      if ( length( grep( node, lines ) ) <= 0 ) next
      line <- lines[ grep( node, lines ) ]
      if ( substr( line, 1, 2 ) != "##" ) next # Skip the commented-out nodes and remove them from node list
      snow.util$alice.nodes <- snow.util$alice.nodes[ snow.util$alice.nodes != node ]
    }
    rm( lines, line, node )
  }

  snow.util$snow.nodes <- "localhost"
  if ( Sys.getenv( "HOST" )[[ 1 ]] == "crossroads.systemsbiology.net" ||
      Sys.getenv( "HOST" )[[ 1 ]] == "stimpy.systemsbiology.net" ||
      Sys.getenv( "HOST" )[[ 1 ]] == "ren.systemsbiology.net" ) {
    ##snow.util$snow.nodes <- snow.util$alice.nodes <- c( "localhost", "localhost" )
  } else if ( substring( Sys.getenv( "HOST" )[[ 1 ]], 1, 5 ) == "alice" ) {
    snow.util$snow.nodes <- snow.util$alice.nodes
  } else if ( Sys.getenv( "HOST" )[[ 1 ]] == "stimpy" ) {
    snow.util$snow.nodes <- rep( c( "stimpy", "stella", "ren" ), 2 )
  }

  snow.util$rsh.cmd <- "rsh -n"
  if ( substring( Sys.getenv( "HOST" )[[ 1 ]], 1, 5 ) != "alice" ) snow.util$rsh.cmd <- "ssh"

  invisible( snow.util )
}

attach( snow.util <<- init.snow( snow.util ) )

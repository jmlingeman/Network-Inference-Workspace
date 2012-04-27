
## March 16,2003
## Some utilities 


# map VNG to common name
"gid2gname" <- function ( gids ){
  gnames <- character(length=length(gids))
  for ( i in 1:length(gids) ) {
    gnames[i] <- gene.names[ which(gene.ids==gids[i])]
  }
  gnames
}

# map common name to VNG
"gname2gid" <- function (gnames){

  gids <- character(length=length(gnames))
  for ( i in 1:length(gnames) ) {
    gids[i] <- gene.ids[ which(gene.names == gnames[i])] 
  }
  gids

}


## find cluster to which gene belongs
## this version uses the common name

"gname2cluster" <- function( gname ){

  gid <- gname2gid(gname) 
  cname <- as.character( clusters.ic [ which ( names(clusters.ic) == gid ) ] )
  redNamesExp[cname]
  
}


## cluster members in common names
## outDated with clusterStack object

"clmemberscommon" <- function ( cname ){

   gids <- names( cl.members[[cname]] )
   gid2gname(gids)
   
}


################################################################################
pause <- function ()
{ 
    cat("Pause. Press <Enter> to continue...")
    readline()
    invisible()
}



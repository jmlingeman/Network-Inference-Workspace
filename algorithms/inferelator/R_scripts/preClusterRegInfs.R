
"pre.cluster.regInfs" <- function( tfs, ratios, cor.cut = 0.85) {

  ## join tfs
  num.tfs <- length( tfs )
  cat("starting with ", num.tfs, " tfs\n preparing to reduce to cor sets\n")

  still.in <- logical( length = num.tfs )
  names( still.in ) <- tfs
  still.in[ tfs ] <- TRUE

  tf.merge.cors <- list()
  
  tf.cor <- cor( t(ratios[tfs,]) )

  for ( i in 1:(num.tfs-1) ) {
    tf.merge.cors[[ names( still.in )[i] ]] <- names( still.in )[i]
    for ( j in (i+1):num.tfs ) {
      if ( tf.cor[i,j] > cor.cut ) {
        still.in[j] <- FALSE
        tf.merge.cors[[ names( still.in )[i] ]] <-
          c( tf.merge.cors[[ names( still.in )[i] ]] , names( still.in )[j] ) 
      }
    }
  }

  tfs.new <- names(still.in[ still.in ] )

  return( list( tfs = tfs.new, tf.cors = tf.merge.cors) )
  
}

##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
# input: cM - colMap
#			r - expression matrix (assumed already normalized)
#			param - a vector of 2 params:
#						1-  c1 - cutoff1: represent the maximal time interval allowed for time series (ts) data
#						2- 'trivial' or 'time_delayed' (param to choose the type of design matrix)
#						3- 'consecutive' or 'all_intervals' (determine if consecutive time measurements [no longer than c1], 
#							 or all permutations of time measurements [up to c1] respectively)
#						4- 'TRUE' or 'FALSE' use_t0_as_steady_state
#						5- 'TRUE' or 'FALSE' use_delt_bigger_than_cutoff_as_steady_state
#
# output: 
#			 steadyStateDesignMat, timeSeriesDesignMat

get_usr_chosen_design <- function(cM, r, delT_min, delT_max, time_delayed, 
                                  all_intervals, use_t0_as_steady_state, 
                                  use_delt_bigger_than_cutoff_as_steady_state) {
  
  delT_min_vec = rep(delT_min, nrow(cM))
  delT_max_vec = rep(delT_max, nrow(cM))
  
  delT_vec = cM$del.t
  isTs_vec = cM$isTs
  eq_idx   = which(!isTs_vec)
  ts_idx   = which(isTs_vec)
  
  delT_vec 		 = delT_vec		 [ts_idx]
  delT_min_vec = delT_min_vec[ts_idx]
  delT_max_vec = delT_max_vec[ts_idx]
  # set delT_vec: 
  #		0 - last time measurement in ts
  #		>0 - first and middle time measurements in ts
  # following line make 0s indicate first time measurement in ts
  delT_vec[which(is.na(delT_vec))] = 0

  delT_vec_trivial = delT_vec
  # following 2 lines make 0s indicate last time measurement in ts
  delT_vec[-length(delT_vec)] = delT_vec[-1]
  delT_vec[length(delT_vec)] = 0
  
  # data for steady state
  rSS = r[,eq_idx]
  # if we have time series experiments
  if(length(ts_idx)>0) { 
    # data for time series
    if( any(eq_idx) ){ #simple check to see if there are any equlibrium conditions 
      rTS = r[,-eq_idx]
    }else{
      rTS = r
    }
    
    eq_idx_pseudo = numeric()
    
    # get ts starting conditions, we treat these as equilibrium
    if (use_t0_as_steady_state)
      eq_idx_pseudo = which(delT_vec_trivial == 0)
    
    # get ts conditions with larger than c1 delt, we treat these as equilibrium
    if (use_delt_bigger_than_cutoff_as_steady_state)
      eq_idx_pseudo = c(eq_idx_pseudo, which(delT_vec_trivial > max(delT_max_vec)))
    
    # create design matrix for steady state
    if( any(eq_idx) ){
      DesignMatSS = cbind(rSS, rTS[,eq_idx_pseudo])
    }else{
      DesignMatSS = rTS[,eq_idx_pseudo,drop=F]
    }
    
    if (all_intervals) { # all permutations time series
      #x 					 = get_all_perms(delT_vec, delT_min_vec[1], delT_max_vec[1]) #assumes delT_min & max are scalars
      x 					 = get_all_perms_vec(delT_vec, delT_min_vec, delT_max_vec) #assumes delT_min & max are vectors
      init_ind 		 = x[[1]]
      boundary_ind = x[[2]]
      DesignMatTS  = rTS[,init_ind]
    } else { # consecutive time series 
      if(time_delayed) {
        DesignMatTS = rTS[,which(delT_vec != 0 & delT_vec <= delT_max_vec)]
      } else {
        DesignMatTS = rTS[,which(delT_vec_trivial != 0 & delT_vec_trivial <= delT_max_vec)]
      }
    }
  } else {
    DesignMatSS = rSS
    DesignMatTS = matrix(0, nrow(DesignMatSS), 0)
  }
  return(list(DesignMatSS,as.matrix(DesignMatTS)))
}


##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
# input: cM - colMap
#			r - ratios matrix (assumed already normalized)
#			param - a vector of 5 params:
#						1-  c1 - cutoff1: represent the maximal time interval allowed for time series (ts) data
#						2- 'trivial','time_difference','rate','inf_1', or 'inf_1_all_intervals'
#						3- tau
#						4- TRUE or FALSE use_t0_as_steady_state
#						5- TRUE or FALSE use_delt_bigger_than_cutoff_as_steady_state
#
# output:
#			 steadyStateResponseMat timeSeriesResponseMat

get_usr_chosen_response <- function(cM, r, delT_min, delT_max, method, tau, 
                                    use_t0_as_steady_state, 
                                    use_delt_bigger_than_cutoff_as_steady_state) {

  delT_min_vec = rep(delT_min, nrow(cM))
  delT_max_vec = rep(delT_max, nrow(cM))
  
  delT_vec = cM$del.t
  isTs_vec = cM$isTs
  eq_idx   = which(!isTs_vec)
  ts_idx   = which(isTs_vec)
  
  delT_vec     = delT_vec[ts_idx]
  delT_min_vec = delT_min_vec[ts_idx]
  delT_max_vec = delT_max_vec[ts_idx]
  
# set delT_vec: 
#		0 - last time measurement in ts
#		>0 - first and middle time measurements in ts
# following line make 0's indicate first time measurement in ts
  delT_vec[which(is.na(delT_vec))] = 0
  delT_vec_trivial = delT_vec
# following 2 lines make 0's indicate last time measurement in ts
  delT_vec[-length(delT_vec)] = delT_vec[-1]
  delT_vec[length(delT_vec)] = 0
  
  rSS = r[,eq_idx]
  # if we have time series experiments
  if(length(ts_idx)>0) { 
    if(any(eq_idx)){
      rTS = r[,-eq_idx]
    }else{
      rTS=r
    }
    
    eq_idx_pseudo = numeric()
    # get ts starting conditions, we treat these as equilibrium
    if (use_t0_as_steady_state)
      eq_idx_pseudo = which(delT_vec_trivial == 0)
    
    # get ts conditions with larger than c1 delt, we treat these as equilibrium
    if (use_delt_bigger_than_cutoff_as_steady_state)
      eq_idx_pseudo = c(eq_idx_pseudo, which(delT_vec_trivial > max(delT_max_vec)))
    
    if(any(eq_idx)){
      response_matrixSS = cbind(rSS, rTS[,eq_idx_pseudo])
    }else{
      if(any(eq_idx_pseudo)){
        response_matrixSS = rTS[,eq_idx_pseudo,drop=F]
      }else{
        response_matrixSS = matrix()		
      }
    }
    
    init_ind = which(delT_vec != 0 & delT_vec <= delT_max_vec)
    boundary_ind = init_ind+1
    
    # finished response matrices now go on to response
    if (method == 'trivial') {
      response_matrixTS = rTS[,boundary_ind]
    } 	else if (method == 'time_difference') {
      response_matrixTS = (rTS[,boundary_ind] - rTS[,init_ind])
    }	else if (method == 'rate') {
      response_matrixTS = t(1/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind]))
    }	else if (method == 'inf_1') {
      response_matrixTS = t(tau/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind])) + (rTS[,init_ind])
    } else if (method == 'inf_1_all_intervals') {
      #x 					 		  = get_all_perms    (delT_vec, delT_min_vec[1], delT_max_vec[1]) #assumes delT_min and delT_max are scalars
      x 					 		  = get_all_perms_vec(delT_vec, delT_min_vec, delT_max_vec) #assumes delT_min and delT_max are vectors
      init_ind 		      = x[[1]]
      boundary_ind      = x[[2]]
      response_matrixTS = t(tau/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind])) + (rTS[,init_ind])
    } else {
      stop("unknown response read")
    }
  } else {
    response_matrixSS = rSS
    response_matrixTS = matrix(0, nrow(response_matrixSS), 0)		
  }
  return(list(response_matrixSS ,response_matrixTS))
}



##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
# input:
#	1- dMSS: design matrix steady state
#	2- dMTS: design matrix time series
#	3- rMSS: response matrix steady state
#	4- dMTS: response matrix time series
#	5- param: what final design matrix? choose from all, ts, or ss

# output:
#	resopnse and corresponding design matrices
make_final_design_and_response_matrix <- function(dMSS, dMTS, rMSS, rMTS, param, cS, r, tf.names, make.des.red.exp = F) {
  if (param == 'all') {
    final_response_matrix = cbind(rMSS, rMTS) 
    final_design_matrix = cbind(dMSS, dMTS)
  } else if (param == 'ts') {
    final_response_matrix = rMTS 
    final_design_matrix = dMTS
  } else if (param == 'ss') {
    final_response_matrix = rMSS		
    final_design_matrix = dMSS
  } else {
    stop("unknown final design or response matrices read")
  }
  
  #check if we have biclusters
  #if we do, make final_response_martix to be the 
  #median of the genes in the bicluster, and add TFs to the end of that
  
  cs.num.rows <- as.vector(unlist(lapply(cS, function(i) i$nrows)))
  if(any(cs.num.rows > 1) & (nrow(r) != length(cS))){
    cat("We have biclusters--generating redExp for response and design--\n")
    #finding if we have any single genesin the clusterStack
    #also adding any genes that are not in clusterStack as singletons
    singletons        <- unlist(lapply( cS[ which(cs.num.rows == 1) ], function(i) i$rows))		
    ag                <- unique(c(unlist(lapply(cS, function(i) i$rows)), tf.names))
    
    singletons.to.add <- unique(c(singletons, rownames(r)[which(!rownames(r) %in% ag)]))
    #tf.names #hack to keep full list of tfs, used in making the design matrix
    if(!is.null(singletons.to.add) & any(singletons.to.add %in% tf.names)) 
      singletons.to.add <- singletons.to.add[-which(singletons.to.add %in% tf.names)]

    #intialize red.exp, which contains the reduced expression (median expression)
    #for reach gene in the cluster
    red.exp <- matrix( NA, length(tf.names) + length(cs.num.rows) + length(singletons.to.add), ncol(final_response_matrix))
    colnames(red.exp) <- colnames(final_response_matrix)
  
    des.red.exp <- matrix( NA, length(tf.names) + length(cs.num.rows) + length(singletons.to.add), ncol(final_response_matrix))
    #initialize resp_idx, 
    #index of i,j contains a 0 if condition i is not in bicluster
    #or the index of j in colnames(final_response_matrix) if condition j is in bicluster i
    resp_idx <- matrix( 0, length(tf.names) + length(cs.num.rows) + length(singletons.to.add), ncol(final_response_matrix))
    
    cs.start.idx <- length(tf.names) + 1
    cs.end.idx   <- cs.start.idx + length(cS) - 1
    mat.idx      <- cs.start.idx:cs.end.idx
    for(i in 1:length(cS)){ 
      #get the rows for the bicluster
      cs.rows <- cS[[i]]$rows
      #get the cols for the bicluster...this if statement is because some biclusters
      #have $cols and some have $conds...WTF
      if( !is.null(cS[[i]]$cols) ){
        cs.cols <- cS[[i]]$cols
      }else{
        cs.cols <- cS[[i]]$conds
      }
      
      #calculate the reduced expression for this bicluster, and put it in red.exp
      #NOTE: some biclusters contain the first time-series...this can't be in the 
      #response matrix, because we use a time lag, so the first time-series is removed
      #THUS, we only take the columns which are in the response matrix
      cs.cols     <- colnames(final_response_matrix)[which(colnames(final_response_matrix) %in% cs.cols)]
      cs.cols.idx <- which(colnames(final_response_matrix) %in% cs.cols)
      
      cs.red.exp <- apply(final_response_matrix[cs.rows, cs.cols, drop=F], 2, mean)
      red.exp[mat.idx[i], cs.cols.idx] <- cs.red.exp
      
      #create the indexing vector for this bicluster and store it in resp_idx
      all.idx <- rep(0, ncol(final_response_matrix))
      all.idx[ which(colnames(final_response_matrix) %in% cs.cols) ] <- which(colnames(final_response_matrix) %in% cs.cols) 
      resp_idx[mat.idx[i], ] <- all.idx
    
      #getting the red_exp for the design matrix
      if(make.des.red.exp)
         des.red.exp[mat.idx[i],] <- apply( final_design_matrix[cs.rows, ,drop=F],2,mean)
    }
    #add the genes which are not in the any clusters to the clusterStack
    sing.start.idx <- length(tf.names) + 1 + length(cS)
    sing.stop.idx  <- sing.start.idx + length(singletons.to.add) - 1
    red.exp[sing.start.idx:sing.stop.idx, ] <- final_response_matrix[singletons.to.add, ]
    
    #add the TFs to red.exp	
    tfs.start.idx = 1
    tfs.stop.idx  = tfs.start.idx + length(tf.names) - 1
    red.exp[tfs.start.idx:tfs.stop.idx, ] <- final_response_matrix[tf.names,]
    #create rownames: its either the bicluster, or the name of the TF, if the bicluster is a singleton then we use the name of the gene
    if (is.null(names(cS))) {
      r.names <- c(paste(sep="","BC_",seq(1:length(cS))))
      r.names <- c(tf.names, r.names, singletons.to.add)#r.names[ which(cs.num.rows == 1) ] <- singletons
    } else {
      r.names <- c(tf.names,names(cS), singletons.to.add)
    }
    rownames(red.exp) <- r.names
    
    #add the indexing vectors for the TFs and singletons, 
    #this is trivial as for the TFs they have expression in every condition
    resp_idx[c(tfs.start.idx:tfs.stop.idx, sing.start.idx:sing.stop.idx) , ] <- t(matrix(rep(1:ncol(final_response_matrix),length(tf.names) + length(singletons.to.add)), 
                                                                                    nrow = ncol(final_response_matrix), ncol= length(tf.names) + length(singletons.to.add)))
    final_response_matrix <- red.exp
    
    #finishing the final_design_matrix
    if(make.des.red.exp){
      #des.red.exp[(length(cS)+1):nrow(des.red.exp), ] <- final_design_matrix[tf.names,]
      des.red.exp[sing.start.idx:sing.stop.idx, ] <- final_design_matrix[singletons.to.add, ]
      #add the TFs to the design red.exp
      des.red.exp[tfs.start.idx:tfs.stop.idx, ] <- final_design_matrix[tf.names, ]
      
      if (is.null(names(cS))) {
        r.names <- c(paste(sep="","BC_",seq(1:length(cS))))
        r.names <- c(tf.names, r.names, singletons.to.add)
        #r.names[ which(cs.num.rows == 1) ] <- singletons
      } else {
        r.names <- c(tf.names, names(cS), singletons.to.add)
      }
      rownames(des.red.exp) <- r.names
      final_design_matrix   <- des.red.exp
    } else {
      final_design_matrix <- final_design_matrix[tf.names, ]
    }
  }else{
    resp_idx <- t(matrix(rep(c(1:ncol(final_response_matrix)), nrow(final_response_matrix)), ncol = nrow(final_response_matrix), nrow=ncol(final_response_matrix)))
  }
    
  return (list(final_response_matrix, final_design_matrix, resp_idx))
}


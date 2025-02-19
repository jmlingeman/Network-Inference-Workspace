##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

## January 2009 Inferelator
## Bonneau lab - Aviv Madar
## NYU - Center for Genomics and Systems Biology

##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
let_usr_choose_dataset <- function() {
	data_is = NULL
	while(is.null(data_is)){
		data_is = switch( menu(c("DREAM2","DREAM3","DREAM4"), graphics = FALSE, title = "Run for:"), "DREAM2","DREAM3","DREAM4")
		if(!is.null(data_is)) { break }
		cat("You must choose a data-set. Click ctrl c to exit.")
	}

	if(data_is == "DREAM2" | data_is == "DREAM3" | data_is == "DREAM4") {
		x = data_is
		data_is = NULL
		while(is.null(data_is)){
			if (x == "DREAM2") {
				data_is = switch( menu(c("DREAM2_1_50","DREAM2_2_50"),graphics = FALSE, title = "Run for which DREAM exactly?"), "DREAM2_1_50","DREAM2_2_50")
			} else if (x == "DREAM3") {
				data_is = switch( menu(c("DREAM3_1_10","DREAM3_1_50","DREAM3_1_100",
										"DREAM3_2_10","DREAM3_2_50","DREAM3_2_100",
										"DREAM3_3_10","DREAM3_3_50","DREAM3_3_100",
										"DREAM3_4_10","DREAM3_4_50","DREAM3_4_100",
										"DREAM3_5_10","DREAM3_5_50","DREAM3_5_100"),
								graphics = FALSE, title = "Run for which DREAM exactly?"),
						"DREAM3_1_10","DREAM3_1_50","DREAM3_1_100",
						"DREAM3_2_10","DREAM3_2_50","DREAM3_2_100",
						"DREAM3_3_10","DREAM3_3_50","DREAM3_3_100",
						"DREAM3_4_10","DREAM3_4_50","DREAM3_4_100",
						"DREAM3_5_10","DREAM3_5_50","DREAM3_5_100")
			} else if (x == "DREAM4") {
				data_is = switch( menu(c("DREAM4_1_10","DREAM4_1_100",
										"DREAM4_2_10","DREAM4_2_100",
										"DREAM4_3_10","DREAM4_3_100",
										"DREAM4_4_10","DREAM4_4_100",
										"DREAM4_5_10","DREAM4_5_100"),
								graphics = FALSE, title = "Run for which DREAM exactly?"),
						"DREAM4_1_10","DREAM4_1_100",
						"DREAM4_2_10","DREAM4_2_100",
						"DREAM4_3_10","DREAM4_3_100",
						"DREAM4_4_10","DREAM4_4_100",
						"DREAM4_5_10","DREAM4_5_100")
			}
			if(!is.null(data_is)) { break }
			cat("You must choose a data-set. Click ctrl c to exit.")
		}
	}
	return (data_is)
}
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

let_usr_choose_response <- function() {
	response_clr = NULL
	while(is.null(response_clr)){
		response_clr = switch( menu(c("y_k [trivial]","y_k - y_{k-1} [time difference]","(y_k - y_{k-1})/(t_k-t_{k-1}) [rate]",
									  "(y_k - y_{k-1})/(t_k-t_{k-1}) + 1/tau*y_k [inf 1]", "(y_k - y_{k-k'})/(t_k-t_{k-k'}) + 1/tau*y_k' [inf 1 all ts intervals]"),
											 graphics = FALSE, title = "Choose response for CLR:"),
											 "trivial","time_difference","rate","inf_1","inf_1_all_intervals")
			if(!is.null(response_clr)) { break }
				cat("You must choose a response. Click ctrl c to exit.")
	}
	response_lars = NULL
	while(is.null(response_lars)){
		response_lars = switch( menu(c("y_k [trivial]","y_k - y_{k-1} [time difference]","(y_k - y_{k-1})/(t_k-t_{k-1}) [rate]",
									   "(y_k - y_{k-1})/(t_k-t_{k-1}) + 1/tau*y_k [inf 1]", "(y_k - y_{k-k'})/(t_k-t_{k-k'}) + 1/tau*y_k' [inf 1 all ts intervals]"),
											  graphics = FALSE, title = "Choose response for LARS:"),
											  "trivial","time_difference","rate","inf_1","inf_1_all_intervals")
		if(!is.null(response_lars)) { break }
			cat("You must choose a response. Click ctrl c to exit.")
	}
	return( list(response_clr,response_lars) )
}

##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '


let_usr_choose_design_matrix <- function() {
	design_clr = NULL
	while(is.null(design_clr)){
		design_clr = switch( menu(c("X_k [trivial]","X_{k-1} [time delayed]"),
										 graphics = FALSE, title = "Choose design matrix for CLR:"),
									    "trivial","time_delayed")
		if(!is.null(design_clr)) { break }
		cat("You must choose a design matrix. Click ctrl c to exit.")
	}
	design_lars = NULL
	while(is.null(design_lars)){
		design_lars = switch( menu(c("X_k [trivial]","X_{k-1} [time delayed]"),
											  graphics = FALSE, title = "Choose design matrix for LARS:"),
											  "trivial","time_delayed")
		if(!is.null(design_lars)) { break }
		cat("You must choose a design matrix. Click ctrl c to exit.")
	}
	return( list(design_clr,design_lars) )
}


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

get_usr_chosen_design_matrix <- function(cM, r, params) {
	c1 = as.numeric(params[1])
	use_t0_as_steady_state = as.logical(params[4])
	use_delt_bigger_than_cutoff_as_steady_state = as.logical(params[5])

	delT_vec = sapply(cM, function(i) i$del.t)
	isTs_vec = sapply(cM, function(i) i$isTs)
    print(isTs_vec)
	eq_idx = which(!isTs_vec) #
	ts_idx = which(isTs_vec)

	delT_vec = delT_vec[ts_idx]
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
	# data for time series
	rTS = r[,-eq_idx]

	eq_idx_pseudo = numeric()

	# get ts starting conditions, we treat these as equilibrium
	if (use_t0_as_steady_state)
		eq_idx_pseudo = which(delT_vec_trivial == 0)

	# get ts conditions with larger than c1 delt, we treat these as equilibrium
	if (use_delt_bigger_than_cutoff_as_steady_state)
		eq_idx_pseudo = c(eq_idx_pseudo, which(delT_vec_trivial > c1))

	# create design matrix for steady state
	DesignMatSS = cbind(rSS, rTS[,eq_idx_pseudo])

	if (params[3] == 'all_intervals') { # all permutations time series
		x = get_all_perms(delT_vec, c1)
		init_ind = x[[1]]
		boundary_ind = x[[2]]
		DesignMatTS = rTS[,init_ind]
	} else # consecutive time series
	{
		if(params[2] == 'time_delayed') {
			DesignMatTS = rTS[,which(delT_vec != 0 & delT_vec <= c1)]
		} else {
			DesignMatTS = rTS[,which(delT_vec_trivial != 0 & delT_vec_trivial <= c1)]
		}
	}
	return (list(DesignMatSS,DesignMatTS))
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

get_usr_chosen_response  <- function(cM, r, params) {
	c1 = as.numeric(params[1])
	tau = as.numeric(params[3])
	use_t0_as_steady_state = as.logical(params[4])
	use_delt_bigger_than_cutoff_as_steady_state = as.logical(params[5])

	delT_vec = sapply(cM, function(i) i$del.t)
	isTs_vec = sapply(cM, function(i) i$isTs)
	eq_idx = which(!isTs_vec)
	ts_idx = which(isTs_vec)

	delT_vec = delT_vec[ts_idx]
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
	rTS = r[,-eq_idx]

	eq_idx_pseudo = numeric()
	# get ts starting conditions, we treat these as equilibrium
	if (use_t0_as_steady_state)
		eq_idx_pseudo = which(delT_vec_trivial == 0)

	# get ts conditions with larger than c1 delt, we treat these as equilibrium
	if (use_delt_bigger_than_cutoff_as_steady_state)
		eq_idx_pseudo = c(eq_idx_pseudo, which(delT_vec_trivial > c1))

	response_matrixSS = cbind(rSS, rTS[,eq_idx_pseudo])

	init_ind = which(delT_vec != 0 & delT_vec <= c1)
	boundary_ind = init_ind+1

# finished response matrices now go on to response
	if (params[2] == 'trivial') {
		response_matrixTS = rTS[,boundary_ind]
	} 	else if (params[2] == 'time_difference') {
		response_matrixTS = (rTS[,boundary_ind] - rTS[,init_ind])
	}	else if (params[2] == 'rate') {
		response_matrixTS = t(1/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind]))
	}	else if (params[2] == 'inf_1') {
		response_matrixTS = t(tau/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind])) + (rTS[,init_ind])
	} else if (params[2] == 'inf_1_all_intervals') {
		x = get_all_perms(delT_vec, c1)
		init_ind = x[[1]]
		boundary_ind = x[[2]]
		response_matrixTS = t(tau/delT_vec[init_ind] * t(rTS[,boundary_ind] - rTS[,init_ind])) + (rTS[,init_ind])
	} else {
		stop("unknown response read")
	}
	return(list(response_matrixSS ,response_matrixTS))
}


##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
# a helper function for get_usr_chosen_design_matrix and get_usr_chosen_response

get_all_perms <- function(vec, cutoff) {
	boundary_idx = numeric()
	init_idx = numeric()
	for (i in 1: length(vec)) {
		j=i;
		while ((vec[j] != 0) & (sum(vec[i:j]) <= cutoff)) {
			init_idx = c(init_idx,i);
			boundary_idx = c(boundary_idx,j+1);
			j=j+1
		}
	}
	return (list(init_idx,boundary_idx))
}
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
# input:
#	1- dMSS: design matrix steady state
#	2- dMTS: design matrix time state
#	3- rMSS: response matrix steady state
#	4- dMTS: response matrix time state
#	5- param: what final design matrix? choose from all, ts, or ss

# output:
#	resopnse and corresponding design matrices
make_final_design_and_response_matrix <- function(dMSS, dMTS, rMSS, rMTS, param) {
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
	return (list(final_response_matrix, final_design_matrix))
}


##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

get_usr_chosen_dataset <- function(dset) {
	obj = list()
	all_data_files <- c("ratios.RData",
			"clusterStack.RData",
			"colMap.RData",
			"tfNames.RData",
			"goldStandard.txt",
			"dreamPrediction.txt","dualknockouts_indexes.tsv",
			"dualknockouts_goldStandard.tsv","dualknockoutsPrediction.txt",
			"dreamPrediction_dyn.txt")

	if (dset == "DREAM2_1_50") { # read data for dream 2_1
		path = "input/DREAM2/synthetic_1_size50/"
	} else if (dset == "DREAM2_2_50") {
		path = "input/DREAM2/synthetic_2_size50/"
	} else if (dset == "DREAM3_1_10") {
		path = "input/DREAM3/InSilicoSize10/ecoli_1/"
	} else if (dset == "DREAM3_2_10") {
		path = "input/DREAM3/InSilicoSize10/ecoli_2/"
	} else if (dset == "DREAM3_3_10") {
		path = "input/DREAM3/InSilicoSize10/yeast_1/"
	} else if (dset == "DREAM3_4_10") {
		path = "input/DREAM3/InSilicoSize10/yeast_2/"
	} else if (dset == "DREAM3_5_10") {
		path = "input/DREAM3/InSilicoSize10/yeast_3/"
	} else if (dset == "DREAM3_1_50") {
		path = "input/DREAM3/InSilicoSize50/ecoli_1/"
	} else if (dset == "DREAM3_2_50") {
		path = "input/DREAM3/InSilicoSize50/ecoli_2/"
	} else if (dset == "DREAM3_3_50") {
		path = "input/DREAM3/InSilicoSize50/yeast_1/"
	} else if (dset == "DREAM3_4_50") {
		path = "input/DREAM3/InSilicoSize50/yeast_2/"
	} else if (dset == "DREAM3_5_50") {
		path = "input/DREAM3/InSilicoSize50/yeast_3/"
	} else if (dset == "DREAM3_1_100") {
		path = "input/DREAM3/InSilicoSize100/ecoli_1/"
	} else if (dset == "DREAM3_2_100") {
		path = "input/DREAM3/InSilicoSize100/ecoli_2/"
	} else if (dset == "DREAM3_3_100") {
		path = "input/DREAM3/InSilicoSize100/yeast_1/"
	} else if (dset == "DREAM3_4_100") {
		path = "input/DREAM3/InSilicoSize100/yeast_2/"
	} else if (dset == "DREAM3_5_100") {
		path = "input/DREAM3/InSilicoSize100/yeast_3/"
	} else if (dset == "DREAM4_1_100") {
		path = "input/DREAM4/InSilicoSize100/net_1/"
	} else if (dset == "DREAM4_2_100") {
		path = "input/DREAM4/InSilicoSize100/net_2/"
	} else if (dset == "DREAM4_3_100") {
		path = "input/DREAM4/InSilicoSize100/net_3/"
	} else if (dset == "DREAM4_4_100") {
		path = "input/DREAM4/InSilicoSize100/net_4/"
	} else if (dset == "DREAM4_5_100") {
		path = "input/DREAM4/InSilicoSize100/net_5/"
	} else if (dset == "DREAM4_1_10") {
		path = "input/DREAM4/InSilicoSize10/net_1/"
	} else if (dset == "DREAM4_2_10") {
		path = "input/DREAM4/InSilicoSize10/net_2/"
	} else if (dset == "DREAM4_3_10") {
		path = "input/DREAM4/InSilicoSize10/net_3/"
	} else if (dset == "DREAM4_4_10") {
		path = "input/DREAM4/InSilicoSize10/net_4/"
	} else if (dset == "DREAM4_5_10") {
		path = "input/DREAM4/InSilicoSize10/net_5/"
	} else {
		return (-1) # failed to read input
	}

	n_loaded = 0
	# load data structures except gold standard (which may not exist)
    #cat("all data files: ", length(all_data_files),"\n") #for debugging
	for (i in 1:4) {
		file = all_data_files[i]
		cat("loading ", file, "\n")
		if(file.exists( paste(path,file, sep = "")) )
			load( paste(path,file, sep = "") )
		else stop(file, " does not exist in dir: ", path, " bailing out...",sep='')
		n_loaded = n_loaded+1
	}

#	for (file in all_data_files[-length(all_data_files)]) {
#		cat("loading ", file, "\n")
#		if(file.exists( paste(path,file, sep = "")) )
#			load( paste(path,file, sep = "") )
#		else stop(file, " does not exist in dir: ", path, " bailing out...",sep='')
#		n_loaded = n_loaded+1
#	}


	obj[[1]] = ratios
	obj[[2]] = clusterStack
	obj[[3]] = colMap
	obj[[4]] = tfNames
	# load gold standard file and dream predictions if exist
	for (i in 5:length(all_data_files)){
		file = all_data_files[i]
		if(file.exists( paste(path,file, sep = "")))    {
			cat("loading ", file, "\n")
			if(i == 5){
				obj[[i]] = load_gold_standard( paste(path,file, sep = ""), r_names=rownames(ratios), c_names = tfNames )
			}else if(i == 6){
			  obj[[i]] = load_predictions( paste(path,file,sep="") )
			}else if(i == 7){
				obj[[i]] <- as.matrix( read.table( paste(path,file, sep = ""),header=T) )
			}else if(i == 10){
				obj[[i]] = load_predictions( paste(path,file,sep="") )
			}else{
				obj[[i]] <- as.matrix( read.table( paste(path,file, sep = "")) )
			}
		} else {
			obj[[i]] = NA
		}
	}
	return( obj )
}

##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

load_gold_standard <- function( file, r_names=NULL, c_names=NULL ) {
	x = as.matrix(read.table(file))
	if(ncol(x) == 3) {
		if(is.null(r_names) | is.null(c_names))
			stop("can't read file: ", file,". missing row names or column names.")
		y = matrix(0,length(r_names),length(c_names))
		rownames(y) = r_names
		colnames(y) = c_names
		idx_non_zero = which(as.numeric(x[,3]) != 0)
		for (i in 1:length(idx_non_zero)) {
			y[x[idx_non_zero[i],2],x[idx_non_zero[i],1]] = as.numeric(x[idx_non_zero[i],3])
		}
		return(y)
	}
	return(x)
}

load_tfnames <- function( file, ratios ) {
    if(file != 'None') {
        x <- as.matrix(read.table(file))
    } else {
        x <- as.matrix(row.names(ratios))
    }
    return( x )
}

load_predictions <- function( filePath ){
	x <- as.matrix( read.table(filePath,sep="\t") )
	#switch col1 and col2, so that when usparsing rows are targers, cols are predictors...this makes
	#it consistent with all of the other matices of this type that we sue
	temp <- x[,1]
	x[,1] <- x[,2]
	x[,2] <- temp
	x[,3] <- as.double(x[,3]) #to convert from exponential notation
	return(x)
}
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '







































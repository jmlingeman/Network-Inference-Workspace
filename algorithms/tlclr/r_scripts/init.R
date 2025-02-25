##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
## May 2010 Dream3/4 pipeline (MCZ,tlCLR,Inferelator)
## Bonneau lab - "Aviv Madar" <am2654@nyu.edu>, 
##  		     "Alex Greenfield" <ag1868@nyu.edu> 
## NYU - Center for Genomics and Systems Biology
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

# source scripts
source("r_scripts/init_util.R")
source("r_scripts/utils.R")
source("r_scripts/larsUtil.R")
source("r_scripts/clr.R")
source("r_scripts/validationScript.R")
source("r_scripts/bootstrapUtil.R")

# get required packages
library(mi)
library(elasticnet)

# init PARAMS and INPUT
PARAMS = list()
INPUT = list()

PARAMS[["general"]] = list()
PARAMS[["clr"]] = list()
PARAMS[["lars"]] = list()
PARAMS[["output"]] = list()

######################### most useful params ##################################
# how many predictors (tf's) for elastic net to choose from?
PARAMS[["lars"]][["max_single_preds"]] = 15
# what l2 norm weights to use in elastic net? (lambda =0 same as LARS)
PARAMS[["lars"]][["lambda"]] = c(0) # or try c(0,1,100)
# how many bootstrap runs? (one for each bootstraped dataset)
PARAMS[["general"]][["numBoots"]] = 10
# what is the maximum delta T to consider? (if delta T is bigger than it will be treated as steady state)
# the time interval between measurements for DREAM2, DREAM3, and DREAM4 was 10,20, and 50 respectively
PARAMS[["general"]][["delT_max"]] = 110 # for dream4 try 110-210, for dream3 try 45-85, for dream2 try 25-45 (ie consider at most two-four consecutive steps in time series)
# what is the time order (tau) of the reactinos (i.e. in what time do you expect reactions to happen in)?
PARAMS[["general"]][["tau"]] = 45 # for dream4 try 40-60, for dream3 15-25, for dream2 try ~10 (ie assumes most reactions happen in about a time step)
# how many low confidence MCZ interactions (in percentile) do you want to filter out ()i.e. remove from further consideration by tlCLR->inf)?
PARAMS[["general"]][["MCZ_fltr_prcntile"]] = .5
# how many processors to use? if use more than one need to install the multicore package
PARAMS[["general"]][["processorsNumber"]] = 1
#boolean to determine weather or not to plot cross validation (cv) curves for elastic net, area under precision recall (AUPR) curves, and performance of 
#double knockout prediction
#note that plotting cv curves works only for when running the pipeline in a non-parallel manner
PARAMS[["general"]][["plot.it"]] <- TRUE
#############################################################################
if(PARAMS[["general"]][["processorsNumber"]]>1){
	library(multicore)
}
PARAMS[["general"]][["inf.version"]] = "nwInf.1.2"
x = unlist(strsplit(date()," "))
PARAMS[["general"]][["date"]] = paste(x[2],x[3],x[5],x[4],sep="_")
PARAMS[["general"]][["data"]] = let_usr_choose_dataset()
PARAMS[["general"]][["use_t0_as_steady_state"]] = FALSE
PARAMS[["general"]][["use_mixCLR"]] = TRUE #for DREAM4
PARAMS[["general"]][["use_delt_bigger_than_delT_max_as_steady_state"]] = TRUE
#making the directory name to save files into
PARAMS[["general"]][["saveToDir"]] = paste("results/distributions/",PARAMS[["general"]]$data,"_",
                                          gsub(":","-",PARAMS[["general"]]$date),"_nboots_",PARAMS$"general"$"numBoots",sep="")
PARAMS[["general"]][["percentCoverage"]] = 100

# are we running for 'time-sereies' only, 'steady state' only, or 'all' for lars and clr respectively
PARAMS[["clr"]][["what_final_design_response_matrix"]] = 'all' # choose here between ts, ss, or all
PARAMS[["lars"]][["what_final_design_response_matrix"]] = 'all' # choose here between ts, ss, or all

x = let_usr_choose_response()
PARAMS[["clr"]][["response_matrix"]] = x[[1]]
PARAMS[["lars"]][["response_matrix"]] = x[[2]]

x = let_usr_choose_design_matrix()
PARAMS[["clr"]][["design_matrix"]] = x[[1]]
PARAMS[["lars"]][["design_matrix"]] = x[[2]]

x = get_usr_chosen_dataset(PARAMS[["general"]][["data"]])
INPUT[["general"]][["dataset"]] = x[[1]]
INPUT[["general"]][["clusterStack"]] = x[[2]]
INPUT[["general"]][["colMap"]] = x[[3]]
INPUT[["general"]][["tf_names"]] = x[[4]]
INPUT[["general"]][["gold_standard"]] = x[[5]] # if available
INPUT[["general"]][["dreamPrediction"]] = x[[6]] # if available
INPUT[["general"]][["dbl_ko"]] = x[[7]]
INPUT[["general"]][["dblKo_goldStandard"]] = x[[8]]
INPUT[["general"]][["dblKo_prediction"]] = x[[9]]

#get clr design matrix
if (PARAMS[["clr"]][["response_matrix"]] == 'inf_1_all_intervals') { 
	x = 'all_intervals' 
} else { 
	x = 'consecutive' 
}

params = c(PARAMS[["general"]][["delT_max"]],PARAMS[["clr"]][["design_matrix"]], x,
			  PARAMS[["general"]][["use_t0_as_steady_state"]],PARAMS[["general"]][["use_delt_bigger_than_delT_max_as_steady_state"]])
# get clr design matrix: 1- steady_state, 2- time_series
x = get_usr_chosen_design_matrix(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])], # colMap with out last element ($K)
											INPUT[["general"]][["dataset"]],
											params)

INPUT[["clr"]][["design_matrix_steady_state"]] = x[[1]]
INPUT[["clr"]][["design_matrix_time_series"]] = x[[2]]

#get lars design matrix
if (PARAMS[["lars"]][["response_matrix"]] == 'inf_1_all_intervals') { 
	x = 'all_intervals' 
} else { 
	x = 'consecutive' 
}

params = c(PARAMS[["general"]][["delT_max"]],PARAMS[["lars"]][["design_matrix"]], x,
			  PARAMS[["general"]][["use_t0_as_steady_state"]],PARAMS[["general"]][["use_delt_bigger_than_delT_max_as_steady_state"]])
# get lars design matrix: 1- steady_state, 2- time_series
x = get_usr_chosen_design_matrix(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])], # colMap with out last element ($K)
											as.matrix(as.data.frame(INPUT[["general"]][["dataset"]])[INPUT[["general"]][["tf_names"]],]),
											params)
											
INPUT[["lars"]][["design_matrix_steady_state"]] = x[[1]]
INPUT[["lars"]][["design_matrix_time_series"]] = x[[2]]

#get clr response matrix
params =  c(PARAMS[["general"]][["delT_max"]],PARAMS[["clr"]][["response_matrix"]], PARAMS[["general"]][["tau"]],
				PARAMS[["general"]][["use_t0_as_steady_state"]],PARAMS[["general"]][["use_delt_bigger_than_delT_max_as_steady_state"]])
if( is.null(INPUT[["general"]][["redExp"]])){
	x = get_usr_chosen_response(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])],
						 INPUT[["general"]][["dataset"]], 
						params)
}else{
	cat("getting clr response for biclusters \n")
	x = get_usr_chosen_response(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])],
			INPUT[["general"]][["redExp"]], 
			params)
}
						 
INPUT[["clr"]][["response_matrix_steady_state"]] = x[[1]]
INPUT[["clr"]][["response_matrix_time_series"]] = x[[2]]

#get lars response matrix
params =  c(PARAMS[["general"]][["delT_max"]],PARAMS[["lars"]][["response_matrix"]], PARAMS[["general"]][["tau"]],
				PARAMS[["general"]][["use_t0_as_steady_state"]],PARAMS[["general"]][["use_delt_bigger_than_delT_max_as_steady_state"]])
if( is.null(INPUT[["general"]][["redExp"]])){
	x = get_usr_chosen_response(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])],
						 INPUT[["general"]][["dataset"]], 
						params)
}else{
	cat("getting lars response for biclusters \n")
	x = get_usr_chosen_response(INPUT[["general"]][["colMap"]][-length(INPUT[["general"]][["colMap"]])],
			INPUT[["general"]][["redExp"]], 
			params)
}
						 
INPUT[["lars"]][["response_matrix_steady_state"]] = x[[1]]
INPUT[["lars"]][["response_matrix_time_series"]] = x[[2]]

# make final design/response matrices for clr
x = make_final_design_and_response_matrix(INPUT[["clr"]][["design_matrix_steady_state"]] , 
												  INPUT[["clr"]][["design_matrix_time_series"]] , 
												  INPUT[["clr"]][["response_matrix_steady_state"]], 
												  INPUT[["clr"]][["response_matrix_time_series"]], 
												  PARAMS[["clr"]][["what_final_design_response_matrix"]]) 
												  
INPUT[["clr"]][["response_matrix"]] = x[[1]]
INPUT[["clr"]][["design_matrix"]] = x[[2]]

# make final design/response matrices for lars
x = make_final_design_and_response_matrix(INPUT[["lars"]][["design_matrix_steady_state"]] , 
												  INPUT[["lars"]][["design_matrix_time_series"]] , 
												  INPUT[["lars"]][["response_matrix_steady_state"]], 
												  INPUT[["lars"]][["response_matrix_time_series"]], 
												  PARAMS[["lars"]][["what_final_design_response_matrix"]]) 

INPUT[["lars"]][["response_matrix"]] = x[[1]]
INPUT[["lars"]][["design_matrix"]] = x[[2]]

# remove helper variables
rm(x,params)









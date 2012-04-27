## Initialization for main.R

initialize <- function() {
	if( Sys.getenv( "HOST" )[[ 1 ]] == "Phaedra.systemsbiology.net" ) .libPaths("~/Library/R/library")
	#library(mva)
	library(MASS)
	##library(sna)
	source("R_scripts/sna.R")  ## put code straite in ... lib was not properly maintained

	library(lars)   ## least angle regression
	if (lars.use.prob == T){
		source("R_scripts/lars_probs.R")    ## lars with prior probs
		source("R_scripts/cor_prob.R")      ## dependancy for lars_probs -calculates p-vals
		source("R_scripts/cv_lars_probs.R") ##cv.lars rewritten to use lars_probs
	}

# source("R_scripts/networkProps.R")
	source("R_scripts/biclustUtil.R")
#	source("R_scripts/getCommandArgsUtil.R")
	source("R_scripts/regulatoryInf.R")
	source("R_scripts/normRedExp.R")  ## for glm ... not necc/ a good thing
	source("R_scripts/reduceReg.R")
	source("R_scripts/addLonerTFs.toClusterStack.R")
	source("R_scripts/microArray-util.R")
	source("R_scripts/util.R")
	source("R_scripts/infer.one.cluster.R")
	#source("R_scripts/snow-util.R")
	source("R_scripts/preClusterRegInfs.R")

	return( 5 )
}

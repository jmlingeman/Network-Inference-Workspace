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

# this main function implements the type of analysis that we have performed for DREAM4
# It will also work on the DREAM3 dataset, and DREAM2.
# one known bug is that DREAM2 net 1 run fails as Input .RData files are corrupt.
# If you have more than one processor on your machine please install package multicore
# and change init.R PARAMS[["general"]][["processorsNumber"]] to the number of processors
# you want to dedicate for the run (important when bootstraping).
# please report bugs to the inferelator google group

rm(list=ls())

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 1- reads params, design and response matrices, found in PARAMS and INPUT list respectively
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	source("./inferelator_launch_script.R")
	#making directory to save everything into
	system(paste("mkdir",PARAMS$general$saveToDir,sep=" "))
	#params for main (object PARAMS defined in init.R)
	b = 1 # this follow current iteration/bootstrap number
	N_b = PARAMS$"general"$"numBoots" # number of bootstraps
	btch_size = 10 # calculate this mumber of genes to all predictors MI scores in batches (to avoid running out of memory when calculating MI)
	percentCoverage <- PARAMS[["general"]][["percentCoverage"]] # (usually 100) percent of matrix that we want to resample
	lambda = PARAMS[["lars"]][["lambda"]] # set of l2 norm regularization weights to try in elastic net
	#chalName <- strsplit(PARAMS$general$data,"_")[[1]][1]
	numNet <- strsplit(PARAMS$general$data,"_")[[1]][2]
	#numGenesInNet <- strsplit(PARAMS$general$data,"_")[[1]][3]
    numGenesInNet <- "10"
    chalName <- "DREAM4"
	fName <- paste(chalName,"_Bonneau_InSilico_Size",numGenesInNet,"_",numNet,".txt",sep="")
	cleanUp <- FALSE # clear functions and other intermediate variables at end of run (leaves important variables more visible for end users)
	rm(numNet)
	# response and design matrices for clr
	Y_clr = INPUT[["clr"]][["response_matrix"]]
	X_clr = INPUT[["clr"]][["design_matrix"]] # single predictors
	# response and design matrices for lars
	Y_lars = INPUT[["lars"]][["response_matrix"]]
	X_lars = INPUT[["lars"]][["design_matrix"]] # single predictors
	# store results (ODEs,Z scores, and error for each model for each bootstrap run, respectively)
	betaList = vector("list", N_b)
	modelErrorList = vector("list", N_b)
	#startTime <- date() #times how long a run takes
	allResults <- list() #list for storing all models

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 2- calculate Median corrected Zscores based on KO data
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	#splitting the data into X_ts, X_wt, X_ko, X_kd
	X_All <- splitDreamDataByType(INPUT$general$dataset)
	X_ts <- X_All[[1]]
	X_ko <- X_All[[2]]
	X_kd <- X_All[[3]]
	X_wt <- X_All[[4]]
	#if( chalName == "DREAM4" ){
		## cols correspond to the intial conditions of the time series are indpndnt observations of w.t.
		## use this observations to calculate median w.t. exprsion
		#X_tZero <- INPUT$general$dataset[, grep("delt_0",colnames( INPUT$general$dataset )) ]
		## note x_wt is a vector median w.t. values (for each gene) while X_wt is the matrix used to calculate x_wt
		#x_wt = apply(cbind(X_wt ,X_ko , X_tZero, X_kd),1,median)
	#} else if (chalName == "DREAM3" || chalName == "DREAM2"){
		## t0 of time series is not an independent w.t. measurement and thus not used to estimate median w.t.
		#x_wt = X_wt # in dream3 we have only one observation for wt
	#} else {
		#stop("this main is only for DREAM2/3/4 datasets.")
	#}
	#if( numGenesInNet == 10 ){
		##the return from this function is a list, with 1) z-scores, and 2) standard deviations
		#allSds <- calcZscores( X_ko, X_kd, x_wt, sigmaZero=.05, numRem= 0, TRUE )[[2]]
		#X_ko_z <- calcZscores( X_ko, X_kd, x_wt, sigmaZero= median(allSds)/2, numRem= 0, TRUE )[[1]]
		#rm(allSds)
	#}else{
		##the return from this function is a list, with 1) z-scores, and 2) standard deviations
		#X_ko_z <- calcZscores( X_ko, X_kd, x_wt, sigmaZero=0, numRem= 0, FALSE )[[1]]
		#X_kd_z <- calcZscores( X_kd, X_ko, x_wt, sigmaZero=0, numRem= 0, FALSE )[[1]]
	#}
	#diag(X_ko_z) <- 0

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 3- setup for bootstrap: create Pi-perm_vector/matrix, Y^pi,X^pi
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

while (b <= 1) {
	#create permutation matrix
	cat("bootsrap #: ",b,"\n")
	if(b == 1){
		#here we want the original permutation, ie. getOrigPerm = TRUE (i.e. first bootstrap is exact dataset, no resampling)
		Pi_s_clr=createPermMatrix(cS=INPUT[["general"]][["clusterStack"]], allConds = colnames(Y_clr), getOrigPerm = TRUE, percentCoverage = percentCoverage)
		Pi_s_lars=createPermMatrix(cS=INPUT[["general"]][["clusterStack"]], allConds = colnames(Y_lars), getOrigPerm = TRUE, percentCoverage = percentCoverage)
	} else {
		Pi_s_clr=createPermMatrix(cS=INPUT[["general"]][["clusterStack"]], allConds = colnames(Y_clr), getOrigPerm = FALSE, percentCoverage = percentCoverage)
		Pi_s_lars=createPermMatrix(cS=INPUT[["general"]][["clusterStack"]], allConds = colnames(Y_lars), getOrigPerm = FALSE, percentCoverage = percentCoverage)
	}
	#create bicluster specific permutation matrix (ie. read from Pi_g, algorithm described in method comments)
	#this should be changed to be general for both cases where we have only single genes and cases where we havee biclusters
	Y_clr_p = permuteCols(Y_clr,Pi_s_clr)
	X_clr_p = permuteCols(X_clr,Pi_s_clr)

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 4- pass one: fill M - mutual information matrix or correlation matrix
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	# dynamic MI scores stored here
	cat("calculating dynamic MI ")
	if(PARAMS[["general"]][["processorsNumber"]] > 1){
		Ms = calc_MI_one_by_one_parallel( Y_clr_p, X_clr_p, Pi_s_clr, processorsNumber = PARAMS[["general"]][["processorsNumber"]])
	} else {
		Ms = calc_MI_inBatces(Y_clr_p,X_clr_p,btch_size)
	}
	diag(Ms) = 0
	cat("\n")
	# static MI scores stored here
	cat("calculating background MI ")
	if(PARAMS[["general"]][["processorsNumber"]] > 1){
		Ms_bg = calc_MI_one_by_one_parallel( X_clr_p, X_clr_p, Pi_s_clr, processorsNumber = PARAMS[["general"]][["processorsNumber"]])
	} else {
		Ms_bg = calc_MI_inBatces(X_clr_p,X_clr_p,btch_size)
	}
	diag(Ms_bg) = 0
	cat("\n")

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 5- calculate mixed-CLR (or clr) matrix
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	if(PARAMS[["general"]][["use_mixCLR"]]){
		Z_nt_fltrd = mixed_clr(Ms_bg,Ms)
	} else {
		Z_nt_fltrd = clr(Ms)
	}
    Z_nt_fltrd <- matrix(1,nrow(Z_nt_fltrd),ncol(Z_nt_fltrd))
	colnames(Z_nt_fltrd) <- rownames(X_clr)
	Z_nt_fltrd <- Z_nt_fltrd[,INPUT[["general"]][["tf_names"]]]
	#Z_ko <- abs(X_ko_z)
	#colnames(Z_ko) <- rownames(Z_ko)
	#rownames(Z_nt_fltrd) <- rownames(Z_ko)

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 6- apply MCZ filter -- i.e. remove unlikely reg inters from further consideration by mixedCLR (and thus from Inf)
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	# filter cutoff
	#ct = PARAMS[["general"]][["MCZ_fltr_prcntile"]]
	Z = Z_nt_fltrd
	#Z[which(Z_ko<quantile(Z_ko,ct))] = 0

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 7- run Inferelator (elastic net with ODE based modifications to response and design matrices)
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	# bug fix: lnet later doesn't work unless it has at least two predictors for each trgt
	# 	next for loop makes sure two preds have a non-zero confidence score in Z (MCZ after filtration)
	for (i in 1: dim(Z)[1]){
		if(length(which(Z[i,]>0))==0){
			Z[i,sort(Z_nt_fltrd[i,],index.return = TRUE,decreasing = TRUE)$ix[1]] = sort(Z_nt_fltrd[i,],decreasing = TRUE)[1]
		}
		if(length(which(Z[i,]>0))==1){
			if (sort(Z_nt_fltrd[i,],index.return = TRUE,decreasing = TRUE)$x[2] == Z[i,which(Z[i,]>0)]){
				Z[i,sort(Z_nt_fltrd[i,],index.return = TRUE,decreasing = TRUE)$ix[1]] = sort(Z_nt_fltrd[i,],decreasing = TRUE)[1]
			} else {
				Z[i,sort(Z_nt_fltrd[i,],index.return = TRUE,decreasing = TRUE)$ix[2]] = sort(Z_nt_fltrd[i,],decreasing = TRUE)[2]
			}
		}
	}
	#rm(ct)
	# run lnet and get predictive models for each target gene as a function of regulators
	# 	returns a list with the sparse beta (weights) matrix and the cross validation errors associated with each target gene model
    print(X_lars)
    print(Y_lars)
    print(Pi_s_lars)
    print(Z)
    print(lambda)
	cat("running elasticnet ")
	if(PARAMS[["general"]][["processorsNumber"]] > 1){
		x = calc_ode_model_weights_parallel(Xs = X_lars,Y = Y_lars, Pi = Pi_s_lars, M1 = Z, nS = PARAMS[["lars"]][["max_single_preds"]],
								 lambda=lambda, processorsNumber = PARAMS[["general"]][["processorsNumber"]], plot.it = FALSE,
								 plot.file.name = paste(PARAMS$general$saveToDir,"/boot_",b,"_models.pdf",sep=""),verbose = FALSE)
	} else {
		x = calc_ode_model_weights(Xs = X_lars,Y = Y_lars, Pi = Pi_s_lars, M1 = Z, nS = PARAMS[["lars"]][["max_single_preds"]], lambda=lambda,
								 plot.it = PARAMS$general$plot.it,plot.file.name = paste(PARAMS$general$saveToDir,"/boot_",b,"_models.pdf",sep=""),verbose = FALSE)
	}
	cat("\n")
	betaList[[b]] = x[[1]]
	modelErrorList[[b]] = t(x[[2]])
	betaList[[b]]=add_weight_beta(betaList=betaList[[b]],model_errors=modelErrorList[[1]],n=nrow(Y_lars),pS=nrow(X_lars),pD=0,col=4,col_name = "prd_xpln_var" )
	#betaList[[b]]=add_zscore(bL=betaList[[b]],M1=Z,M2=NULL,col=5,col_name = "clr_zs")
	#betaList[[b]]=add_zscore(bL=betaList[[b]],M1=Z_ko,M2=NULL,col=6,col_name = "ko_zs")
	betaList[[b]]=add_bias_term(bL=betaList[[b]],bT=t(x[[3]]),col=7,col_name = "bias")
	# betaList holds (for each bootstrap, b) all reg inters founds together with a bunch of confidence values: specifically
	# betaList[[b]] is a matrix with rows representing each reg inter. col_1=trgt,col_2=regulator,col_3=beta weigth,col_4=portion of xplnd var,
	#	col_5=mixCLR score, col_6=abs(MCZ score), col_7=bias term associated with each reg inter (the intercept for each trgt regression model)
	rm(x)

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 8- run heuristic to combine results from different methods (MCZ, mixCLR, and Inf)--- i.e. results from different pipelines
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	# elastic net produces a model for each l2 norm weight (choose a model for each target from the l2 norm weight with minimum CV error)
	beta.mat = combine_l2_net_res(betaList[[b]],modelErrorList[[b]],col="beta")
	# beta list is a sparse matrix representation.  Turn it into a matrix
	beta.mat = unsparse(beta.mat ,matrix(0,dim(Z)[1],dim(Z)[2]) )
	# same as beta.mat only instead of having beta weight as values it has predictive value for each reg inter
	pred.mat.lnet = combine_l2_net_res(betaList[[b]],modelErrorList[[b]],col="prd_xpln_var")
	pred.mat.lnet = unsparse(pred.mat.lnet,matrix(0,dim(Z)[1],dim(Z)[2]) )
	# for each trgt get the bias term (needed to predict system's response to new perturbations)
	pred.mat.bias = combine_l2_net_res(betaList[[b]],modelErrorList[[b]],col="bias")
	# this is the heuristic described in DREAM3 and DREAM4 papers z = sqrt(z1^2+z2^2)^2
	#  first for DREAM3 pipeline (not additive with MCZ)
    pred.mat.lnet.mixCLR = combine_mtrcs(Z,pred.mat.lnet)
	#  second for DREAM4 pipeline (i.e. DREAM3 + MCZ)
	#pred.mat.lnet.mixCLR.zKo = combine_mtrcs(Z_ko,pred.mat.lnet.mixCLR)

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 9- for DREAM4 predict systems response to double ko's
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	#if( chalName == "DREAM4" ){
		#S = pred.mat.lnet.mixCLR.zKo

		##for a detailed description of each input the function below see utils.R. Here, we point out that the last parameter determines
		##which initial conditions are used. The choices are:
		##		"origWt" - use wt values(whichever way we calculate them) as null predictions
		##   "koMean" - take columns i,j of single_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction
		##   "combine" - combine columns i,j of single_ko as a weighted sum based on sores of i,j in S
		#pred <- makePredictions( S,pred.mat.bias[,3],beta.mat,X_ko,X_wt, pred.mat.lnet, inCut <- 75,INPUT$general$dbl_ko,"combine")

		##similarly, the final parameter defines how the baselines is calculated. Choices are:
		##   "origWt" - use wt values(whichever way we calculate them) as null predictions
		##   "meanKo" - take columns i,j of dream_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction
		#predRes <- calcDblKoRmsd(pred,INPUT$general$dblKo_goldStandard,INPUT$general$dbl_ko,X_ko,X_wt,"meanKo")
	#}

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 10- store current re-sampling results
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	#allResults[[b]] <- list()
	#allResults[[b]][["MCZ"]] <- X_ko_z
	#allResults[[b]][["betaMat"]] <- beta.mat
	#allResults[[b]][["bias"]] <- pred.mat.bias
	#allResults[[b]][["MixCLR.Inf"]] <- pred.mat.lnet.mixCLR
	#allResults[[b]][["MCZ.MixCLR.Inf"]] <- pred.mat.lnet.mixCLR.zKo
	#if( chalName == "DREAM4"){
		#allResults[[b]][["pred"]] <- pred
		#allResults[[b]][["predRes"]] <- predRes
	#}
	## every saveInt runs save data as .RData file
	#if( N_b < 10 ){
		#saveInt <- 10
	#}else{
		#saveInt <- 50
	#}
	#if( b %% saveInt == 0){
		  #cat("saving! \n")
			##saveAndPlot(makePlots = FALSE, bootNum <- b, PARAMS$general$saveToDir )
			#save(betaList,INPUT,PARAMS,N_b, b, allResults, file=paste(PARAMS$general$saveToDir,"/savedData_numBoots_",b,".RData",sep=""))
			#prevBootNum <- b - saveInt
			##the line below is specific for linux based systems (ie. NOT WINDOWS)
			#system(paste("rm ", paste(PARAMS$general$saveToDir,"/savedData_numBoots_",prevBootNum,".RData",sep=""), sep=""))
	#}

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 11- while b<N_b increament b by 1 adn repeat steps 2-6
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	b = b + 1
}
rownames(Z_nt_fltrd) <- colnames(Z_nt_fltrd)
print(pred.mat.lnet)

write.table(pred.mat.lnet.mixCLR, file=PARAMS$general$output_file, sep="\t", col.names=T, row.names=T)



##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 12- evaluate results based on gold standards
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	## MCZ predictions (this is the same for each bootstrap)
	#res.MCZ <- evaluate(abs(allResults[[1]][["MCZ"]]),INPUT$general$gold_standard)
	#AUPR.MCZ <- res.MCZ$AUPR
	#res.MixCLR.Inf <- evaluate(allResults[[1]][["MixCLR.Inf"]],INPUT$general$gold_standard)
	#AUPR.MixCLR.Inf <- res.MixCLR.Inf$AUPR
	#res.MCZ.MixCLR.Inf <- evaluate(allResults[[1]][["MCZ.MixCLR.Inf"]],INPUT$general$gold_standard)
	#AUPR.MCZ.MixCLR.Inf <- res.MCZ.MixCLR.Inf$AUPR
	## calc bootstrap results for MixCLR.Inf and MCZ.MixCLR.Inf pipelines
	#if (N_b >1) {
		##   MCZ.MixCLR.Inf is bootstrap dependent: the purpose of the next three steps is to
		## 	get a mtrix of median MCZ.MixCLR.Inf for each interaction
		#median.conf.scores <- getMedianNetworkFromBootstraps(allResults, "MixCLR.Inf")
		#res.MixCLR.Inf.boot <- evaluate(median.conf.scores,INPUT$general$gold_standard)
		#AUPR.MixCLR.Inf.boot <- res.MixCLR.Inf.boot$AUPR
		#median.conf.scores <- getMedianNetworkFromBootstraps(allResults, "MCZ.MixCLR.Inf")
		#res.MCZ.MixCLR.Inf.boot <- evaluate(median.conf.scores,INPUT$general$gold_standard)
		#AUPR.MCZ.MixCLR.Inf.boot <- res.MCZ.MixCLR.Inf.boot$AUPR
	#}

	##if the user wants plots to be generated, the code below will
	##generate AUPR and AUROC curves
	#if(PARAMS$general$plot.it){
		#resList <- list()
		#resList[["MCZ"]] <- res.MCZ
		#resList[["tlclr_inf"]] <- res.MixCLR.Inf
		#resList[["MCZ plus tlclr_inf"]] <- res.MCZ.MixCLR.Inf
		#if(N_b > 1){
			#resList[["tlclr_inf (bootstrapped)"]] <- res.MixCLR.Inf.boot
			#resList[["MCZ plus tlclr_inf (bootstrapped)"]] <- res.MCZ.MixCLR.Inf.boot
		#}
		#plotAUPRCurves(resList, PARAMS$general$saveToDir)
	#}

	###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
## 13- making predictions of response of the system to double knocouts based on the median of the betas from the ensemble
	###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
	#if( chalName == "DREAM4" ){
		#medBeta <- getMedianNetworkFromBootstraps(allResults, "betaMat")
		#medBias <- getMedianBias(allResults)

		##for a detailed description of each input the function below see utils.R. Here, we point out that the last parameter determines
		##which initial conditions are used. The choices are:
		##		"origWt" - use wt values(whichever way we calculate them) as null predictions
		##   "koMean" - take columns i,j of single_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction
		##   "combine" - combine columns i,j of single_ko as a weighted sum based on sores of i,j in S
		#ensPred <- makePredictions( S,medBias,medBeta,X_ko,X_wt, pred.mat.lnet, inCut <- 75,INPUT$general$dbl_ko,"combine")

		##similarly, the final parameter defines how the baselines is calculated. Choices are:
		##   "origWt" - use wt values(whichever way we calculate them) as null predictions
		##   "meanKo" - take columns i,j of dream_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction
		#ensPredRes <- calcDblKoRmsd(pred,INPUT$general$dblKo_goldStandard,INPUT$general$dbl_ko,X_ko,X_wt,"meanKo")

		#if(PARAMS$general$plot.it){
			#plotDblKoPreds(allResults, ensPredRes, INPUT$general$dbl_ko, PARAMS$general$saveToDir)
		#}
	#}
###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
## 14- print results
###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	#cat("##############################\n")
	#cat("area under precision recall:\n")
	#cat("MCZ =",AUPR.MCZ,"\n")
	#cat("tlclr_inf =",AUPR.MixCLR.Inf,"\n")
	#if (N_b >1) { cat("tlclr_inf (bootstrapped) =",AUPR.MixCLR.Inf.boot,"\n") }
	#cat("MCZ plus tlclr_inf =",AUPR.MCZ.MixCLR.Inf,"\n")
	#if (N_b >1) { cat("MCZ plus tlclr_inf (bootstrapped) =",AUPR.MCZ.MixCLR.Inf.boot,"\n") }
	#cat("------------------------------\n")
	#cat("for more results check variables: res.MCZ (for MCZ alone) and res.MCZ.plus.tlclr_inf (for MCZ + tlCLR->Inferelator)\n")
	#cat("##############################\n")

###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
## 15- cleanup tmp variables functions
###  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.

	#if (cleanUp) {
		#rm(numGenesInNet,make_final_design_and_response_matrix,add_bias_term,add_weight_beta,add_zscore,calc_MI_inBatces,calc_ode_model_weights,
			#calcDblKoRmsd,calcFoldChange,calcZscores,create_Pi_g,create_Pi_s,create_Xpi,createPermMatrix,fName,get_all_perms,get_best_preds_idx,
			#get_usr_chosen_dataset,get_usr_chosen_design_matrix,get_usr_chosen_response,let_usr_choose_dataset,let_usr_choose_design_matrix,
			#let_usr_choose_response,load_gold_standard,load_predictions,make_sparse2,makePredictions,modelErrorList,percentCoverage,permuteCols,
			#Pi_s_clr,Pi_s_lars,saveInt,splitDreamDataByType,btch_size)
	#}

##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
# 2- calculate Median corrected Zscores based on KO data
##  .-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.***.-.-.
    source('utils.R')

    args <- commandArgs(trailingOnly=TRUE)
    X_wt = t(data.matrix(read.table(args[[1]], header=TRUE)))
    X_ko = data.matrix(read.table(args[[2]], header=TRUE, row.names=1))
    if (args[[3]] != "NULL") {
      X_kd = data.matrix(read.table(args[[3]], header=TRUE, row.names=1))
    } else {
      X_kd = NULL
    }
    if (args[[4]] != "NULL") {
      X_tZero = data.matrix(read.table(args[[4]], header=TRUE, row.names=1))
    } else {
      X_tZero = NULL
    }

    chalName = "DREAM4"

    print(X_wt)
    print(X_ko)
    print(X_kd)
    print(X_tZero)

	#splitting the data into X_ts, X_wt, X_ko, X_kd
	#X_All <- splitDreamDataByType(INPUT$general$dataset)
	#X_ts <- X_All[[1]]
	#X_ko <- X_All[[2]]
	#X_kd <- X_All[[3]]
	#X_wt <- X_All[[4]]
	if( chalName == "DREAM4" ){
		# cols correspond to the intial conditions of the time series are indpndnt observations of w.t.
		# use this observations to calculate median w.t. exprsion
		#X_tZero <- INPUT$general$dataset[, grep("delt_0",colnames( INPUT$general$dataset )) ]
		# note x_wt is a vector median w.t. values (for each gene) while X_wt is the matrix used to calculate x_wt
        if (!is.null(X_kd) & !is.null(X_tZero)) {
		  x_wt = apply(cbind(X_wt ,X_ko , X_tZero, X_kd),1,median)
        } else if (!is.null(X_kd) & is.null(X_tZero)) {
		  x_wt = apply(cbind(X_wt ,X_ko , X_kd),1,median)
        } else if (is.null(X_kd) & !is.null(X_tZero)) {
		  x_wt = apply(cbind(X_wt ,X_ko , X_tZero),1,median)
        } else {
		  x_wt = apply(cbind(X_wt ,X_ko),1,median)
        }
	} else if (chalName == "DREAM3" || chalName == "DREAM2"){
		# t0 of time series is not an independent w.t. measurement and thus not used to estimate median w.t.
		x_wt = X_wt # in dream3 we have only one observation for wt
	} else {
		stop("this main is only for DREAM2/3/4 datasets.")
	}
	#if( numGenesInNet == 10 ){
		##the return from this function is a list, with 1) z-scores, and 2) standard deviations
        if (!is.null(X_kd)) {
          allSds <- calcZscores( X_ko, X_kd, x_wt, sigmaZero=.05, numRem= 0, TRUE )[[2]]
          print("All SDs calculated.  Getting ZScores.")
          X_ko_z <- calcZscores( X_ko, X_kd, x_wt, sigmaZero= median(allSds)/2, numRem= 0, TRUE )[[1]]
        } else {
          allSds <- calcZscores( X_ko, X_kd, x_wt, sigmaZero=.05, numRem= 0, FALSE )[[2]]
          print("All SDs calculated.  Getting ZScores.")
          X_ko_z <- calcZscores( X_ko, X_kd, x_wt, sigmaZero= median(allSds)/2, numRem= 0, FALSE )[[1]]
        }

        #rm(allSds)
	#}else{
		#the return from this function is a list, with 1) z-scores, and 2) standard deviations
        #X_ko_z <- calcZscores( X_ko, X_kd, x_wt, sigmaZero=0, numRem= 0, FALSE )[[1]]
        #X_kd_z <- calcZscores( X_kd, X_ko, x_wt, sigmaZero=0, numRem= 0, FALSE )[[1]]
	#}
	diag(X_ko_z) <- 0
    print(X_ko_z)
    #print(allSds)
    write.table(X_ko_z, "../output/mcz_output.txt", row.names=TRUE, col.names=TRUE, sep="\t")

# given an expression matrix, create a trivial cluster stack - no (bi)clusters
trivial.cluster.stack <- function(exp.mat) {
  clusterStack <- list()
  for (i in 1:nrow(exp.mat)) {
    clusterStack[[i]] <- list(cols=NA, ncols=NA, rows=NA, nrows=NA, resid=NA, k=NA, redExp=NA)
    clusterStack[[i]]$cols <- colnames(exp.mat)
    clusterStack[[i]]$ncols <- ncol(exp.mat)
    clusterStack[[i]]$rows <- rownames(exp.mat)[i]
    clusterStack[[i]]$nrows <- 1
    clusterStack[[i]]$k <- i
    clusterStack[[i]]$redExp <- exp.mat[i, ]
  }
  return(clusterStack)
}

# given the condition names, create meta data data frame that assumes all
# observations are steady state measurements
trivial.meta.data <- function(cond.names) {
  meta.data <- data.frame(condName=cond.names)
  meta.data$isTs <- FALSE
  meta.data$is1stLast <- 'e'
  meta.data$prevCol <- NA
  meta.data$del.t <- NA
  return(meta.data)
}

read.input <- function(input.dir, exp.mat.file, tf.names.file, meta.data.file,
                       priors.file, gold.standard.file, leave.out.file) {
  IN <- list()

  if (grepl('.RData$', exp.mat.file)) {
    IN$exp.mat <- local(get(load(file.path(input.dir, exp.mat.file))))
  } else {
    IN$exp.mat <- as.matrix(read.table(file=file.path(input.dir, exp.mat.file),
                            row.names=1, header=T, sep='\t', check.names=F))
  }
  IN$tf.names <- as.vector(as.matrix(read.table(file.path(input.dir, tf.names.file))))

  IN$meta.data <- NULL
  if (!is.null(meta.data.file)) {
    IN$meta.data <- read.table(file=file.path(input.dir, meta.data.file),
                               header=T, sep='\t')
  }

  # if there is a leave-out file, ignore some conditions
  if (!is.null(leave.out.file)) {
    leave.out <- as.vector(as.matrix(read.table(file.path(input.dir, leave.out.file))))
    cat('Leaving out the following conditions:', leave.out, '\n')
    lo <- colnames(IN$exp.mat) %in% leave.out
    IN$exp.mat.lo <- as.matrix(IN$exp.mat[, lo])
	print(leave.out)
    print(lo)
    print(IN$exp.mat.lo)
	colnames(IN$exp.mat.lo) <- leave.out
    IN$meta.data.lo <- IN$meta.data[lo, ]
    IN$exp.mat <- as.matrix(IN$exp.mat[, !lo])
    IN$meta.data <- IN$meta.data[!lo, ]
  }

  IN$priors.mat <- NULL
  if (!is.null(priors.file)) {
    IN$priors.mat <- as.matrix(read.table(file=file.path(input.dir, priors.file),
                                    row.names=1, header=T, sep='\t', check.names=F))
  }
  IN$gs.mat <- NULL
  if (!is.null(gold.standard.file)) {
    IN$gs.mat <- as.matrix(read.table(file=file.path(input.dir, gold.standard.file),
                                    row.names=1, header=T, sep='\t', check.names=F))
  }
  return(IN)
}

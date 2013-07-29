require(Matrix)

ChristophsPR <- function(ord.idx, gs) {
  prec <- cumsum(gs[ord.idx]) / cumsum(rep(1, length(ord.idx)))
  rec <- cumsum(gs[ord.idx]) / sum(gs)

  prec <- c(prec[1], prec)
  rec <- c(0, rec)

  auc <- ChristophsAUC(rec, prec)
  return(list(prec=prec, rec=rec, auc=auc))
}


ChristophsAUC <- function(x, y) {
  dx <- diff(x)
  my <- y[1:(length(y) - 1)] + diff(y) / 2
  return(sum(dx * my))
}


aupr <- function(mat, gs, eval.on.subset=FALSE) {
  rows <- rep(TRUE, nrow(gs))
  cols <- rep(TRUE, ncol(gs))
  if (eval.on.subset) {
    rows <- apply(gs, 1, sum) > 0
    cols <- apply(gs, 2, sum) > 0
  }
  return(ChristophsPR(order(mat[rows, cols], decreasing=TRUE), gs[rows, cols])$auc)
}


summarizeResults <- function(full.dir, eval.on.subset) {
  params.and.input <- paste(full.dir, 'params_and_input.RData', sep='/')
  if (!file.exists(params.and.input)) {
    cat('No params_and_input.RData - skipping', full.dir, '\n')
    return()
  }
  load(params.and.input)
  files <- list.files(full.dir, "combinedconf_.+\\.RData$")
  print(file.path(full.dir, files))
  
  gs <- IN$gs.mat

  out <- matrix('', 0, 2)
  for (res.file in files) {
    load(file.path(full.dir, res.file))
    aupr.tot <- aupr(comb.confs, gs, eval.on.subset)
    out <- rbind(out, c(res.file, aupr.tot))      
  }

  # write results for this directory
  write.table(out, file=file.path(full.dir, 'auprs.tsv'), quote=FALSE, sep='\t', row.names=FALSE, col.names=FALSE)
}  


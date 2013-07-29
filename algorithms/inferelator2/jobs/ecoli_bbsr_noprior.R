PARS$input.dir <- 'input/ecoli'

PARS$meta.data.file <- 'meta.data.tsv'
PARS$priors.file <- 'gold_standard.tsv'
PARS$gold.standard.file <- 'gold_standard.tsv'

PARS$num.boots <- 20
PARS$cores <- 12

PARS$delT.max <- 270
PARS$delT.min <- 30
PARS$tau <- 20

PARS$perc.tp <- 0
PARS$perm.tp <- 1
PARS$perc.fp <- 0
PARS$perm.fp <- 1

PARS$eval.on.subset <- TRUE

PARS$method <- 'BBSR'
PARS$prior.weight <- NULL

PARS$save.to.dir <- paste('output/ecoli', PARS$method, PARS$prior.weight, sep='_')


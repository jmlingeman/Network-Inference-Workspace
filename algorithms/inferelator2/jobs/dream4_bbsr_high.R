PARS$input.dir <- 'input/dream4'

PARS$meta.data.file <- 'meta_data.tsv'
PARS$priors.file <- 'gold_standard.tsv'
PARS$gold.standard.file <- 'gold_standard.tsv'

PARS$num.boots <- 1
PARS$cores <- 4

PARS$delT.max <- 110
PARS$delT.min <- 0
PARS$tau <- 45

PARS$perc.tp <- 50
PARS$perm.tp <- 1
PARS$perc.fp <- 100
PARS$perm.fp <- 1

PARS$eval.on.subset <- FALSE

PARS$method <- 'BBSR'
PARS$prior.weight <- 2.8

PARS$save.to.dir <- paste('output/dream4', PARS$method, PARS$prior.weight, sep='_')

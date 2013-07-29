PARS$input.dir <- 'input/bsubtilis'

PARS$meta.data.file <- 'meta.data.tsv'
PARS$priors.file <- 'gold_standard.tsv'
PARS$gold.standard.file <- 'gold_standard.tsv'

PARS$num.boots <- 20
PARS$cores <- 12

PARS$delT.max <- 60
PARS$delT.min <- 15
PARS$tau <- 15

PARS$perc.tp <- rep(50, 4)
PARS$perm.tp <- rep(1, 4)
PARS$perc.fp <- c(0, 100, 250, 500)
PARS$perm.fp <- c(1, 5, 5, 5)

PARS$eval.on.subset <- TRUE

PARS$method <- 'BBSR'
PARS$prior.weight <- 1.6

PARS$save.to.dir <- paste('output/bsubtilis', PARS$method, PARS$prior.weight, sep='_')


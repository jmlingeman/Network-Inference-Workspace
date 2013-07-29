PARS$input.dir <- 'input/ecoli'

PARS$meta.data.file <- 'meta.data.tsv'
PARS$priors.file <- 'gold_standard.tsv'
PARS$gold.standard.file <- 'gold_standard.tsv'

PARS$num.boots <- 20
PARS$cores <- 12

PARS$delT.max <- 270
PARS$delT.min <- 30
PARS$tau <- 20

PARS$perc.tp <- rep(50, 4)
PARS$perm.tp <- rep(1, 4)
PARS$perc.fp <- c(0, 100, 250, 500)
PARS$perm.fp <- c(1, 5, 5, 5)

PARS$eval.on.subset <- TRUE

PARS$method <- 'MEN'
PARS$prior.weight <- 0.01

PARS$save.to.dir <- paste('output/ecoli', PARS$method, PARS$prior.weight, sep='_')


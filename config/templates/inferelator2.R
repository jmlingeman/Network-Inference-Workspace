PARS$input.dir <- '{{data_dir}}'

PARS$priors.file <- '{{prior_file}}'
if(PARS$priors.file == "NULL") {
    PARS$priors.file <- NULL
}
PARS$gold.standard.file <- '{{gold_standard}}'

if(PARS$gold.standard.file == "NULL") {
    PARS$gold.standard.file <- NULL
}

PARS$leave.out.file <- '{{leave_out}}'

if(PARS$leave.out.file == "NULL") {
    PARS$leave.out.file <- NULL
}

PARS$num.boots <- {{num_bootstraps}}
PARS$cores <- {{num_cores}}

PARS$delT.max <- {{delta_t_max}}
PARS$delT.min <- {{delta_t_min}}
PARS$tau <- {{tau}}

PARS$perc.tp <- {{perctp}}
PARS$perm.tp <- {{permtp}}
PARS$perc.fp <- {{percfp}}
PARS$perm.fp <- {{permfp}}

PARS$enet.nCv <- {{nCv}}

PARS$job.seed <- {{random_seed}}

PARS$eval.on.subset <- FALSE

PARS$method <- '{{method}}'
PARS$prior.weight <- {{prior_weight}}

PARS$save.to.dir <- paste('{{output_dir}}', "/output", sep="")



library(TDARACNE)

launch_tdaracne <- function(input_file, N, output_file, delta, likelihood, norm, logarithm, thresh, ksd, tolerance) {

    exprsFile <- file.path(input_file)
    exprs <- t(as.matrix(read.table(exprsFile, header = TRUE, as.is = TRUE)))
    print(exprs)
    minimalSet <- new("ExpressionSet", exprs = exprs)

    TDARACNE(minimalSet, N, name = output_file, delta = delta, likehood = likelihood, norm = norm, logarithm = logarithm, ksd = ksd, tolerance = tolerance, dot = TRUE, adj=TRUE, plot=TRUE)

}

options <- commandArgs(trailingOnly = T)
input_file <- options[1]
N <- as.numeric(options[2])
output_file <- options[3]
delta <- as.numeric(options[4])
likelihood <- as.numeric(options[5])
norm <- as.numeric(options[6])
logarithm <- as.numeric(options[7])
thresh <- options[8]
ksd <- as.numeric(options[9])
tolerance <- as.numeric(options[10])

outspt = strsplit(output_file, '/')
filename = outspt[[1]][length(outspt[[1]])]

launch_tdaracne(input_file, N, filename, delta, likelihood, norm, logarithm, thresh, ksd, tolerance)

file.rename(paste("net",filename,".dot",sep=''), output_file)

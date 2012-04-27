\name{brlr}
\alias{brlr}
\alias{print.brlr}
\alias{summary.brlr}
\alias{print.summary.brlr}
\alias{vcov.brlr}
% \alias{predict.brlr}
\alias{add1.brlr}
\alias{drop1.brlr}
\title{Bias-reduced Logistic Regression}
\description{
Fits a logistic regression by maximum penalized likelihood, in 
which the penalty function is the Jeffreys invariant prior.  This
removes the O(1/n) term from the asymptotic bias of estimated
coefficients (Firth, 1993), and always yields finite estimates
and standard errors (whereas the MLE is infinite in situations
of complete or quasi-complete separation).  Now deprecated, in
favour of \code{\link[brglm]{brglm}}.
}
\usage{
brlr(formula, data = NULL, offset, weights, start, ..., subset,
   dispersion = 1, na.action = na.omit, contrasts = NULL,
   x = FALSE, br = TRUE, control = list(maxit = 200))
}
\arguments{
  \item{formula}{ a model formula as for \code{\link{glm}}, or an
  object of class \code{glm} }
  \item{data}{ an data frame as for \code{\link{glm}} }
  \item{offset}{ an optional vector as for \code{\link{glm}} }
  \item{weights}{ an optional vector as for \code{\link{glm}} }
  \item{start}{ an optional set of starting values (of the model
                coefficients) for the optimization }
  \item{\dots}{ further arguments passed to or from other methods }
  \item{subset}{an optional vector specifying a subset of observations to be
          used in the fitting process }
  \item{dispersion}{an optional parameter for over- or under-dispersion
                    relative to binomial variation -- default is 1}
  \item{na.action}{a function which indicates what should happen when the data
          contain `NA's.  The default is set by the \code{na.action} setting
          of \code{options}, and is \code{na.fail} if that is unset.  The
          ``factory-fresh'' default is \code{na.omit}. }
  \item{contrasts}{ an optional list. See the \code{contrasts.arg} of
          \code{\link{model.matrix.default}}.}
  \item{x}{should the model matrix be included in the resultant object?}
  \item{br}{a logical switch indicating whether the bias-reducing 
    penalty is applied; default is \code{TRUE}}
  \item{control}{as for \code{\link{optim}}}
}
\details{
  \code{brlr} has essentially the same user interface as
  \code{glm(family=binomial, ...)} ---
  see the example below.

  As of version 0.9-0 (May 2008), the \code{brlr} function is
  \bold{deprecated}: 
  it is likely to be withdrawn at some time in the future.  This is because
  it is superseded by more general and more efficient facilities in
  \code{\link[brglm]{brglm}}, contributed to CRAN in 2008 by Ioannis Kosmidis.  
}
\value{
A model object of class \code{brlr}, with components
    	\item{coefficients}{as for \code{\link{glm}}}
    	\item{deviance}{as for \code{\link{glm}}}
        \item{penalized.deviance}{deviance minus 2*logdet(Fisher information)}
        \item{fitted.values}{as for \code{\link{glm}}}
        \item{linear.predictors}{as for \code{\link{glm}}}
        \item{call}{as for \code{\link{glm}}}
        \item{formula}{as for \code{\link{glm}}}
        \item{convergence}{logical, did the optimization converge?}
        \item{niter}{number of iterations of the optimization algorithm
                     (BFGS via \code{\link{optim}})}
        \item{df.residual}{as for \code{\link{glm}}}
        \item{df.null}{as for \code{\link{glm}}}
        \item{model}{as for \code{\link{glm}}}
        \item{y}{the observed binomial proportions, as for \code{\link{glm}}}
        \item{family}{a \code{family} object, binomial with logistic link, 
                          as for \code{\link{glm}}}
        \item{offset}{as for \code{\link{glm}}}
        \item{prior.weights}{as for \code{\link{glm}}}
        \item{terms}{as for \code{\link{glm}}}
        \item{dispersion}{as for \code{\link{glm}}; the
	  \code{dispersion} argument if supplied, otherwise 1}
        \item{bias.reduction}{logical, the value of argument \code{br}}
        \item{leverages}{the diagonal elements of the model's ``hat''
	  matrix}
	\item{qr}{as for \code{\link{glm}}}
	\item{rank}{as for \code{\link{glm}}}
	\item{FisherInfo}{the estimated Fisher information matrix}
	\item{contrasts}{as for \code{\link{glm}}}
	\item{xlevels}{as for \code{\link{glm}}}
	\item{residuals}{as for \code{\link{glm}}}
	\item{data}{as for \code{\link{glm}}}
	\item{boundary}{as for \code{\link{glm}}; but always \code{FALSE}}
	\item{x}{if \code{x = TRUE} is specified}
	\item{control}{the \code{control} list as used in the call to
	  \code{\link{optim}}}
}
\references{

Firth, D. (1993)  Bias reduction of maximum likelihood estimates.
              \emph{Biometrika} \bold{80}, 27--38.
              
Firth, D. (1992)  Bias reduction, the Jeffreys prior and GLIM. In 
\emph{Advances in GLIM and Statistical Modelling}, Eds. L Fahrmeir,
B J Francis, R Gilchrist and G Tutz, pp91--100.  New York: Springer.

Heinze, G. and Schemper, M. (2002)  A solution to the problem of
separation in logistic regression.  \emph{Statistics in Medicine} \bold{21},
2409--2419.

Zorn, C (2005).  A solution to separation in binary response models.
\emph{Political Analysis} \bold{13}, 157--170.}

\seealso{
            \code{\link[brglm]{brglm}}.
          }
	  
\author{David Firth, \email{d.firth@warwick.ac.uk}}
\note{
1.  Methods specific to the \code{brlr} class of models are 
\itemize{
  \item \code{print.brlr}
  \item \code{summary.brlr}
  \item \code{print.summary.brlr}
  \item \code{vcov.brlr}
%  \item \code{predict.brlr}
  \item \code{add1.brlr}
  \item \code{drop1.brlr}
}

Others are inherited from the \code{\link{glm}} class.

2.  The results of the bias-reduced fit typically have regression
coefficients slightly closer to zero than the maximum likelihood
estimates, and slightly smaller standard errors.  (In logistic
regression, bias reduction is achieved by a slight shrinkage
of coefficients towards zero; thus bias reduction also reduces
variance.)  The difference is typically small except in situations
of sparse data and/or complete separation.  See also Heinze and
Schemper (2002), Zorn (2005).
}

\examples{
\dontrun{
## Habitat preferences of lizards, from McCullagh and Nelder (1989, p129);
## this reproduces the results given in Firth (1992).
##
## First the standard maximum-likelihood fit:
data(lizards)
glm(cbind(grahami, opalinus) ~ height + diameter + light + time,
    family = binomial, data=lizards)
## Now the bias-reduced version:
brlr(cbind(grahami, opalinus) ~ height + diameter + light + time,
    data=lizards)
  }
}
\keyword{models}
\keyword{regression}

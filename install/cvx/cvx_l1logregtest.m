% Section 7.1.1
% Boyd & Vandenberghe, "Convex Optimization"
% Original by Lieven Vandenberghe
% Adapted for CVX by Argyris Zymnis - 01/31/06
%
% We consider a binary random variable y with prob(y=1) = p and
% prob(y=0) = 1-p. We assume that that y depends on a vector of
% explanatory variables u in R^n. The logistic model has the form
% p = exp(a'*u+b)/(1+exp(a'*u+b)), where a and b are the model parameters.
% We have m data points (u_1,y_1),...,(u_m,y_m).
% We can reorder the data so that for u_1,..,u_q the outcome is y = 1
% and for u_(q+1),...,u_m the outcome is y = 0. Then it can be shown
% that the ML estimate of a and b can be found by solving
%
% minimize sum_{i=1,..,q}(a'*u_i+b) - sum_i(log(1+exp(a'*u_i+b)))
%
% In this case we have m = 100 and the u_i are just scalars.
% The figure shows the data as well as the function
% f(x) = exp(aml*x+bml)/(1+exp(aml*x+bml)) where aml and bml are the
% ML estimates of a and b.

randn('state',0);
rand('state',0);

a =  1;
b = -5;

m = 100;
u = 10*rand(m,1);
y = (rand(m,1) < exp(a*u+b)./(1+exp(a*u+b)));

% order the observation data
ind_false = find( y == 0 );
ind_true  = find( y == 1 );

% X is the sorted design matrix
% first have true than false observations followed by the bias term
X = [u(ind_true); u(ind_false)];
X = [X ones(size(u,1),1)];
[m,n] = size(X);
q = length(ind_true);

size(X)
size(y)

lambda = 0.5

cvx_expert true
cvx_begin
    variables a(n) b
    minimize ( sum(log(1+exp(-y.*(X*a+b))) + lambda * norm(a,1) ) )
cvx_end

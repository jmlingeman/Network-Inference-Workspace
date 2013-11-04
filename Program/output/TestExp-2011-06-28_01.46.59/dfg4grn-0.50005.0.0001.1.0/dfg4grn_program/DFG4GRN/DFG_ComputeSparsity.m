% DFG_ComputeSparsity  Compute Hoyer sparsity measure of a vector or matrix
%
% Syntax:
%   sparsity = DFG_ComputeSparsity(w)
% Inputs:
%   w:        matrix or vector of <n> elements
% Output:
%   sparsity: (sqrt(n) - sqrt(||w||^2)/sum|w|) / (sqrt(n) - 1)
%
% Reference:
%   Patrik O. Hoyer, "Non-negative matrix factorization with sparseness
%   constraints", J. Mach. Learn. Res., vol. 5, pp. 1457-1469, 2004.
%   Niall Hurley and Scott Rickard, "Comparing measures of sparsity",
%   IEEE MLSP, 2008.

function sparsity = DFG_ComputeSparsity(w)

n = numel(w);
sparsity = ...
  (sqrt(n) - sum(sum(abs(w))) / sqrt(sum(sum(w .^ 2)))) / (sqrt(n) - 1);

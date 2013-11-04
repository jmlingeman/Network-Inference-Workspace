function X = normalize(X)
% NORMALIZE  Normalize the observations of a data matrix.
%    X = NORMALIZE(X) centers and scales the observations of a data
%    matrix such that each variable (column) has unit length.
%
% Author: Karl Skoglund, IMM, DTU, kas@imm.dtu.dk

[n p] = size(X);
X = center(X);
X = X./sqrt(ones(n,1)*sum(X.^2));

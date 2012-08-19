% DFG_TrendError  Trend error (sign and magnitude)
%
% Compare the change ("delta") over time in the original data <x>
% (i.e. change from x(t) to x(t+1))
% and in the predictions <xStar> (i.e. change from x(t) to x*(t+1))
% and express it as the percentage of correct signs, and as R^2 error
% (coefficient of determination).
% Do the evaluation only on selected variables <indVars>, and skip
% unknown measurements <xKnown> (i.e. when xKnown(n, t) = 0).
%
% Syntax:
%   [eSign, c] = DFG_TrendError(x, xStar, indVars, xKnown)
% Inputs:
%   x:       matrix of target variables of size <N> x <T>
%   xStar:   matrix of generated variables of size <N> x <T>
%   indVars: matrix of indicator variables (1=known) of size <N> x <T>
%            leave as empty in order to evaluate error on all genes
%   xKnown:  vector of selected variables (1=selected) of size <N>
% Output:
%   eSign    percentage of correct signs for the trend prediction
%   c:       R2 coefficient of determination of trend prediction

% Copyright (C) 2009 Piotr Mirowski
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with this program; if not, write to the Free Software
% Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
%
% Version 1.0, New York, 08 November 2009
% (c) 2009, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: compute trend error on all sequences at once

function [eSign, c] = DFG_TrendError(x, xStar, dXmean, indVars, xKnown)

% If we evaluate sign error on all genes, not only on a selection
n_genes = length(dXmean);
if isempty(indVars)
  indVars = 1:n_genes;
end

% Inputs <x>: remove non-selected variables, unknown elements, compute diff
if iscell(x)
  n_seq = length(x);
  dX = [];
  for j = 1:n_seq
    x{j} = x{j}(indVars, :);
    if (nargin > 4)
      xKnown{j} = xKnown{j}(indVars, :);
      x{j}(logical(~xKnown{j})) = nan;
    end
    dX = [dX diff(x{j}, 1, 2)];
  end
else
  x = x(indVars, :);
  if (nargin > 4)
    xKnown = xKnown(indVars, :);
    x(logical(~xKnown)) = nan;
  end
  dX = diff(x, 1, 2);
end

% Compute the deltas (along time) of the observed and predicted variables
if iscell(xStar)
  dXstar = [];
  for j = 1:n_seq
    xStar{j} = xStar{j}(indVars, :);
    dXstar = [dXstar (xStar{j} - x{j}(:, 1:(end-1)))];
  end
else
  xStar = xStar(indVars, :);
  dXstar = xStar - x(:, 1:(end-1));
end

% Mean gene expressions
dXmean = mean(dXmean(indVars));

% Set "unknown" observed deltas to 0
% and remove them from both the observed and predicted deltas
if ((nargin > 4) && any(any(xKnown == 0)))
  dXnan = isnan(dX);
  dX(dXnan) = 0;
  dXstar(dXnan) = 0;
end

% Set to 0 elements that are negligible
tol = 1e-3;
dX(abs(dX) < tol) = 0;
dXstar(abs(dXstar) < tol) = 0;

% Compute sign of trend error and coefficient of determination error
% for each time point
eSignMat = (sign(dX) == sign(dXstar));
eSign = mean(eSignMat, 1);
cNum = sum((dXstar - dX).^2, 1);
cDen = sum((dX - dXmean).^2, 1);
c = 1 - cNum ./ cDen;

% DFG_Dynamics_AR1_DistribDelta  Compute distribution of changes in <y>
%
% Syntax:
%   coeff_delta = DFG_Dynamics_AR1_DistribDelta(y, tau, deltaT)
% Inputs:
%   y:         matrix of size <N> x <T> of time series measurements
%   tau:       kinetic coefficient (in min)
%   deltaT:    matrix of size <1> x <T-1> of time steps (in min)
% Outputs:
%   coeff_delta: coefficient by which to multiply the output of a nonlinear
%                transfer function (that limits outputs between -1 and 1)
%                in order to get the variations "deltay" present in <y>
%                assuming a kinetic equation parameterized by <tau>,
%                and staying within 1 standard deviation

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

function coeff_delta = DFG_Dynamics_AR1_DistribDelta(y, tau, deltaT)

% Handle cell data
if ~iscell(y), y = {y}; end
if ~iscell(deltaT)
  n_seq = length(y);
  deltaTcell = cell(1, n_seq);
  for k = 1:n_seq
    deltaTcell{k} = deltaT;
  end
  deltaT = deltaTcell;
end
n_seq = length(y);
dim_y = size(y{1}, 1);

deltas = [];
deltaY = cell(1, n_seq);
min_alpha0 = inf;
max_alpha0 = -inf;
min_alpha1 = inf;
max_alpha1 = -inf;
for k = 1:n_seq
  % Compute the time coefficients
  [alpha0, alpha1] = ...
    DFG_Dynamics_AR1_ComputeKineticCoeff(deltaT{k}, tau, dim_y);
  min_alpha0 = min(min(min(min_alpha0, alpha0)));
  max_alpha0 = max(max(max(max_alpha0, alpha0)));
  min_alpha1 = min(min(min(min_alpha1, alpha1)));
  max_alpha1 = max(max(max(max_alpha1, alpha1)));

  % Select the "input" and "output" to the system
  yIn = y{k}(:, 1:(end-1));
  yOut = y{k}(:, 2:end);

  % Compute the "perturbation"
  % yOut = alpha0 * deltaY + alpha1 * yIn
  % alpha0 * deltaY = yOut - alpha1 * yIn
  deltaY{k} = (yOut - alpha1 .* yIn) ./ alpha0;
  deltas = [deltas deltaY{k}];
end
min_delta = min(deltas(:));
max_delta = max(deltas(:));

% Take twice the maximum absolute value of the delta
coeff_delta = 2 * max(max_delta, -min_delta);

if (nargout < 1)
  % Compute the distribution of these perturbations
  figure;
  hist(deltas', 20);
  title(sprintf('\\deltaY [%g, %g], \\alpha0 [%g, %g], \\alpha1 [%g, %g]', ...
    min_delta, max_delta, min_alpha0, max_alpha0, min_alpha1, max_alpha1));
end

% DFG_Dynamics_AR1_ComputeKineticCoeff  Compute the kinetic coefficients
%
% Compute the kinetic coefficients <alpha0> and <alpha1> for all times
% (which are matrices of <dim_z> rows by <T-1> time points)
% following equation: tau * dy/dt = -y + f(x)
%                     tau/Dt * (y(t+1) - y(t)) = -y + f(x)
%                     y(t+1) = y(t) * (1 - Dt/tau) + Dt/tau * f(x)
%                     y(t+1) = alpha1 * y(t) + alpha0 * f(x)
%
% Syntax:
%   [alpha0, alpha1] = ...
%     DFG_Dynamics_AR1_ComputeKineticCoeff(deltaT, tau, dim_z)
% Inputs:
%   deltaT: horizontal vector of <T-1> values of time differences
%   tau:    kinetic time coefficient
%   dim_z:  number of dynamical variables
% Outputs:
%   alpha0: matrix of size <T-1> x <dim_z> of "perturbation" coefficients
%           applied to TF influences
%   alpha1: matrix of size <T-1> x <dim_z> of auto-regressive coefficients
%           applied to the previous values of the time series
%
% References:
%   Wahde M, Hertz J, "Modeling genetic regulatory dynamics in neural
%   development", Journal of Computational Biology, 8:429-442 (2001)

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

% Revision 1: added handling of steady-state (alpha0 = 1, alpha1 = 0)

function [alpha0, alpha1] = ...
  DFG_Dynamics_AR1_ComputeKineticCoeff(deltaT, tau, dim_z)

% deltay = f(w * x + b)
% tau * dx/dt = -x + deltay
% tau * (y* - x) / deltaT = -x + deltay
% tau / deltaT * y* + x * (1 - tau / deltaT) = deltay
% y* + deltaT / tau * x * (1 - tau / deltaT) = deltaT / tau * deltay
% y* = (1 - deltaT / tau) * x + deltaT / tau * deltay
% y* = alpha1 * x + alpha0 * deltay
% deltay = (y* - alpha1 * x) / alpha0

if (length(deltaT) > 1)
  % Multiple steps
  ind = find(isnan(deltaT));
  % Kinetics
  alpha0 = repmat(deltaT / tau, dim_z, 1);
  alpha1 = repmat(1 - deltaT / tau, dim_z, 1);
  % Handle steady states
  if ~isempty(ind)
    alpha0(:, ind) = 1;
    alpha1(:, ind) = 0;
  end
else
  % Single time step
  if isnan(deltaT)
    % Steady state
    alpha0 = 1;
    alpha1 = 0;
  else
    % Kinetics
    alpha0 = deltaT / tau;
    alpha1 = 1 - deltaT / tau;
  end
end

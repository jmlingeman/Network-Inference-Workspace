% DFG_Dynamics_AR1_BackProp  Compute gradients of the dynamical energy
%
% Syntax:
%   [dE_dzIn, dE_dwDyna, dE_dbDyna] = ...
%     DFG_Dynamics_AR1_BackProp(zIn, zOut, dE_dzOut, model_num, ...
%     alpha0, alpha1, coeff_delta)
% Inputs:
%   zIn:         matrix of size <N> x <T-1> of input latent variables
%   zOut:        matrix of size <N> x <T-1> of output latent variables
%   dE_dzOut:    matrix of size <N> x <T-1> of gradients w.r.t. input
%                latent variables
%   model_num:   model number (default 1)
%   alpha0:      matrix of size <N> x <T> of coefficients applied to 
%                the "perturbation" modeling TF effects
%                (used only in the "kinetic" mode)
%   alpha1:      matrix of size <N> x <T> of coefficients applied to
%                the autoregressive component (previous time point)
%                (used only in the "kinetic" mode)
%   coeff_delta: coefficient applied to 'tanh' deltas
% Outputs:
%   dE_dzIn:   matrix of size <N> x <N> of the gradient of dynamical energy
%              w.r.t. dynamic weights
%   dE_dwDyna: matrix of size <N> x <N> of the gradient of dynamical energy
%              w.r.t. dynamic weights
%   dE_dbDyna: vector of size <N> x 1 of the gradient of dynamical energy
%              w.r.t. dynamic bias

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

function [dE_dzIn, dE_dwDyna, dE_dbDyna] = ...
  DFG_Dynamics_AR1_BackProp(zIn, zOut, dE_dzOut, model_num, ...
  alpha0, alpha1, coeff_delta)

global MODEL

% Do we predict the delta or the actual value?
% E = 1/2 * (y* - y)^2
% equivalent to: E = 1/2 * ((y* - x) - (y - x))^2
% dE/dy* = y* - y   (here equal to dE_dzOut)
switch (MODEL{model_num}.dynamic.pde_type)
  case 'delta'
    % deltay = f(w * x + b)
    % y* = x + f(w * x + b) = x + deltay
    % E = 1/2 * ((y* - x) - (y - x))^2 = 1/2 * (deltay - (y - x))^2
    % dE/ddeltay = deltay - y + x
    % dE/dx = dE/dy* * dy*/dx = (y* - y) * w * f'(w * x + b)
    % dE/dw = dE/dy* * dy*/dw = (y* - y) * x * f'(w * x + b)
    % dE/dx = dE/ddeltay * ddeltay/dx
    %       = (deltay - y + x) * w * f'(w * x + b)
    % dE/dw = dE/ddeltay * ddeltay/dw
    %       = (deltay - y + x) * x * f'(w * x + b)
    zOut = zOut - zIn;
    if isequal(MODEL{model_num}.dynamic.transfer, 'tanh')
      dE_dzOut = coeff_delta * dE_dzOut;
      zOut = zOut / coeff_delta;
    end
  case 'kinetic'
    % y* = alpha1 * x + alpha0 * f(w * x + b)
    %    = alpha1 * x + alpha0 * deltay
    % deltay = (y* - alpha1 * x) / alpha0
    zOut = (zOut - alpha1 .* zIn) ./ alpha0;
    % dE/dy* = y* - y
    % dE/ddeltay = dE/dy* * dy*/ddeltay
    % dE/ddeltay = alpha0 * (y* - y)
    %            = alpha0 * (alpha0 * deltay + alpha1 * x - y)
    dE_dzOut = alpha0 .* dE_dzOut;
    % deltay = f(w * x + b)
    % ddeltay/dx = w * f'(w * x + b)
    % ddeltay/dw = x * f'(w * x + b)
    % dE/dx = dE/ddeltay * ddeltay/dx
    %       = alpha0 * (alpha0 * deltay - y + alpha1 * x) * w * f'(w*x + b)
    % dE/dw = dE/ddeltay * ddeltay/dw
    %       = alpha0 * (alpha0 * deltay - y + alpha1 * x) * x * f'(w*x + b)
    if isequal(MODEL{model_num}.dynamic.transfer, 'tanh')
      dE_dzOut = coeff_delta * dE_dzOut;
      zOut = zOut / coeff_delta;
    end
  otherwise
    % Direct prediction of gene expression (e.g. in steady-state mode)
end

% Handle different transfer functions
switch (MODEL{model_num}.dynamic.transfer)
  case 'logistic'
    % Derivative of the logistic sigmoid transfer on the code
    dNonlinear_dsum = zOut .* (1 - zOut);
    % Derivative of the energy w.r.t. linear sum
    dE_dsum = dE_dzOut .* dNonlinear_dsum;
  case 'tanh'
    % Derivative of the tanh sigmoid transfer on the code
    dNonlinear_dsum = 1 - zOut.^2;
    % Derivative of the energy w.r.t. linear sum
    dE_dsum = dE_dzOut .* dNonlinear_dsum;
  case 'linear'
    % No non-linearity
    dE_dsum = dE_dzOut;
  otherwise
    error('Unknown transfer function: %s', ...
      MODEL{model_num}.dynamic.transfer);
end

if (nargout == 1)
  % Jacobian of the energy w.r.t. input latent states
  % We assume that the weights are 0 when there are no connections
  dE_dzIn = MODEL{model_num}.dynamic.w' * dE_dsum;
else
  % No back-prop onto the input latent states
  dE_dzIn = [];
  
  % Jacobian of the energy w.r.t. dynamic weight matrix
  dE_dwDyna = dE_dsum * zIn';
  % Apply the connections mask to the matrix of gradients to weights
  dE_dwDyna = dE_dwDyna .* MODEL{model_num}.dynamic.connections;
  % Derivative of the energy w.r.t. dynamic bias
  dE_dbDyna = dE_dsum;
  n_samples = size(dE_dbDyna, 2);
  if (n_samples > 1), dE_dbDyna = sum(dE_dbDyna, 2); end
end

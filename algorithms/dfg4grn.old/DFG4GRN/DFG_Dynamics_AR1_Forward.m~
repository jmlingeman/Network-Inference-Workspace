% DFG_Dynamics_AR1_Forward  Compute gradients of the dynamical energy
%
% Syntax:
%   zStar = ...
%     DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta)
% Inputs:
%   zIn:        matrix of size <N> x <T> of input latent variables
%   model_num:  model number (default 1)
%   alpha0:     matrix of size <N> x <T> of coefficients applied to 
%               the "perturbation" modeling TF effects
%               (used only in the "kinetic" mode)
%   alpha1:     matrix of size <N> x <T> of coefficients applied to
%               the autoregressive component (previous time point)
%               (used only in the "kinetic" mode)
%   coeff_delta: coefficient applied to 'tanh' deltas
% Outputs:
%   zStar:      matrix of size <N> x <T> of output latent variables
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

function zStar = ...
  DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta)

n_samples = size(zIn, 2);

% Linear forward propagation
% We assume that a sparse connection mask was applied to the weight matrix
global MODEL
zStar = MODEL{model_num}.dynamic.w * zIn + ...
  repmat(MODEL{model_num}.dynamic.bias, 1, n_samples);

switch (MODEL{model_num}.dynamic.transfer)
  case 'logistic'
    % Logistic non-linearity
    zStar = 1 ./ (1 + exp(-zStar));
  case 'tanh'
    % tanh sigmoid non-linearity
    zStar = coeff_delta * tanh(zStar);
  case 'linear'
    % Nothing to be added
  otherwise
    error('Unknown transfer function: %s', ...
      MODEL{model_num}.dynamic.transfer);
end

% Do we predict the delta or the actual value?
% Use the dynamical kinetic model (Wahde & Hertz, 2001)
switch (MODEL{model_num}.dynamic.pde_type)
  case 'kinetic'
    zStar = alpha0 .* zStar + alpha1 .* zIn;
  case 'delta'
    zStar = zStar + zIn;
  otherwise
    % Direct prediction of gene expression (e.g. in steady-state mode)
end

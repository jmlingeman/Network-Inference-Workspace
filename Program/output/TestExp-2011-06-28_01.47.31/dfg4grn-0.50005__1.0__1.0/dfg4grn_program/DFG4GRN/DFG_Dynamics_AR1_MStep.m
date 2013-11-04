% DFG_Dynamics_AR1_MStep  Perform M-step gradient descent on the dynamics
%
% Syntax:
%   [n_m_steps, eta_coeff] = ...
%     DFG_Dynamics_AR1_MStep(z, eta, params, model_num, deltaT)
% Inputs:
%   z:         matrix of size <N> x <T> of frozen latent variables
%   eta:       learning rate for gradient descent
%   params:    parameters struct
%   model_num: model number (default 1)
%   deltaT:    horizontal vector of <T-1> values of time differences
% Outputs:
%   n_m_steps: number of M-steps
%   eta_coeff: mean value of the learning rate over the M-steps

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
% Version 1.1, New York, 17 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: process all sequences at once
%             add conjugate gradient reset

function [n_m_steps, eta_coeff] = ...
  DFG_Dynamics_AR1_MStep(z, eta, params, model_num, deltaT)

% Define the input and output within latent variables and AR1 dynamics
[zIn, zTarget] = DFG_Dynamics_AR1_GetInputOutput(z);
deltaT = cell2mat(deltaT);

% Compute the kinetic time coefficients
global MODEL
tau = MODEL{model_num}.dynamic.tau;
switch (params.pde_type)
  case 'kinetic'
    [alpha0, alpha1] = ...
      DFG_Dynamics_AR1_ComputeKineticCoeff(deltaT, tau, size(zIn, 1));
  otherwise
    alpha0 = []; alpha1 = [];
end

% Parameters
n_max_m_steps = params.n_max_m_steps;
e_w_conv = params.e_w_conv;
lambda = params.lambda_w;
m_step_depth = params.m_step_depth;
coeff_delta = params.coeff_delta;

% Evaluate the output of the dynamical module before the M-step
zStar = ...
  DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta);
% Compute the energy before the M-step
e_previous = mean(DFG_Energy_Gaussian(zTarget, zStar));

% Momentum terms of the conjugate gradient
deltaWDynaPrev = [];
deltaBDynaPrev = [];
beta_w = 0;
beta_b = 0;

% Loop until convergence
n_m_steps = 0;
eta_opt_mean = 0;
cond = 1;
while (cond)
  % Recompute the gradient of the MSE energy
  dE_dzTarget = (zStar - zTarget);
  
  % Compute the gradient to the model parameters (weight matrix and bias)
  [dummy, dE_dwDyna, dE_dbDyna] = ...
    DFG_Dynamics_AR1_BackProp(zIn, zStar, dE_dzTarget, model_num, ...
    alpha0, alpha1, coeff_delta);
  deltaWDyna = -dE_dwDyna;
  deltaBDyna = -dE_dbDyna;

  % Conjugate gradient
  if ((n_m_steps > 0) && (params.use_conj_grad))
    % Compute the conjugate coefficient using Polak-Ribiere
    % with direction reset
    beta_w = sum(sum((deltaWDyna - deltaWDynaPrev) .* deltaWDyna)) / ...
      sum(sum(deltaWDynaPrev .* deltaWDynaPrev));
    beta_b = ((deltaBDyna - deltaBDynaPrev)' * deltaBDyna) / ...
      (deltaBDynaPrev' * deltaBDynaPrev);
    beta_w = max(beta_w, 0);
    beta_b = max(beta_b, 0);
    
    % Add the momentum term of the conjugate gradient
    deltaWDyna = deltaWDyna + beta_w * deltaWDynaPrev;
    deltaBDyna = deltaBDyna + beta_b * deltaBDynaPrev;
  end
  
  % L1-regularization
  if (lambda > 0)
    [deltaWDyna, deltaBDyna] = ...
      Dynamics_AR1_AddRegularizer(deltaWDyna, deltaBDyna, ...
      lambda, model_num);
  end
  
  if (m_step_depth >= 0)
    % Perform line search to find the optimal learning rate and energy
    etas = [0 1 2 3 4] * eta;
    energies = [e_previous nan nan nan nan];
    [eta_opt, e_current] = Dynamics_AR1_LineSearch(zIn, zTarget, ...
      alpha0, alpha1, coeff_delta, ...
      etas, params, energies, deltaWDyna, deltaBDyna, ...
      m_step_depth, model_num);
  else
    % Do not perform a line-search, and simply use a fixed learning rate
    eta_opt = eta;

    % Copy the previous values of the weights and bias
    [weightsCopy, biasCopy] = ModelCopy(model_num);
    % Evaluate the current energy
    Dynamics_AR1_ApplyGradient(eta, deltaWDyna, deltaBDyna, model_num);
    zStar = ...
      DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta);
    e_current = mean(DFG_Energy_Gaussian(zTarget, zStar));
    % Retrieve previous version of weights and bias
    ModelPaste(weightsCopy, biasCopy, model_num);
  end
  eta_opt_mean = eta_opt_mean + eta_opt;

  % Improvement criterion
  if (e_current < e_previous)
    
    % Apply gradient descent to the parameters
    Dynamics_AR1_ApplyGradient(eta_opt, deltaWDyna, deltaBDyna, model_num);
    n_m_steps = n_m_steps + 1;
    % Re-evaluate the output of the dynamical module after the iteration
    zStar = ...
      DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta);
    
    % Trace
    if (params.verbosity_level > 1)
      fprintf(1, 'Dynamic M-step %3d: E=%9.8f, eta=%.4f (%.2f) %g %g\n', ...
        n_m_steps, e_current, eta_opt, eta_opt/eta, beta_w, beta_b);
    end

    % Convergence criterion
    if (((e_previous - e_current) / e_previous) < e_w_conv)
      if (params.verbosity_level > 0)
        fprintf(1, 'Dynamic module converged.\n');
      end
      cond = 0;
    end
    
    % For the next M-step iteration
    e_previous = e_current;
  else
    if (params.verbosity_level > 0)
      fprintf(1, 'Dynamic energy constant or starts to increase.\n');
    end
    cond = 0;
  end
  
  % Stop if too many steps are taken
  if (n_m_steps >= n_max_m_steps)
    cond = 0;
  end
  
  % For the conjugate gradient
  deltaWDynaPrev = deltaWDyna;
  deltaBDynaPrev = deltaBDyna;
end
eta_coeff = eta_opt_mean / (eta * n_m_steps);

% Trace
if (params.verbosity_level > 0)
  fprintf(1, 'Dynamic %3d M-steps: E=%9.8f, eta=%.4f (%.2f)\n', ...
    n_m_steps, e_current, eta_opt, eta_opt/eta);
end


% -------------------------------------------------------------------------
function [eta, e] = Dynamics_AR1_LineSearch(zIn, zTarget, ...
  alpha0, alpha1, coeff_delta, ...
  etas, params, energies, deltaWDyna, deltaBDyna, depth, model_num)

% Copy the previous values of the weights and bias
[weightsCopy, biasCopy] = ModelCopy(model_num);

% Loop over the grid of etas and energies and evaluate only missing values
for k = 1:5
  if isnan(energies(k))
    eta = etas(k);

    % Apply the current gradient to the model
    Dynamics_AR1_ApplyGradient(eta, deltaWDyna, deltaBDyna, model_num);
    
    % Re-evaluate the output of the model
    zStar = ...
      DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, coeff_delta);

    % Recompute the energy
    energies(k) = mean(DFG_Energy_Gaussian(zTarget, zStar));
    
    % Retrieve previous version
    ModelPaste(weightsCopy, biasCopy, model_num);
  end
end

% Find the minimum energy
[e_coarse, k] = min(energies);

if (depth > 0)
  if (k == 1)
    etas = linspace(etas(1), etas(2), 5);
    energies = [energies(1) nan nan nan energies(2)];
  elseif (k == 5)
    etas = linspace(etas(1), 4 * (etas(5)-etas(1)), 5);
    energies = [energies(1) energies(5) nan nan nan];
  else
    eta0 = DFG_QuadraticLineSearch(etas, energies);
    energies = [energies nan];
    [etas, ind] = sort([etas eta0]);
    energies = energies(ind);
    depth = 1;
  end
  
  % Recursively call the next level of line search
  [eta, e] = Dynamics_AR1_LineSearch(zIn, zTarget, ...
    alpha0, alpha1, coeff_delta, ...
    etas, params, energies, deltaWDyna, deltaBDyna, depth - 1, model_num);
else
  eta = etas(k);
  e = energies(k);
end


% -------------------------------------------------------------------------
function [deltaWDyna, deltaBDyna] = ...
  Dynamics_AR1_AddRegularizer(deltaWDyna, deltaBDyna, lambda, model_num)

% Apply L1 regularization to the parameters
global MODEL
deltaWDyna = deltaWDyna - lambda * sign(MODEL{model_num}.dynamic.w);
deltaBDyna = deltaBDyna - lambda * sign(MODEL{model_num}.dynamic.bias);


% -------------------------------------------------------------------------
function Dynamics_AR1_ApplyGradient(eta, deltaWDyna, deltaBDyna, model_num)

% Gradient descent on the parameters
global MODEL
MODEL{model_num}.dynamic.w = ...
  MODEL{model_num}.dynamic.w + eta * deltaWDyna;
MODEL{model_num}.dynamic.bias = ...
  MODEL{model_num}.dynamic.bias + eta * deltaBDyna;

% We do not need to check that forbidden connections are set to 0
% since the gradient deltaWDyna was set to 0 for forbidden connections,
% and the L1- or L2-regularization keeps the sign(MODEL.dynamic.w) at 0 
% MODEL.dynamic.w = MODEL.dynamic.w .* MODEL.dynamic.connections;


% -------------------------------------------------------------------------
function [weightsCopy, biasCopy] = ModelCopy(model_num)

global MODEL
weightsCopy = MODEL{model_num}.dynamic.w;
biasCopy = MODEL{model_num}.dynamic.bias;


% -------------------------------------------------------------------------
function ModelPaste(weightsCopy, biasCopy, model_num)

global MODEL
MODEL{model_num}.dynamic.w = weightsCopy;
MODEL{model_num}.dynamic.bias = biasCopy;

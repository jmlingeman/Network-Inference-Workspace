% DFG_Infer  E-step of the EM-like algorithm: infer latent variables
%
% Syntax:
% [zBar, yBar, meter] = ...
%   DFG_Infer(z, y, yKnown, params, eta_z, meter, traceStr, model_num, dT)
% Inputs:
%   z:         matrix of current values of latent variables, size <M> x <T>
%              or cell array of such matrices
%   y:         matrix of observerd values, size <N> x <T>
%              or cell array of such matrices
%   yKnown:    matrix of known observations, size <N> x <T>, or cell array
%   params:    parameters struct
%   eta_z:     inference rate for the gradient descent on latent variables
%   meter:     meter struct
%   traceStr:  character string
%   model_num: model number
%   dT:        vector of time points, size 1 x <T>
% Outputs:
%   zBar:      matrix of latent variables, size <M> x <T>
%   yBar:      matrix of observed variables, size <N> x <T>
%   meter:     updated meter struct

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

% Revision 1: evaluate NMSE/SNR using mean energy over all sequences
%             return also <zBarOut>, output of the dynamical model

function [zBar, yBar, meter, zBarOut] = ...
  DFG_Infer(z, y, yKnown, params, eta_z, meter, traceStr, model_num, dT)

% Dimensions
n_samples = size(y, 2);
dim_y = size(y, 1);
dim_z = params.dim_z;
zStar = z;
if (params.verbosity_level > 0)
  fprintf(1, '[%s] E-step\n', datestr(now));
end

% Parameters
e_z_conv = params.e_z_conv;
n_max_e_steps = params.n_max_e_steps;
gamma = params.gamma;
n_steps_trace = params.n_steps_trace;
coeff_delta = params.coeff_delta;
dynamic_model = params.dynamic_model;
switch (dynamic_model)
  case 'AR1'
  otherwise
    error('Unknown dynamical model %s.', dynamic_model);
end
dynamic_ar_order = params.dynamic_ar_order;
observation_model = params.observation_model;
switch (observation_model)
  case 'id'
  otherwise
    error('Unknown observation model %s.', observation_model);
end
indVars = params.indVars;

% Compute the kinetic time coefficients
global MODEL
tau = MODEL{model_num}.dynamic.tau;
switch (params.pde_type)
  case 'kinetic'
    [alpha0, alpha1] = ...
      DFG_Dynamics_AR1_ComputeKineticCoeff(dT, tau, dim_z);
  otherwise
    alpha0 = []; alpha1 = [];
end


% E-step: refine the estimate of codes, looping until convergence
% ---------------------------------------------------------------
n_e_steps = 0;
cond_grad = 1;
cond_keep = 1;
e_current = inf;
while (cond_grad)
  trace = (mod(n_e_steps+1, n_steps_trace) == 0);

  % Compute the current value of the energies and gradients w.r.t. codes
  % --------------------------------------------------------------------
  
  % Forward propagation to generate the next value of latent variables
  % using their past values and the dynamic module
  switch (dynamic_model)
    case 'AR1'
      zIn = zStar(:, 1:(end-1));
      zTarget = zStar(:, 2:end);
      zOut = DFG_Dynamics_AR1_Forward(zIn, model_num, ...
        alpha0, alpha1, coeff_delta);
  end

  % Forward propagation to generate the next value of observations
  % using the latent variables
  switch (observation_model)
    case 'id'
      yStar = zStar;
  end

  % Compute the dynamic energy and gradient on the current latent sequence
  e_dyna_mean_current = mean(DFG_Energy_Gaussian(zTarget, zOut)) / dim_z;
  dEdyna_dzDyna = (zOut - zTarget);
  dEdyna_dzTarget = -dEdyna_dzDyna;
  switch (dynamic_model)
    case 'AR1'
      dEdyna_dzIn = ...
        DFG_Dynamics_AR1_BackProp(zIn, zOut, dEdyna_dzDyna, model_num, ...
        alpha0, alpha1, coeff_delta);
  end
  % Add the "leftward" and "rightward" dynamical gradients
  switch (dynamic_ar_order)
    case 1
      dEdyna_dz = zeros(dim_z, n_samples);
      dEdyna_dz(:, 1:(n_samples - dynamic_ar_order)) = dEdyna_dzIn;
      dEdyna_dz(:, (dynamic_ar_order + 1):end) = dEdyna_dzTarget;
  end

  % Compute the dynamic observation and gradient on current latent sequence
  e_obs_mean_current = mean(DFG_Energy_Gaussian(y, yStar, yKnown)) / dim_z;
  dEobs_dzObs = (yStar - y) .* yKnown;
  switch (observation_model)
    case 'id'
      dEobs_dz = dEobs_dzObs;
  end

  % Merge the two gradients
  dE_dz = dEobs_dz + gamma * dEdyna_dz;


  % Evaluate the convergence and make statistics on energy
  % ------------------------------------------------------
  % Evaluate the current weighted sum of energies
  e_prev = e_current;
  e_current = e_obs_mean_current + gamma * e_dyna_mean_current;

  % No E-step: stop now
  if (eta_z == 0)
    cond_grad = 0;
    cond_keep = 0;
    if (params.verbosity_level > 0)
      fprintf(1, '(no E-step) ');
    end
  else
    % Compute the convergence criterion and update the latent code
    if (e_current >= e_prev)
      % Current code values <z_star> are worse
      cond_grad = 0;
      cond_keep = 0;
      if (params.verbosity_level > 0)
        fprintf(1, '(increase) ');
      end
    elseif (n_e_steps >= n_max_e_steps)
      % Too many E-steps
      cond_grad = 0;
      cond_keep = 1;
      if (params.verbosity_level > 0)
        fprintf(1, '(max #steps) ');
      end
    elseif (e_z_conv && ((e_prev - e_current) < (e_prev * e_z_conv)))
      % Error converged
      cond_grad = 0;
      cond_keep = 1;
      if (params.verbosity_level > 0)
        fprintf(1, '(converged) ');
      end
    elseif (sum(sum(dE_dz.^2)) == 0)
      % No gradient
      cond_grad = 0;
      cond_keep = 1;
      if (params.verbosity_level > 0)
        fprintf(1, '(no gradient) ');
      end
    else
      % Normal case: continue gradient descent
      n_e_steps = n_e_steps + 1;
      if (params.verbosity_level > 1)
        fprintf(1, 'E-step %2d: ', n_e_steps);
      end
    end
  end
  if (params.verbosity_level > 1)
    fprintf(1, 'E=%8.8f: Edyna=%8.8f, Eobs=%8.8f\n', ...
      e_current, e_dyna_mean_current, e_obs_mean_current);
  end


  % Keep the previously found latent variables and reconstruction
  % -------------------------------------------------------------
  if (cond_keep)
    % Save a copy of the previously found reconstruction and classification
    yBar = yStar;
    % Save a copy of the previously inferred code
    zBar = zStar;
    zBarOut = zOut;
    % Keep track of statistics on codes and reconstruction
    e_dyna_mean = e_dyna_mean_current;
    e_obs_mean = e_obs_mean_current;
  end
  if (~cond_grad && (eta_z == 0))
    % Latent variables are equal to the observations
    yBar = y;
    zBar = y;
    zBarOut = zOut;
    % Keep track of statistics on codes and reconstruction
    e_dyna_mean = e_dyna_mean_current;
    e_obs_mean = e_obs_mean_current;
  end

  % Apply gradient descent to the codes if we have not converged yet
  % ----------------------------------------------------------------
  if (cond_grad)
    zStar = zStar - eta_z * dE_dz;
  end
end

% Final statistics
e_obs_mean_signal = params.e_mean_signal;
if isequal(params.dynamic_model, 'AR1')
  e_dyna_mean_signal = e_obs_mean_signal;
  dYmean = params.dYmean;
else
  e_dyna_mean_signal = 0.5 * sum(sum(zBar.^2)) / (dim_z * n_samples);
  dYmean = mean(diff(y, 1, 2), 2);
end
nmse_obs = e_obs_mean / e_obs_mean_signal;
nmse_dyna = e_dyna_mean / e_dyna_mean_signal;
snr_obs = -10 * log10(nmse_obs);
snr_dyna = -10 * log10(nmse_dyna);
[z_smoothness, z_sparsity] = StatisticsLatentVar(z);

% Trace after convergence
if (params.verbosity_level > 0)
  fprintf(1, '%s %3d E-steps ', traceStr, n_e_steps);
  fprintf(1, 'eDyna=%7.4f, eObs=%7.4f, ', e_dyna_mean, e_obs_mean);
  fprintf(1, 'smoos=%.3f, spars=%.3f\n', z_smoothness, z_sparsity);
  fprintf(1, 'NMSE obs=%5.4f, dyna=%5.4f, SNR obs=%5.2f, dyna=%5.2f\n', ...
    nmse_obs, nmse_dyna, snr_obs, snr_dyna);
end

% Update the meter
meter = DFG_MeterUpdate(meter, 'energy_observation', e_obs_mean, model_num);
meter = DFG_MeterUpdate(meter, 'energy_dynamic', e_dyna_mean, model_num);
meter = DFG_MeterUpdate(meter, 'nmse_observation', nmse_obs, model_num);
meter = DFG_MeterUpdate(meter, 'nmse_dynamic', nmse_dyna, model_num);
meter = DFG_MeterUpdate(meter, 'snr_observation', snr_obs, model_num);
meter = DFG_MeterUpdate(meter, 'snr_dynamic', snr_dyna, model_num);
meter = DFG_MeterUpdate(meter, 'z_sparsity', z_sparsity, model_num);
meter = DFG_MeterUpdate(meter, 'z_smoothness', z_smoothness, model_num);
meter = DFG_MeterUpdate(meter, 'n_e_steps', n_e_steps, model_num);
meter = DFG_MeterUpdate(meter, 'eta_z', eta_z, model_num);


% -------------------------------------------------------------------------
function [z_smoothness, z_sparsity] = StatisticsLatentVar(z)

% Statistics on latent variables
z_smoothness = sum(sum(diff(z, 1, 1).^2));
z_sparsity = DFG_ComputeSparsity(z);

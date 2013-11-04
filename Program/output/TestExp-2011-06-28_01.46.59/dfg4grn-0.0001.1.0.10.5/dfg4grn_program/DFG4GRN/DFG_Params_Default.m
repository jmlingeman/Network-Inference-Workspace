% DFG_Params_Default  Initialize the default values of parameters
%
% Syntax:
%   params = DFG_Params_Default(dim_z)
% Inputs:
%   dim_z: number of latent dimensions

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

function params = DFG_Params_Default(dim_z)

params = struct('n_epochs', 1000, ...
  'dim_z', dim_z, ...
  'indVars', true(dim_z, 1), ...
  'normalize_data', 0, ... % Do not normalize data by default
  ... % Dynamical model: we use a Markov (non)linear model (AR1)
  ... % --------------------------------------------------------
  'dynamic_model', 'AR1', ... % This should not change
  'dynamic_ar_order', 1, ...  % This should not change
  'pde_type', 'kinetic', ...  % Use kinetic equation (Wahde & Hertz 2001)
  'dynamic_transfer', 'linear', ... % Put nonlinearity here
  'tau', 1, ...               % Kinetic hyper-parameter to modify
  'dynamic_algorithm', 'gradient', ... % Gradient-based, LARS, Elastic Net?
  'coeff_delta', 1, ...       % Coefficient that multiplies tanh activation
  ...                         % set to 0 for automatic (IMPORTANT)
  'n_tfs', 0, ...             % Number of transcription factors
  ... % Target gene regulation network
  ... % ------------------------------
  'targetGRN', [], ...
  ... % Connection matrix: which TF-gene connections are allowed?
  ... % ---------------------------------------------------------
  'dynamic_connections', true(dim_z), ... % All connect. allowed by default
  'p_level', 0.001, ...                   % P-level for stat. signif. links
  ... % Observation model: we use a trivial identity function now
  ... % ---------------------------------------------------------
  'observation_model', 'id', ...
  ... % Learning hyper-parameters (regularized conjugate gradient)
  ... % ----------------------------------------------------------
  'eta_w', 0.1, ...          % Learning rate (IMPORTANT, despite line search)
  'eta_w_decay', 0.9975, ... % Decays by half after 300 epochs, whatever
  'lambda_w', 1e-4, ...      % L1-regularization rate (IMPORTANT)
  'e_w_conv', 1e-9, ...      % Energy convergence threshold (KEEP LOW)
  'n_max_m_steps', 100, ...  % Max number of M-steps (IMPORTANT, KEEP HIGH)
  'm_step_depth', 10, ...    % Max depth of recursion per M-step (KEEP 10)
  'use_conj_grad', 0, ...    % Use conjugate gradient? (AVOID IT)
  ... % Inference parameters
  ... % --------------------
  'eta_z', 0.1, ...          % Inference rate (IMPORTANT)
  'eta_z_decay', 0.9975, ... % Decay of the inference rate
  'lambda_z', 0, ...         % L1-regularization on latent variables (BAD)
  'e_z_conv', 1e-9, ...      % Error convergence threshold
  'gamma', 1, ...            % Weight given to obs w.r.t. dyna (IMPORTANT)
  'n_max_e_steps', 100, ...  % Max number of E-steps (IMPORTANT, KEEP HIGH)
  ... % Display and save parameters
  ... % ---------------------------
  'n_steps_display', 1, ...  % If we want to display (plot) results
  'n_steps_trace', 1, ...    % If we want to trace (print) results
  'n_steps_infer', 1, ...    % If we want to infer on train/test data
  'n_steps_save', 1, ...     % If we want to save to a file
  'n_steps_archive', 0, ...  % If we want to archive consecutive GRNs
  'verbosity_level', 2, ...  % From 0 (none) to 2 (high)
  'labels_y', [], ...        % Regulated gene names
  'save_z', 1);              % Save the inferred latent variables?

% Temporary filename and time when learning experiment starts
params.time_begin = now;
params.temp_filename = ...
  ['temp_' datestr(params.time_begin, 'yyyymmdd_HHMMSS') '.mat'];

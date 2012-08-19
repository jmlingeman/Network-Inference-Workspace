% DFG_Dynamics_AR1_Lasso  Perform Lasso regression on the dynamics
%
% Syntax:
%   DFG_Dynamics_AR1_Lasso(z, params, model_num, deltaT)
% Inputs:
%   z:         matrix of size <N> x <T> of frozen latent variables
%   params:    parameters struct
%   model_num: model number (default 1)
%   deltaT:    horizontal vector of <T-1> values of time differences

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

function DFG_Dynamics_AR1_Lasso(z, params, model_num, deltaT)

global MODEL
if ~isequal(MODEL{model_num}.dynamic.transfer, 'linear')
  error('Only linear transfer functions allowed for Lasso');
end

% Define the input and output within latent variables and AR1 dynamics
[zIn, zTarget] = DFG_Dynamics_AR1_GetInputOutput(z);
deltaT = cell2mat(deltaT);

% Compute the kinetic time coefficients
switch (params.pde_type)
  case 'kinetic'
    tau = MODEL{model_num}.dynamic.tau;
    [alpha0, alpha1] = ...
      DFG_Dynamics_AR1_ComputeKineticCoeff(deltaT, tau, size(zIn, 1));
  otherwise
    alpha0 = []; alpha1 = [];
end

% Evaluate the output of the dynamical module before the Lasso optimization
zStar = ...
  DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, 0);
% Compute the energy before the Lasso optimization
e_previous = mean(DFG_Energy_Gaussian(zTarget, zStar));

% Convert the target w.r.t. kinetic equations
switch (MODEL{model_num}.dynamic.pde_type)
  case 'delta'
    % Random walk model
    zTargetLasso = zTarget - zIn;
  case 'kinetic'
    % Kinetic equation parameterized by <tau>
    zTargetLasso = (zTarget - alpha1 .* zIn) ./ alpha0;
  otherwise
    % Direct prediction of gene expression (e.g. in steady-state mode)
    zTargetLasso = zTarget;
end

% Parameters
lambda = params.lambda_w;

% Initialize the Lasso matrix
w = zeros(size(MODEL{model_num}.dynamic.w));
n_genes = size(w, 1);
b = zeros(n_genes, 1);
warning('off');

% Perform Lasso gene by gene
for k = 1:n_genes

  % Select only relevant predictors for the gene (allowed connections)
  indPredictors = MODEL{model_num}.dynamic.connections(k, :);
  [dummy, wk] = ...
    bolasso(zIn(indPredictors, :)', zTargetLasso(k, :)', ...
    'lambda', lambda, 'statusBar', 0, 'plotResults', 0, 'nbootstraps', 0);
  b(k) = wk(1);
  w(k, indPredictors) = wk(2:end)';
  fprintf(1, 'Performed Lasso for gene %4d/%4d\n', k, n_genes);
end
MODEL{model_num}.dynamic.w = w;
MODEL{model_num}.dynamic.bias = b;

% Evaluate the output of the dynamical module before the Lasso optimization
zStar = ...
  DFG_Dynamics_AR1_Forward(zIn, model_num, alpha0, alpha1, 0);
% Compute the energy before the Lasso optimization
e_current = mean(DFG_Energy_Gaussian(zTarget, zStar));

% Trace
fprintf(1, 'Dynamic Lasso: E=%9.8f->%9.8f\n', e_previous, e_current);
warning('on');

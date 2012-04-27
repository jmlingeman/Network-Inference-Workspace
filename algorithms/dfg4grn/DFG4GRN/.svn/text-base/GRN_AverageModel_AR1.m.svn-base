% GRN_AverageModel_AR1  Create average models from all runs
%
% Create 2 average models (simple average, and statistically significant
% links only) from all existing models (assuming same data and parameters).
% Models are created by increasing global variable cell array MODEL by 2.
%
% Syntax:
%   [wAverage, biasAverage, pNet, pNetBinary] = ...
%     GRN_AverageModel_AR1(p_level)
% Inputs:
%   p_level: scalar between 0 and 0.5, typically 0.001
% Outputs:
%   wAverage:    matrix of size <N> x <M> of weights for AR(1) model
%   biasAverage: vector of size <N> x <1> of biases for AR(1) model
%   pNet:        matrix of size <N> x <M> of P-values
%   pNetBinary:  boolean matrix of size <N> x <M> (is there a link or not?)

% Copyright (C) 2010 Piotr Mirowski
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
% Version 1.0, New York, 24 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [wAverage, biasAverage, pNet, pNetBinary] = ...
  GRN_AverageModel_AR1(p_level)

% Significance level
if (nargin < 1)
  p_level = 0.001;
end

% Gather all the network models and compute the average
global MODEL
n_models = length(MODEL);
nets = cell(1, n_models);
wAverage = zeros(size(MODEL{1}.dynamic.w));
biasAverage = zeros(size(MODEL{1}.dynamic.bias));
for k = 1:n_models
  nets{k} = MODEL{k}.dynamic.w;
  wAverage = wAverage + nets{k};
  biasAverage = biasAverage + MODEL{k}.dynamic.bias;
end
wAverage = wAverage / n_models;
biasAverage = biasAverage / n_models;

% Compute statistically significant links
[pNet, pNetBinary] = ...
  GRN_StatisticallySignificantLinks(nets, p_level);

% Create the simple average model
MODEL{n_models+1} = struct('dynamic', [], 'observation', []);
MODEL{n_models+1}.dynamic = struct('architecture', 'AR1');
MODEL{n_models+1}.dynamic.transfer = MODEL{1}.dynamic.transfer;
MODEL{n_models+1}.dynamic.connections = MODEL{1}.dynamic.connections;
MODEL{n_models+1}.dynamic.pde_type = MODEL{1}.dynamic.pde_type;
MODEL{n_models+1}.dynamic.connections = MODEL{1}.dynamic.connections;
MODEL{n_models+1}.dynamic.tau = MODEL{1}.dynamic.tau;
MODEL{n_models+1}.dynamic.w = wAverage;
MODEL{n_models+1}.dynamic.bias = biasAverage;
MODEL{n_models+1}.observation = struct('architecture', 'id');

% Create the average model with statistically significant links only
MODEL{n_models+2} = MODEL{n_models+1};
MODEL{n_models+2}.dynamic.w = wAverage .* pNetBinary;

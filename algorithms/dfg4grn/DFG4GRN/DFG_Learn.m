% DFG_Infer  E-step of the EM-like algorithm: infer latent variables
%
% Syntax:
%   [yStarTrain, zStarTrain] = DFG_Learn(yTrain, yKnownTrain, ...
%     yTest, yKnownTest, params, model_num, deltaTtrain, deltaTtest)
% Inputs:
%   yTrain:      matrix of observerd values, size <N> x <T1>
%   yKnownTrain: matrix of observerd values, size <N> x <T1>
%   yTest:       matrix of observerd values, size <N> x <T2>
%   yKnownTest:  matrix of observerd values, size <N> x <T2>
%   params:      parameters struct
%   model_num:   model number
%   deltaTtrain: vector of delays, size 1 x <T1-1>
%   deltaTtest:  vector of delays, size 1 x <T2-1>
% Outputs:
%   yStarTrain:  matrix of observerd values, size <N> x <T1>
%   zStarTrain:  matrix of observerd values, size <N> x <T1>
%
% N.B. In the (probable case) that there are several sequences,
% the above matrices become cell arrays, each cell containing a matrix.
% The number of time points can change between sequences, but the number of
% genes <N> must remain the same.

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

% Revision 1: added AUC and other statistics comparing AR1 model to target,
%             compute mean energy of signal over all sequences
%             add LASSO (LARS) and Elastic Nets
% Revision 2: recompute coeff_delta only at this point, when needed,
%             move meter updates into independent functions
%             correct bug in mean energy of signal, move function out
% Revision 3: characterize model sparsity also in METER_INFER_TRAIN

function [yStarTrain, zStarTrain] = DFG_Learn(yTrain, yKnownTrain, ...
  yTest, yKnownTest, params, model_num, deltaTtrain, deltaTtest)


% --------------
% Initialization
% --------------

% Dimensions
if iscell(yTrain)
  n_seq_train = length(yTrain);
else
  n_seq_train = 1;
  yTrain = {yTrain};
end
if iscell(yTest)
  n_seq_test = length(yTest);
else
  n_seq_test = 1;
  yTest = {yTest};
end
dim_y = size(yTrain{1}, 1);
dim_z = params.dim_z;

% (Un)known observations
if isempty(yKnownTrain)
  yKnownTrain = cell(1, n_seq_train);
  for j = 1:n_seq_train
    yKnownTrain{j} = ones(size(yTrain{j}));
  end
end
if isempty(yKnownTest)
  yKnownTest = cell(1, n_seq_test);
  for j = 1:n_seq_test
    yKnownTest{j} = ones(size(yTest{j}));
  end
end

% Parameters
n_epochs = params.n_epochs;
eta_w = params.eta_w;
eta_w_decay = params.eta_w_decay;
eta_z = params.eta_z;
eta_z_decay = params.eta_z_decay;
gamma = params.gamma;
n_steps_display = params.n_steps_display;
n_steps_trace = params.n_steps_trace;
n_steps_infer = params.n_steps_infer;
n_steps_archive = params.n_steps_archive;
dynamic_model = params.dynamic_model;
observation_model = params.observation_model;
if (nargin < 4), model_num = 1; end

% Multiplier to deltaZ in the kinetic equation, in the case of non-linear
% activation 'tanh'
if ((params.coeff_delta == 0) && isequal(params.dynamic_transfer, 'tanh'))
  params.coeff_delta = ...
    DFG_Dynamics_AR1_DistribDelta(yTrain, params.tau, deltaTtrain);
end

% Time steps
if (nargin < 7)
  deltaTtrain = cell(1, n_seq_train);
  for k = 1:n_seq_train
    deltaTtrain{k} = 1;
  end
end
if (nargin < 8)
  deltaTtest = cell(1, n_seq_test);
  for k = 1:n_seq_test
    deltaTtest{k} = 1;
  end
end

% Sanity checks
switch (dynamic_model)
  case 'AR1'
  otherwise
    error('Unknown dynamical model %s.', dynamic_model);
end
switch (observation_model)
  case 'id'
  otherwise
    error('Unknown observation model %s.', observation_model);
end

% Initialize the parameters and set missing connections
ModelInit(dim_z, dynamic_model, observation_model, ...
  params, model_num, yTrain);
global MODEL
MODEL{model_num}.dynamic.tau = params.tau;

% Initalize latent variables
if ((dim_y == dim_z) && isequal(observation_model, 'id'))
  yStarTrain = yTrain;
  zStarTrain = yTrain;
  yStarTest = yTest;
  zStarTest = yTest;
  zTrain = yTrain;
end
switch (dynamic_model)
  case 'AR1'
    zStarTrainOut = cell(1, n_seq_train);
    for j = 1:n_seq_train
      zStarTrainOut{j} = zStarTrain{j}(:, 2:end);
    end
    zStarTestOut = cell(1, n_seq_test);
    for j = 1:n_seq_test
      zStarTestOut{j} = zStarTest{j}(:, 2:end);
    end
  otherwise
end

% Initialize the energy results and meters
global METER_INFER_TRAIN
global METER_INFER_TEST
global METER_LEARN
METER_INFER_TRAIN{model_num} = [];
METER_INFER_TEST{model_num} = [];
METER_LEARN{model_num} = [];
e_best_train = inf;

% Compute mean energies of the signal
[params.dYmean, params.e_mean_signal] = ...
  DFG_Data_MeanSignal(yTrain, yTest, yKnownTrain, yKnownTest);

% Display the parameters
disp(params);


% --------
% Learning
% --------

% Loop over the epochs
nSteps = 1;
for k = 1:params.n_epochs
  fprintf(1, '\nLearning epoch %d/%d:\n', k, n_epochs);

  % E-step (inference/relaxation of latent variables Z)
  % ---------------------------------------------------

  % Loop over the scrambled order of sequences
  order = ScrambleSeq(n_seq_train);
  len_total = 0;
  e_obs_epoch = 0;
  e_dyna_epoch = 0;
  for j = order
    len_seq_train = size(yTrain{j}, 2);
    len_total = len_total + len_seq_train;
    [zTrain{j}, yStarTrain{j}, METER_LEARN] = ...
      DFG_Infer(zTrain{j}, yTrain{j}, yKnownTrain{j}, ...
      params, eta_z, METER_LEARN, ...
      sprintf('Learn epoch %d, %d/%d:', k, j, n_seq_train), ...
      model_num, deltaTtrain{j});

    % Merge statistics from different mini-batches of same epoch
    e_dyna_epoch = e_dyna_epoch + ...
      len_seq_train * METER_LEARN{model_num}.last_energy_dynamic;
    e_obs_epoch = e_obs_epoch + ...
      len_seq_train * METER_LEARN{model_num}.last_energy_observation;
    nSteps = nSteps + 1;
  end

  % M-step (model update)
  % Backpropagate the gradients from the generative and dynamical models
  % through the codes, to the model parameters
  % --------------------------------------------------------------------
  fprintf(1, '[%s] M-step\n', datestr(now));
  coeff_learn = eta_w / len_seq_train;
  switch (dynamic_model)
    case 'AR1'
      switch (params.dynamic_algorithm)
        case 'gradient'
          [n_m_steps, eta_coeff] = ...
            DFG_Dynamics_AR1_MStep(zTrain, coeff_learn, params, ...
            model_num, deltaTtrain);
          MeterUpdateParams(n_m_steps, model_num, eta_coeff);
        case 'lars'
          DFG_Dynamics_AR1_Lasso(zTrain, params, model_num, deltaTtrain);
          MeterUpdateParams(0, model_num, 0);
      end
  end

  % Statistics after this M-step on all the sequences
  if (params.verbosity_level > 0)
    fprintf(1, 'Learn epoch %d/%d, seq %d/%d: eDyna=%g, eObs=%g\n', ...
      k, n_epochs, j, n_seq_train, ...
      e_dyna_epoch/len_total, e_obs_epoch/len_total);
  end

  % Compare with target GRN
  METER_LEARN = ...
    DFG_MeterUpdate_Comparison2TargetGRN(model_num, params, METER_LEARN);

  % Statistics on epoch
  e_dyna_epoch = e_dyna_epoch / len_total;
  e_obs_epoch = e_obs_epoch / len_total;
  fprintf(1, '\nLearn epoch %4d/%4d: eDyna=%10.6f, eObs=%10.6f\n', ...
    k, n_epochs, e_dyna_epoch, e_obs_epoch);

  % Compare with target GRN
  METER_INFER_TRAIN = ...
    DFG_MeterUpdate_Comparison2TargetGRN(model_num, params, ...
    METER_INFER_TRAIN);

  % Infer all the sequences of latent states using the current model
  e_current_train = 0;
  if (mod(k, n_steps_infer) == 0)
    for j = 1:n_seq_train
      [zStarTrain{j}, yStarTrain{j}, ...
        METER_INFER_TRAIN, zStarTrainOut{j}] = ...
        DFG_Infer(zStarTrain{j}, yTrain{j}, yKnownTrain{j}, ...
        params, 0, METER_INFER_TRAIN, ...
        sprintf('Train epoch %d, %d/%d:', k, j, n_seq_train), ...
        model_num, deltaTtrain{j});
      if (gamma > 0)
        e_current_train = e_current_train + ...
          METER_INFER_TRAIN{model_num}.last_energy_observation + ...
          gamma * METER_INFER_TRAIN{model_num}.last_energy_dynamic;
      else
        e_current_train = METER_INFER_TRAIN{model_num}.last_energy_dynamic;
      end
    end
    METER_INFER_TRAIN = ...
      DFG_MeterUpdate_TrendError(zStarTrain, zStarTrainOut, ...
      params, METER_INFER_TRAIN, model_num);
  end

  % Infer all the sequences of latent states on test data, using the model
  if (mod(k, n_steps_infer) == 0)
    for j = 1:n_seq_test
      [zStarTest{j}, yStarTest{j}, METER_INFER_TEST, zStarTestOut{j}] = ...
        DFG_Infer(zStarTest{j}, yTest{j}, yKnownTest{j}, ...
        params, 0, METER_INFER_TEST, ...
        sprintf('Test epoch %d, %d/%d:', k, j, n_seq_test), ...
        model_num, deltaTtest{j});
    end
    METER_INFER_TEST = ...
      DFG_MeterUpdate_TrendError(zStarTest, zStarTestOut, ...
      params, METER_INFER_TEST, model_num);
  end

  % Is it the best model yet? Keep it if so...
  if (e_current_train < e_best_train)
    e_best_train = e_current_train;
    DFG_Model_SaveBest(k, model_num);
  end

  % Store history of DFGs
  if (mod(k, n_steps_archive) == 0)
    DFG_Model_Archive(model_num);
  end

  % Plots
  if (n_steps_display > 0)
    DFG_Trace_AR1_Id(yTrain, zTrain, zStarTrainOut, ...
      k, n_epochs, model_num, ...
      n_seq_train, n_seq_test, params);
  end

  % Anneal the learning rates
  eta_w = eta_w * eta_w_decay;
  fprintf(1, 'Parameter learning rate annealed to %f\n', eta_w);
  eta_z = eta_z * eta_z_decay;
  fprintf(1, 'Code learning rate annealed to %f\n', eta_z);
end


% ---------
% Inference
% ---------

% Retrieve the best model
DFG_Model_RetrieveBest(model_num);
eta_z = params.eta_z;

% Infer on the training sequences
for j = 1:n_seq_train
  [zStarTrain{j}, yStarTrain{j}, METER_INFER_TRAIN, zStarTrainOut{j}] = ...
    DFG_Infer(zStarTrain{j}, yTrain{j}, yKnownTrain{j}, ...
    params, eta_z, METER_INFER_TRAIN, ...
    sprintf('Train %d/%d:', j, n_seq_train), model_num, deltaTtrain{j});
end
METER_INFER_TRAIN = ...
  DFG_MeterUpdate_TrendError(zStarTrain, zStarTrainOut, ...
  params, METER_INFER_TRAIN, model_num);

% Characterize the sparsity of the model
stats_wDyna = ...
  DFG_CharacterizeParams(MODEL{model_num}.dynamic.w, 'wDyna', 1);
METER_INFER_TRAIN = ...
  DFG_MeterUpdate(METER_INFER_TRAIN, 'w_sparsity', ...
  stats_wDyna.sparsity, model_num);

% Infer on the testing sequences
for j = 1:n_seq_test
  [zStarTest{j}, yStarTest{j}, METER_INFER_TEST, zStarTestOut{j}] = ...
    DFG_Infer(zStarTest{j}, yTest{j}, yKnownTest{j}, ...
    params, 0, METER_INFER_TEST, ...
    sprintf('Test %d/%d:', j, n_seq_test), model_num, deltaTtest{j});
end
METER_INFER_TEST = ...
  DFG_MeterUpdate_TrendError(zStarTest, zStarTestOut, ...
  params, METER_INFER_TEST, model_num);

% Plots
if (n_steps_display > 0)
  % Plot the reconstruction and dynamical energies per epoch
  figure;
  subplot(2, 1, 1);
  title('Average observation SNR, per epoch');
  plot(METER_INFER_TRAIN{model_num}.snr_observation, 'b-');
  subplot(2, 1, 2);
  title('Average dynamical SNR, per epoch');
  plot(METER_INFER_TRAIN{model_num}.snr_dynamic, 'b-');
end


% -------------------------------------------------------------------------
function ModelInit(dim_z, dynamic_model, observation_model, params, n, y)

global MODEL

% Initialize the dynamical model
MODEL{n} = struct('dynamic', [], 'observation', []);
MODEL{n}.dynamic = struct('architecture', dynamic_model);
switch dynamic_model
  case 'AR1'
    MODEL{n}.dynamic.bias = zeros(dim_z, 1);
    MODEL{n}.dynamic.transfer = params.dynamic_transfer;
    MODEL{n}.dynamic.connections = ones(dim_z, dim_z);
    MODEL{n}.dynamic.pde_type = params.pde_type;
    if (isfield(params, 'correlation_prior') && params.correlation_prior)
      ModelCorrelationPrior(params, n);
%     else if (isfield(params, 'tf_prior') && params.tf_prior)
%       ModelTFPrior(y, dim_z, params, n);
    elseif (isfield(params, 'file_prior_weights') && params.file_prior_weights)
      ModelFilePriorWeights(params, n);
    else
      MODEL{n}.dynamic.w = randn(dim_z, dim_z) / sqrt(dim_z);
    end
    if (isfield(params, 'use_dex') && params.use_dex)
        ModelApplyDexToWeights(params, n);
    end
end

% Enforce missing connections
if (isfield(params, 'file_prior_connections') && params.file_prior_connections)
    ModelFilePriorConnections(params, n);
else
    MODEL{n}.dynamic.connections = params.dynamic_connections;
end
MODEL{n}.dynamic.w = MODEL{n}.dynamic.w .* params.dynamic_connections;

% Initialize the observation model
MODEL{n}.observation = struct('architecture', observation_model);
switch observation_model
  case 'id'
end


% -------------------------------------------------------------------------
% function ModelTFPrior(yTrain, dim_z, params, model_num)
%
%     global MODEL
%     global geneNames

    % TODO: Initialize the model using priors instead of by corrcoef
    % To do this we'll need the TF names, TF links, and Gene Names
    % If cluster 1 has N genes and cluster 2 has M genes and there
    % are k a priori edges between genes in cluster 1 and genes in cluster 2,
    % then the weight in the edge between cluster 1 and cluster 2
    % should be: k/(N*M)

    % We want to read in the files from params, initialize the matrix,
    % and then everything that isn't set gets a weight based on correlation

    % For reading sif file
    %[gene1, direction, gene2] = textread(...
    %    'Arabidopsis/AtRegNet.confirmed.sif', '%s %s %s')

    % For reading tbl file
    %[tf, gene, direction] = textread('Arabidopsis/AtRegNetCompleteTable.tbl',...
    %    '%*s\t%s\t%*s\t%*s\t%s\t%*s\t%*s\t%*s\t%*s\t%s\t%*s\t%*s\t%*s', ...
    %    'delimiter', '\t', 'whitespace', '');

    % For reading Huang-Wen's csv file
%     [genes, evals, tfs] = textread('Arabidopsis/arab-homeodomain-edges.csv',...
%         '%s %s %s', 'delimiter',',', 'whitespace', '');
%
%     % Now we want to find connections in this matrix between our genes.
%     for i=1:length(tfs)
%         if ( sum( strcmp( upper(tfs{i}), upper(geneNames) ) ) > 0 && ...
%                 sum( strcmp( upper(genes{i}), upper(geneNames) ) ) > 0 )
%             % Then gene1 is acting on gene2
%
%             sprintf('%s matches %s at %d', tfs{i}, genes{i}, i)
%         end
%         if mod(i,30000) == 0
%             sprintf('At %d out of %d', i, length(tfs))
%         end
%     end





    % Hack to initialize links using correlation
%     y = [];
%     n_seq_train = length(yTrain);
%     for k = 1:n_seq_train
%       y = [y yTrain{k}];
%     end
%     w = corrcoef(y');
%     MODEL{model_num}.dynamic.w = w / sqrt(dim_z);
%     MODEL{model_num}.dynamic.w = ...
%         MODEL{model_num}.dynamic.w .* params.dynamic_connections;


function ModelApplyDexToWeights(params, model_num)
    global MODEL
    global geneNames

    sprintf('Applying dex to the current network weights\n')

    rfile = textread(params.dex_file, '%s', 'delimiter', '\n');

    weights = MODEL{model_num}.dynamic.w;
    target_index = 0;
    dex_targets = regexp(params.dex_target, ',', 'split');

    target_indexes = [];
    for j=1:length(dex_targets)
        for i=1:length(geneNames)
            if strcmpi(geneNames{i}, dex_targets{j})
                target_indexes(j) = i;
            end
        end
    end

    for k=1:length(dex_targets)
        for i=1:length(rfile)
            l = regexp(rfile{i}, '\t', 'split');
            gene_name = l{1};
            ratio = str2num(l{k});
            idx = 0;
            for j=1:length(geneNames)
                if strcmpi(geneNames{j}, gene_name)
                    idx = j;
                end
            end
            if ratio >= 2.0
                %weights(target_indexes(k), idx) = abs(weights(target_indexes(k), idx)) * ratio;
                weights(target_indexes(k), idx) = 1;
            elseif ratio <= 0.5
                %weights(target_indexes(k), idx) = -abs(weights(target_indexes(k), idx)) * (1-ratio);
                weights(target_indexes(k), idx) = -1;
            end
        end
    end

    MODEL{model_num}.dynamic.w = weights;
    MODEL{model_num}.dynamic.w_init = weights;


function ModelFilePriorWeights(params, model_num)
  global MODEL

  sprintf('Reading in prior from %s\n', params.prior_file)
  % Read in the data file
  rfile = textread(params.prior_file, '%s', 'delimiter', '\n');

  % Split the file on spaces
  rfile = regexp(rfile, ' ', 'split');

  % Read in experiment headers
  geneNames = regexp(rfile{1}, '\t', 'split');
  geneNames = geneNames{1};

  % Now read in gene names and exp values
  prior = zeros(length(geneNames));
  for i=2:size(rfile)
      l = regexp(rfile{i}, '\t', 'split');
      l = l{1};
      geneNames{i-1} = l{1}; % Read in gene name
      for j = 2:length(l)
        val = str2num(l{j});
        if val > 0
          prior(i-1,j-1) = val;
        elseif val < 0
          prior(i-1,j-1) = val;
        else
          % Keep it between 0.2 and -0.2
          prior(i-1,j-1) = (rand * 2 - 1) / 5.0;
        end
      end
  end

  %for i=1:size(prior,1)
      %for j=1:size(prior,2)
          %if(i == j)
            %prior(i,j) = 0;
          %end
      %end
  %end

  MODEL{model_num}.dynamic.w = prior;
  MODEL{model_num}.dynamic.w_init = prior;

  %A = ones(size(prior));
  %A(logical(eye(size(A)))) = 0;

  %MODEL{model_num}.dynamic.connections = logical(A);
  %MODEL{model_num}.dynamic.connections_init = logical(A);

  %params.dynamic_connections = logical(A);


% -------------------------------------------------------------------------
function ModelFilePriorConnections(params, model_num)
  global MODEL

  sprintf('Reading in prior from %s\n', params.prior_file)
  % Read in the data file
  rfile = textread(params.prior_file, '%s', 'delimiter', '\n');

  % Split the file on spaces
  rfile = regexp(rfile, ' ', 'split');

  % Read in experiment headers
  geneNames = regexp(rfile{1}, '\t', 'split');
  geneNames = geneNames{1};

  % Now read in gene names and exp values
  prior = zeros(length(geneNames));
  for i=2:size(rfile)
      l = regexp(rfile{i}, '\t', 'split');
      l = l{1};
      geneNames{i-1} = l{1}; % Read in gene name
      for j = 2:length(l)
          prior(i-1,j-1) = str2num(l{j});
      end
  end

  %for i=1:size(prior,1)
      %for j=1:size(prior,2)
          %if(i == j)
            %prior(i,j) = 0;
          %end
      %end
  %end

  MODEL{model_num}.dynamic.connections = logical(prior);
  MODEL{model_num}.dynamic.connections_init = logical(prior);

% -------------------------------------------------------------------------
function ModelCorrelationPrior(yTrain, dim_z, params, model_num)

global MODEL

% Hack to initialize links using correlation
y = [];
n_seq_train = length(yTrain);
for k = 1:n_seq_train
  y = [y yTrain{k}];
end
w = corrcoef(y');
MODEL{model_num}.dynamic.w = w / sqrt(dim_z);
MODEL{model_num}.dynamic.w = ...
    MODEL{model_num}.dynamic.w .* params.dynamic_connections;


% -------------------------------------------------------------------------
function order = ScrambleSeq(n_seq)
[dummy, order] = sort(rand(1, n_seq));


% -------------------------------------------------------------------------
function MeterUpdateParams(n_m_steps, model_num, eta_coeff)

% Compute the stats
global MODEL
stats_wDyna = ...
  DFG_CharacterizeParams(MODEL{model_num}.dynamic.w, 'wDyna', 1);
fprintf(1, ' ');
stats_bDyna = ...
  DFG_CharacterizeParams(MODEL{model_num}.dynamic.bias, 'bDyna', 1);
fprintf(1, '\n');

% Update the meter
global METER_LEARN
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'wDyna_min', stats_wDyna.min, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'wDyna_normL1', ...
  stats_wDyna.normL1, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'wDyna_normL2', ...
  stats_wDyna.normL2, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'wDyna_max', stats_wDyna.max, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'bDyna_min', stats_bDyna.min, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'bDyna_normL1', ...
  stats_bDyna.normL1, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'bDyna_normL2', ...
  stats_bDyna.normL2, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'bDyna_max', stats_bDyna.max, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'n_m_steps', n_m_steps, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'eta_coeff', eta_coeff, model_num);
METER_LEARN = ...
  DFG_MeterUpdate(METER_LEARN, 'w_sparsity', ...
  stats_wDyna.sparsity, model_num);


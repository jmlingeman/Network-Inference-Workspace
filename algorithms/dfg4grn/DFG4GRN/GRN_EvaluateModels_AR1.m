% GRN_EvaluateModels_AR1
%
% This function evaluates <n_model> trained AR1 models of GRN.
% The following 4 global variables should be present in memory:
%   MODEL
%   METER_LEARN
%   METER_INFER_TRAIN
%   METER_INFER_TEST
% These are cell arrays indexed by the number <model_num> of the instance.
% All metrics normally evaluated during the training and testing of a model
% are also evaluated here, i.e. similarity to a target GRN, trend of
% predictions (percentage correct signs and R2).
%
% The handling of parameters is identical to function GRN_Batch_AR1,
% which means that parameters are defined by script <script_name>
% and thanks to pairs (<parameter_name>, <parameter_value>).
%
% If input <params0> is not empty, then it overwrites above parameters:
% the point of calling <script_name> is merely to initialize data,
% not parameters.
%
% If an alternative dataset is to be substituted to the one in the script,
% use argument pair ('gene_dataset_filename', FILENAME),
% as in ('gene_dataset_filename', 'DREAM3_InSilicoSize10_1.mat').
%
% Syntax:
%   GRN_EvaluateModels_AR1(params0, ...
%                          script_name, dir_res, file_res, varargin)
% Inputs:
%   params0:      parameter struct used in model training, can be empty
%   script_name:  filename of the Matlab script that loads the data
%                 and sets the parameters
%   dir_res:      path to files containing the results
%   file_res:     filename of the file containing the results
%   varargin:     additional arguments, going by pairs

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
% Version 1.0, New York, 25 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: write results to a text file as well
% Revision 2: evaluate model sparsity
%             add <p_level> to <params>
% Revision 3: add data normalization

function GRN_EvaluateModels_AR1(params0, script_name, ...
  dir_res, file_res, varargin)

% Initialization
% --------------

DFG_InitOctave;


% Loading data and parameters
% ---------------------------

% Check for optional dataset filename
for k = 1:(length(varargin)-1)
  if isequal(lower(varargin{k}), 'gene_dataset_filename')
    gene_dataset_filename = varargin{k+1};
  end
end

% Evaluate the script (which loads a dataset, defines a few variables,
% and initializes the parameters)
fprintf(1, 'Trying to evaluate script %s...\n', script_name);
eval(script_name);
if ~exist('params', 'var'), error('Need to define <params>'); end
if ~exist('yTrain', 'var'), error('Need to define <yTrain>'); end
if ~exist('yTest', 'var'), error('Need to define <yTest>'); end
if ~exist('knownTrain', 'var'), error('Need to define <knownTrain>'); end
if ~exist('knownTest', 'var'), error('Need to define <knownTest>'); end
if ~exist('deltaTtrain', 'var'), error('Need to define <deltaTtrain>'); end
if ~exist('deltaTtest', 'var'), error('Need to define <deltaTtest>'); end


% Modify the parameters and normalize data
% ----------------------------------------

% All changes to the parameters are handled by the following function:
params = DFG_Params_Modify(params, varargin);

% Overwrite parameters using those from model training
if ~isempty(params0)
  params = params0;
end

% Normalize the data if required
if (params.normalize_data)
  [yTrain, yTest, params] = GRN_Normalize(yTrain, yTest, params);
end

% Multiplier to deltaZ in the kinetic equation, in the case of non-linear
% activation 'tanh'
if ((params.coeff_delta == 0) && isequal(params.dynamic_transfer, 'tanh'))
  params.coeff_delta = ...
    DFG_Dynamics_AR1_DistribDelta(yTrain, params.tau, deltaTtrain);
end

% Compute mean energies of the signal
[params.dYmean, params.e_mean_signal] = ...
  DFG_Data_MeanSignal(yTrain, yTest, knownTrain, knownTest);


% Create average models
% ---------------------

global MODEL
n_models = length(MODEL);

% Create an average model with statistically significant links
[wAverage, biasAverage, pNet, pNetBinary] = ...
  GRN_AverageModel_AR1(params.p_level);

% Plots
% -----
% 
% if (params.n_steps_display > 0)
%   for k = 1:n_models
%     GRN_Plot(MODEL{k}.dynamic.w, ...
%       sprintf('GRN model %d', k), params.labels_y, []);
%   end
%   GRN_Plot(wAverage, ...
%     'Average GRN', params.labels_y, []);
%   GRN_Plot(wAverage .* pNetBinary, ...
%     'Average GRN, significant links', params.labels_y, []);
% end


% Evaluation of simple average model on data
% ------------------------------------------

% Evaluate the simple average model
Evaluate(yTrain, yTest, knownTrain, knownTest, ...
  deltaTtrain, deltaTtest, params, n_models + 1);

% Evaluate the average model with selected links only
Evaluate(yTrain, yTest, knownTrain, knownTest, ...
  deltaTtrain, deltaTtest, params, n_models + 2);


% Save everything
% ---------------

if ~isequal(lower(dir_res(end)), '/')
  dir_res = [dir_res '/'];
end
try
  mkdir(dir_res);
catch
end
global METER_LEARN
global METER_INFER_TRAIN
global METER_INFER_TEST
save([dir_res file_res], '-mat', 'params', 'MODEL', ...
  'METER_INFER_TRAIN', 'METER_INFER_TEST', 'METER_LEARN', ...
  'wAverage', 'biasAverage', 'pNet', 'pNetBinary');

% Export main results to a text file
GRN_SaveResultsText(params, [dir_res file_res], pNet);

% Exit program.
exit


% -------------------------------------------------------------------------
function Evaluate(yTrain, yTest, knownTrain, knownTest, ...
  deltaTtrain, deltaTtest, params, model_num)

% Infer on the training sequences
global METER_INFER_TRAIN
n_seq_train = length(yTrain);
for j = 1:n_seq_train
  [zStarTrain{j}, yStarTrain{j}, METER_INFER_TRAIN, zStarTrainOut{j}] = ...
    DFG_Infer(yTrain{j}, yTrain{j}, knownTrain{j}, ...
    params, 0, METER_INFER_TRAIN, ...
    sprintf('Eval train %d/%d:', j, n_seq_train), model_num, deltaTtrain{j});
end

% Evaluate the trend errors (% correct signs, R2 Pearson correlation)
METER_INFER_TRAIN = ...
  DFG_MeterUpdate_TrendError(zStarTrain, zStarTrainOut, ...
  params, METER_INFER_TRAIN, model_num);

% Characterize the sparsity of the model
global MODEL
stats_wDyna = ...
  DFG_CharacterizeParams(MODEL{model_num}.dynamic.w, 'wDyna', 1);
METER_INFER_TRAIN = ...
  DFG_MeterUpdate(METER_INFER_TRAIN, 'w_sparsity', ...
  stats_wDyna.sparsity, model_num);

% Infer on the testing sequences
global METER_INFER_TEST
n_seq_test = length(yTest);
for j = 1:n_seq_test
  [zStarTest{j}, yStarTest{j}, METER_INFER_TEST, zStarTestOut{j}] = ...
    DFG_Infer(yTest{j}, yTest{j}, knownTest{j}, ...
    params, 0, METER_INFER_TEST, ...
    sprintf('Eval test %d/%d:', j, n_seq_test), model_num, deltaTtest{j});
end
METER_INFER_TEST = ...
  DFG_MeterUpdate_TrendError(zStarTest, zStarTestOut, ...
  params, METER_INFER_TEST, model_num);

if ~isempty(params.targetGRN)
  % Compare with target GRN
  METER_INFER_TRAIN = ...
    DFG_MeterUpdate_Comparison2TargetGRN(model_num, params, ...
    METER_INFER_TRAIN);
end

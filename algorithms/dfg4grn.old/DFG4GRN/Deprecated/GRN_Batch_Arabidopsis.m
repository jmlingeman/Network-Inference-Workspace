% GRN_Batch_Arabidopsis
%
% This is the entry point for learning one instance of a GRN. When the
% computation is done, the function generates 4 global variables:
%   MODEL
%   METER_LEARN
%   METER_INFER_TRAIN
%   METER_INFER_TEST
% These are cell arrays indexed by the number <model_num> of the instance.
%
% The handling of parameters has been replaced. So, for instance, to call
% the script on the Arabidopsis data, run it as the 12th model,
% with kinetic coefficient tau=3.5,
% latent variable inference rate eta_z=0.1,
% and L1-regularization rate lambda=0.001, and then save results to
% file 'Arabidopsis_12_test.mat',
% one now needs to call:
%
% GRN_Batch_Arabidopsis(12, 'Script_Arabidopsis_LeaveNone', ...
%                       'Arabidopsis_12_test', ...
%                       'eta_z', 0.1, 'tau', 3.5, 'lambda', 0.001);
% 
% Syntax:
%   GRN_Batch_Arabidopsis(model_num, script_name, file_res, varargin)
% Inputs:
%   model_num:    number you give to the model and results, for storage
%   script_name:  filename of the Matlab script that loads the data
%                 and sets the parameters
%   file_res:     filename of the file containing the results
%   varargin:     additional arguments, going by pairs

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

function GRN_Batch_Arabidopsis(model_num, script_name, file_res, varargin)


% Path to the Dynamical Factor Graph (state-space model) library
% --------------------------------------------------------------

path_library = '../DFG4GRN';
addpath(path_library);
% Initialization
DFG_InitOctave;


% Loading data and parameters
% ---------------------------

% Evaluate the script (which loads a dataset and defines a few variables)
fprintf(1, 'Trying to evaluate script %s...\n', script_name);
eval(script_name);
if ~exist('params', 'var'), error('Need to define <params>'); end
if ~exist('yTrain', 'var'), error('Need to define <yTrain>'); end
if ~exist('yTest', 'var'), error('Need to define <yTest>'); end
if ~exist('knownTrain', 'var'), error('Need to define <knownTrain>'); end
if ~exist('knownTest', 'var'), error('Need to define <knownTest>'); end
if ~exist('deltaTtrain', 'var'), error('Need to define <deltaTtrain>'); end
if ~exist('deltaTtest', 'var'), error('Need to define <deltaTtest>'); end


% Additional handling of command line parameters
% ----------------------------------------------

% All the changes to the parameters are handled by the following function:
params = Params_Modify(params, varargin);

% Multiplier to deltaZ in the kinetic equation, in the case of non-linear
% activation 'tanh'
params.coeff_delta = ...
  DFG_Dynamics_AR1_DistribDelta(yTrain, params.tau, deltaTtrain);


% Run the model
% -------------

fprintf(1, 'Computing model %d...\n', model_num);
DFG_Learn(yTrain, knownTrain, yTest, knownTest, ...
  params, model_num, deltaTtrain, deltaTtest);


% Save the results
% ----------------
file_res = sprintf('%s_%d.mat', file_res, model_num);
global MODEL
global METER_LEARN
global METER_INFER_TRAIN
global METER_INFER_TEST
save(file_res, 'MODEL', 'params', ...
  'METER_LEARN', 'METER_INFER_TRAIN', 'METER_INFER_TEST');

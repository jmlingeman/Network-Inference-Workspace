% The structure of the folders is as following:
%   Arabidopsis/
%     contains the Matlab scripts that are called by the main, batch,
%     function. For instance, it contains scripts:
%       Script_Arabidopsis76_Leave1.m
%       Script_Arabidopsis76_Leave1_LARS.m
%     which are used for leave-out-last learning of the Arabidopsis GRN
%     using mRNA measurements from time points 0, 3, 6, 9, 12 and 15min
%     and uses the two last time points (15min and 20min) to evaluate the
%     out-of-sample trend.
%   DFG4GRN/
%     contains all the code for our SSM,
%     minus the LARS library, which needs to be downloaded at:
%   ClusterFrontend/
%     contains functions used running the 

% addpath Arabidopsis/
% addpath DFG4GRN/

% Learn a GRN using an SSM with LARS optimization of the parameters
% and gradient descent on latent variables
% then save it to file 'temp_Arabidopsis76_lars.mat'
GRN_Batch_AR1(1, ...        % This the model number
  'Script_Arabidopsis76_Leave1_LARS', ... % Script name
  'temp_Arabidopsis76_lars', ...          % Filename where data are saved
  'n_steps_display', 1, ... % We want to display results after each epoch
  'tau', 3, ...             % Kinetic time coefficient
  'gamma', 0.1, ...         % Weight of the dynamic error term
  'lambda_w', 0.1);         % L-1 regularization coefficient

% Access the meter global structures
global METER_INFER_TRAIN
global METER_INFER_TEST

% Display the percentage of correct sign predictions on test data
fprintf(1, 'Correct sign predictions: %.2f%%\n', ...
  100*METER_INFER_TEST{1}.last_error_trend_sign);

% Display the fit (SNR in dB) to the train data
fprintf(1, 'Fit to train data: SNR=%.2f (observ.), SNR=%.2f (dynamics)\n', ...
  METER_INFER_TRAIN{1}.last_snr_observation, ...
  METER_INFER_TRAIN{1}.last_snr_dynamic);

% Clean-up
clear all;

% Learn multiple models
GRN_Batch_MultiModel_AR1(20, ...          % Number of models
  'Script_Arabidopsis76_Leave1_LARS', ... % Script name
  'temp_Arabidopsis76', ...               % Directory when data are save
  'temp_file', ...                        % File where data are saved
  'n_steps_display', 10, ...              % Display results every 10 epochs
  'tau', 3, ...                           % Kinetic time coefficient
  'gamma', 0.1, ...                       % Weight of the dynamic error
  'lambda_w', 0.1);                       % L-1 regularization coefficient
% Various plots are generated, and results are stoed both in a Matlab
% and in a text file

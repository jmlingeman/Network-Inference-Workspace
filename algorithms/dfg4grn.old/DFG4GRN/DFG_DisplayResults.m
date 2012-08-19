% DFG_DisplayResults  Display results after training using global variables
%
% Syntax:
%   [best_dyna_sign_test, best_dyna_r2_test] = ...
%     DFG_DisplayResults(model_num, n_seq_train, n_seq_test)
% Inputs:
%   model_num:   model number
%   n_seq_train: number of training sequences
%   n_seq_train: number of testing sequences
% Outputs:
%   best_dyna_sign_test: report the number of correct signs on the test set
%   best_dyna_r2_test:   report the R2 error on the test set
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

function [best_dyna_sign_test, best_dyna_r2_test] = ...
  DFG_DisplayResults(model_num, n_seq_train, n_seq_test)

global METER_LEARN
global METER_INFER_TRAIN
global METER_INFER_TEST
global MODEL

n_measures_eval = length(METER_INFER_TRAIN{model_num}.snr_observation);
n_measures_learn = length(METER_LEARN{model_num}.snr_observation);
n_measures_test = length(METER_INFER_TEST{model_num}.snr_observation);
indXeval = [1:n_measures_eval] / n_seq_train;
indXlearn = [1:n_measures_learn] / n_seq_train;
indXtest = [1:n_measures_test] / n_seq_test;
n_epochs_eval = n_measures_eval / n_seq_train;
n_epochs_learn = n_measures_learn / n_seq_train;
indEpochEval = 1:n_epochs_eval;
indEpochLearn = 1:n_epochs_learn;
best_epoch = MODEL{model_num}.dynamic.best_epoch;

% Collect best results (best training epoch)
best_obs_snr_train = ...
  GetValueEpoch(METER_INFER_TRAIN{model_num}.snr_observation, ...
  n_seq_train, best_epoch);
best_obs_snr_test = ...
  GetValueEpoch(METER_INFER_TEST{model_num}.snr_observation, ...
  n_seq_test, best_epoch);
best_dyna_snr_train = ...
  GetValueEpoch(METER_INFER_TRAIN{model_num}.snr_dynamic, ...
  n_seq_train, best_epoch);
best_dyna_snr_test = ...
  GetValueEpoch(METER_INFER_TEST{model_num}.snr_dynamic, ...
  n_seq_test, best_epoch);
best_dyna_sign_train = ...
  GetValueEpoch(METER_INFER_TRAIN{model_num}.error_trend_sign, ...
  n_seq_train, best_epoch);
best_dyna_sign_test = ...
  GetValueEpoch(METER_INFER_TEST{model_num}.error_trend_sign, ...
  n_seq_test, best_epoch);
best_dyna_r2_train = ...
  GetValueEpoch(METER_INFER_TRAIN{model_num}.error_trend_r2, ...
  n_seq_train, best_epoch);
best_dyna_r2_test = ...
  GetValueEpoch(METER_INFER_TEST{model_num}.error_trend_r2, ...
  n_seq_test, best_epoch);


figure;
subplot(2, 2, 1);
hold on;
plot(indXlearn, METER_LEARN{model_num}.snr_observation, 'g-');
plot(indXeval, METER_INFER_TRAIN{model_num}.snr_observation, 'b-');
plot(indXtest, METER_INFER_TEST{model_num}.snr_observation, 'r-');
plot(indEpochLearn, ...
  SubSample(METER_LEARN{model_num}.snr_observation, n_seq_train), ...
  'y-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TRAIN{model_num}.snr_observation, n_seq_train), ...
  'c-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TEST{model_num}.snr_observation, n_seq_test), ...
  'm-', 'LineWidth', 2);
plot(indEpochEval(best_epoch) * [1 1], [-30 50], 'k-');
title(sprintf('Observation SNR, per epoch and seq\ntr %.1f, te %.1f', ...
  best_obs_snr_train, best_obs_snr_test));
ylabel('SNR (dB)');
set(gca, 'YLim', [0 50], 'YTick', [0:2:50]);
set(gca, 'XTick', [0:50:n_epochs_eval], 'XLim', [0 n_epochs_eval]);
grid on;


subplot(2, 2, 2);
hold on;
plot(indXlearn, METER_LEARN{model_num}.snr_dynamic, 'g-');
plot(indXeval, METER_INFER_TRAIN{model_num}.snr_dynamic, 'b-');
plot(indXtest, METER_INFER_TEST{model_num}.snr_dynamic, 'r-');
plot(indEpochLearn, ...
  SubSample(METER_LEARN{model_num}.snr_dynamic, n_seq_train), ...
  'y-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TRAIN{model_num}.snr_dynamic, n_seq_train), ...
  'c-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TEST{model_num}.snr_dynamic, n_seq_test), ...
  'm-', 'LineWidth', 2);
plot(indEpochEval(best_epoch) * [1 1], [-30 50], 'k-');
title(sprintf('Dynamic SNR, per epoch and seq\ntr %.1f, te %.1f', ...
  best_dyna_snr_train, best_dyna_snr_test));
ylabel('SNR (dB)');
set(gca, 'YLim', [-10 50], 'YTick', [-10:2:50]);
set(gca, 'XTick', [0:50:n_epochs_eval], 'XLim', [0 n_epochs_eval]);
grid on;



subplot(2, 2, 3);
hold on;
plot(indXlearn, 100 * METER_LEARN{model_num}.error_trend_sign, 'g-');
plot(indXeval, 100 * METER_INFER_TRAIN{model_num}.error_trend_sign, 'b-');
plot(indXtest, 100 * METER_INFER_TEST{model_num}.error_trend_sign, 'r-');
plot(indEpochLearn, ...
  100 * SubSample(METER_LEARN{model_num}.error_trend_sign, n_seq_train), ...
  'y-', 'LineWidth', 2);
plot(indEpochEval, ...
  100 * SubSample(METER_INFER_TRAIN{model_num}.error_trend_sign, n_seq_train), ...
  'c-', 'LineWidth', 2);
plot(indEpochEval, ...
  100 * SubSample(METER_INFER_TEST{model_num}.error_trend_sign, n_seq_test), ...
  'm-', 'LineWidth', 2);
plot(indEpochEval(best_epoch) * [1 1], [0 100], 'k-');
title(sprintf('Trend error sign, per epoch and seq\ntr %.2f, te %.2f', ...
  best_dyna_sign_train, best_dyna_sign_test));
ylabel('Trend error (%)');
set(gca, 'YLim', [0 100], 'YTick', [0:10:100]);
set(gca, 'XTick', [0:50:n_epochs_eval], 'XLim', [0 n_epochs_eval]);
grid on;

subplot(2, 2, 4);
hold on;
plot(indXlearn, METER_LEARN{model_num}.error_trend_r2, 'g-');
plot(indXeval, METER_INFER_TRAIN{model_num}.error_trend_r2, 'b-');
plot(indXtest, METER_INFER_TEST{model_num}.error_trend_r2, 'r-');
plot(indEpochLearn, ...
  SubSample(METER_LEARN{model_num}.error_trend_r2, n_seq_train), ...
  'y-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TRAIN{model_num}.error_trend_r2, n_seq_train), ...
  'c-', 'LineWidth', 2);
plot(indEpochEval, ...
  SubSample(METER_INFER_TEST{model_num}.error_trend_r2, n_seq_test), ...
  'm-', 'LineWidth', 2);
plot(indEpochEval(best_epoch) * [1 1], [-2 1], 'k-');
title(sprintf('Trend error R^2, per epoch and seq\ntr %.2f, te %.2f', ...
  best_dyna_r2_train, best_dyna_r2_test));
ylabel('R^2');
set(gca, 'YLim', [-2 1], 'YTick', [-2:0.1:1]);
set(gca, 'XTick', [0:50:n_epochs_eval], 'XLim', [0 n_epochs_eval]);
grid on;


% -------------------------------------------------------------------------
function y = SubSample(x, n)

len = length(x);
len_sub = ceil(len / n);
y = zeros(1, len_sub);
for k = 1:len_sub
  a = (k-1) * n + 1;
  b = min(k * n, len);
  y(k) = mean(x(a:b));
end


% -------------------------------------------------------------------------
function val = GetValueEpoch(x, n, epoch)

y = SubSample(x, n);
val = y(epoch);

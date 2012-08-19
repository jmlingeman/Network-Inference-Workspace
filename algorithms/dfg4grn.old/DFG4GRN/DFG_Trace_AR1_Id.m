% DFG_Trace_AR1_Id  Trace the results during learning
%
% Syntax:
%   DFG_Trace_AR1_Id(y, zStar, k, n_epochs, model_num)

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

% Revision 1: display F1, Jaccard and AUC of ROC if known GRN
%             use red/blue colormap
%             monitor the direction of predictions
% Revision 2: bug when missing target GRN
%             show gene names

function DFG_Trace_AR1_Id(y, zStar, zStarOut, k, n_epochs, model_num, ...
  n_seq_train, n_seq_test, params)

global MODEL
global METER_INFER_TRAIN
global METER_INFER_TEST
global METER_LEARN

% Plot the matrix weights
% -----------------------

figure(1);
subplot(2, 1, 1);
axis square;
mat = MODEL{model_num}.dynamic.w;
val_max = max(abs(mat(:)));
mat = (mat / val_max) * 32 + 32;
image(mat);
map = ...
  [(0:(1/31):1) ones(1, 32); ...
  (0:(1/31):1) (1:(-1/31):0); ...
  ones(1, 32) 1:(-1/31):0];
colormap(map');

% Check for forbidden connections
if (any(any((~MODEL{model_num}.dynamic.connections & ...
    (MODEL{model_num}.dynamic.w ~= 0)))))
  warning('Found forbidden connections');
end

% Put the gene labels, if possible
n_genes = size(mat, 1);
n_tfs = size(mat, 2);
set(gca, 'YTick', [1:n_genes], 'XTick', [1:n_tfs]);
grid on;

% Make title
str = ...
  sprintf('GRN: sparsity=%.3f', METER_LEARN{model_num}.last_w_sparsity);
if ~isempty(params.targetGRN)
  str = ...
    sprintf('%s, AUROC=%.3f AUPR=%.3f (optimal F1=%.3f, Jaccard=%.3f)', ...
    str, METER_INFER_TRAIN{model_num}.last_target_auroc, ...
    METER_INFER_TRAIN{model_num}.last_target_aupr, ...
    METER_INFER_TRAIN{model_num}.last_target_F1, ...
    METER_INFER_TRAIN{model_num}.last_target_Jaccard);
end

% Plot target GRN, if present
if ~isempty(params.targetGRN)
  n_genes = size(params.targetGRN, 1);
  n_tfs = size(params.targetGRN, 2);
  [xs, ys] = meshgrid(1:n_tfs, 1:n_genes);
  ind = find(params.targetGRN);
  hold on;
  plot(xs(ind), ys(ind), 'kx', 'LineWidth', 2, 'MarkerSize', 6);
end

% colorbar;
title(str);
xlabel('TFs');
ylabel('Genes');

% We will compute averages over the training and testing
indTrain = ...
  length(METER_INFER_TRAIN{model_num}.snr_dynamic) - [0:(n_seq_train-1)];
indTest = ...
  length(METER_INFER_TEST{model_num}.snr_dynamic) - [0:(n_seq_test-1)];

% Plot the targets (blue), latent variables (red) and predictions (green)
subplot(2, 1, 2);
cla; hold on;
len_total = 0;
n_seq = length(y);
for j = 1:n_seq
  len_seq = size(y{j}, 2);
  plot(len_total + [1:len_seq], y{j}', 'b', ...
    len_total + [1:len_seq], zStar{j}', 'r');
  for i = 1:(len_seq-1)
    plot(len_total + [i (i+1)], [y{j}(:, i) zStarOut{j}(:, i)], 'g');
  end
  len_total = len_total + len_seq;
end
str = sprintf('Epoch %d/%d train: e_{Dyna}=%5.2fdB (%5.2fdB) ', ...
  k, n_epochs, ...
  mean(METER_INFER_TRAIN{model_num}.snr_dynamic(indTrain)), ...
  mean(METER_LEARN{model_num}.snr_dynamic(indTrain)));
str = sprintf('%se_{Obs}=Inf (%5.2fdB)', ...
  str, mean(METER_LEARN{model_num}.snr_observation(indTrain)));
str = sprintf('%s sign=%4.1f%% (all %4.1f%%) R^2=%.2f (all %.2f)', str, ...
  100 * METER_INFER_TRAIN{model_num}.last_error_trend_sign, ...
  100 * METER_INFER_TRAIN{model_num}.last_error_trend_sign_all, ...
  METER_INFER_TRAIN{model_num}.last_error_trend_r2, ...
  METER_INFER_TRAIN{model_num}.last_error_trend_r2_all);
str = sprintf('%s\ntest: e_{Dyna}=%5.2fdB e_{Obs}=%5.2fdB', str, ...
  mean(METER_INFER_TEST{model_num}.snr_dynamic(indTest)), ...
  mean(METER_INFER_TEST{model_num}.snr_observation(indTest)));
str = sprintf('%s sign=%4.1f%% (all %4.1f%%) R^2=%.2f (all %.2f)', str, ...
  100 * METER_INFER_TEST{model_num}.last_error_trend_sign, ...
  100 * METER_INFER_TEST{model_num}.last_error_trend_sign_all, ...
  METER_INFER_TEST{model_num}.last_error_trend_r2, ...
  METER_INFER_TEST{model_num}.last_error_trend_r2_all);
title(str);

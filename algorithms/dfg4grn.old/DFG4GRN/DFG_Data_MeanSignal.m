% DFG_Data_MeanSignal  Compute mean energy and mean delta
%
% Syntax:
%   [dYmean, e_mean_signal] = ...
%     DFG_Data_MeanSignal(yTrain, yTest, yKnownTrain, yKnownTest)
% Inputs:
%   yTrain:      matrix of observed values, size <N> x <T1>, or cell array
%   yTest:       matrix of observed values, size <N> x <T2>, or cell array
%   yKnownTrain: matrix of indicators, size <N> x <T1>, or cell array
%   yKnownTest:  matrix of indictaors, size <N> x <T2>, or cell array
% Outputs:
%   dYmean:        vector of mean deltas, size <N> x <1>
%   e_mean_signal: mean square energy
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

function [dYmean, e_mean_signal] = ...
  DFG_Data_MeanSignal(yTrain, yTest, yKnownTrain, yKnownTest)

% Compute mean energies of the signal
yAll = [];
dYall = [];

n_seq_train = length(yTrain);
n_samples_train = 0;
for j = 1:n_seq_train
  n_samples_train = n_samples_train + size(yTrain{j}, 2);
  yAll = [yAll (yTrain{j} .* yKnownTrain{j})];
  dYall = [dYall diff(yTrain{j}, 1, 2)];
end
dim_y = size(yAll, 1);
fprintf(1, 'Training: %d seq of %d variables (%d samples)\n', ...
  n_seq_train, dim_y, n_samples_train);

n_seq_test = length(yTest);
n_samples_test = 0;
for j = 1:n_seq_test
  n_samples_test = n_samples_test + size(yTest{j}, 2);
  yAll = [yAll (yTest{j} .* yKnownTest{j})];
  dYall = [dYall diff(yTest{j}, 1, 2)];
end
fprintf(1, 'Test: %d seq of %d variables (%d samples)\n', ...
  n_seq_test, dim_y, n_samples_test);

n_samples = n_samples_train + n_samples_test;
dYmean = mean(dYall, 2);
e_mean_signal = 0.5 * sum(sum(yAll.^2)) / (dim_y * n_samples);

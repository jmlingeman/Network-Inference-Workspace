% GRN_Normalize  Normalize data to zero mean and unit standard deviation
%
% Syntax:
%   [yTrain, yTest, params] = GRN_Normalize(yTrain, yTest, params)
% Inputs:
%   yTrain:      matrix of observerd values, size <N> x <T1>, or cell array
%   yTest:       matrix of observerd values, size <N> x <T2>, or cell array
%   params:      parameters struct
% Outputs:
%   yTrain:      matrix of observerd values, size <N> x <T1>, or cell array
%   yTest:       matrix of observerd values, size <N> x <T2>, or cell array
%   params:      parameters struct with fields <yMean> and <yStd>

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

function [yTrain, yTest, params] = GRN_Normalize(yTrain, yTest, params)

n_seq_train = length(yTrain);
n_seq_test = length(yTest);

% Shall we use existing mean and standard deviation (in the parameters)?
if (isfield(params, 'yMean') && isfield(params, 'yStd'))
  yMean = params.yMean;
  yStd = params.yStd;
else
  % Compute mean and standard deviation on training sequences
  y = [];
  for k = 1:n_seq_train
    y = [y yTrain{k}];
  end
  yMean = mean(y, 2);
  % We normalize by the sample size
  yStd = std(y, 1, 2);
end

% Normalize training sequences
for k = 1:n_seq_train
  len = size(yTrain{k}, 2);
  yTrain{k} = (yTrain{k} - repmat(yMean, 1, len)) ./ repmat(yStd, 1, len);
end

% Normalize training sequences
for k = 1:n_seq_test
  len = size(yTest{k}, 2);
  yTest{k} = (yTest{k} - repmat(yMean, 1, len)) ./ repmat(yStd, 1, len);
end

% Store in the parameters
params.yMean = yMean;
params.yStd = yStd;

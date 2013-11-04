% DFG_Data_Normalize  Normalize/standardize data
%
% Standardize data so that each element/gene has 0 mean and unit variance.
%
% Syntax:
%   [yNorm, m, s] = DFG_Data_Normalize(y)
% Inputs:
%   y:     cell array of input data (matrices of size <N> x <T>) 
% Outputs:
%   yNorm: normalized (standardized) version of <y>
%   m:     vector of size <N> x 1 of mean values per variable
%   s:     vector of size <N> x 1 of standard deviation values per variable

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

function [yNorm, m, s] = DFG_Data_Normalize(y)

% Dimensions
if iscell(y)
  n_seq = length(y);
else
  n_seq = 1;
  y = {y};
end

% Compute the mean and standard deviation vectors over all the sequences
data = [];
for k = 1:n_seq
  data = [data y{k}];
end
n_samples = size(data, 2);
m = mean(data, 2);
data = data - repmat(m, 1, n_samples);
s = sqrt(mean(data.^2, 2));

% Standardize the inputs
yNorm = cell(1, n_seq);
for k = 1:n_seq
  len_k = size(y{k}, 2);
  yNorm{k} = y{k} - repmat(m, 1, len_k);
  yNorm{k} = yNorm{k} ./ repmat(s, 1, len_k);
end

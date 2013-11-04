% GRN_CreateCombinations
%   Create replicate-based combinations of micro-array data
%
% Syntax:
%   [nComb, KNO3, KCl, ratio] = ...
%     GRN_CreateCombinations(geneNames, dataKNO31, dataKNO32, ...
%                            [dataKCl1, dataKCl2])
%
% Inputs:
%   <geneNames>  : cell of size <nGenes> with gene names
%   <dataKNO31>  : matrix of size <nGenes> x <nTimes> containing
%                  micro-array data for experiments on the reaction to KN03
%                  for replicate 1
%   <dataKNO32>  : matrix of size <nGenes> x <nTimes> containing
%                  micro-array data for experiments on the reaction to KN03
%                  for replicate 2
%   <dataKCl1>   : matrix of size <nGenes> x <nTimes> containing
%                  micro-array data for experiments on the reaction to KCl
%                  for replicate 1 (optional)
%   <dataKCl2>   : matrix of size <nGenes> x <nTimes> containing
%                  micro-array data for experiments on the reaction to KCl
%                  for replicate 2 (optional)
%
% Return:
%   <nComb>  : number of combinations of replicates 
%              (for 2 replicates A and B, there are 4 combinations for
%              a time series of 5 time points:
%              AAAAA, ABABA, BBBBB, BABAB
%   <KNO3>   : cell array with <nComb> cells, one for each combination 
%              of replicates, each containing a matrix of size 
%              <nGenes> x <nTimes> with micro-array data for KNO3
%   <KCL>    : cell array with <nComb> cells, one for each combination 
%              of replicates, each containing a matrix of size 
%              <nGenes> x <nTimes> with micro-array data for KCl (optional)
%   <ratio>  : cell array with <nComb> cells, one for each combination 
%              of replicates, each containing a matrix of size 
%              <nGenes> x <nTimes> with micro-array data for KNO3/KCl (optional)
%
% Copyright (C) 2009 Piotr Mirowski
%                    Courant Institute of Mathematical Sciences
%                    New York University

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
% Version 1.0, New York, 5 July 2009
% (c) 2009, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [nComb, KNO3, KCl] = ...
  GRN_CreateCombinations(geneNames, dataKNO31, dataKNO32, ...
                         dataKCl1, dataKCl2)

nGenes = length(geneNames);

dataKNO3{1} = log2(dataKNO31);
dataKNO3{2} = log2(dataKNO32);
if nargin == 5
  dataKCl{1} = log2(dataKCl1);
  dataKCl{2} = log2(dataKCl2);
end

% Create new series by combining replicates
lenComb = size(dataKNO31, 2);
switch lenComb
  case 5
    comb = ['11111'; '12121'; '21212'; '22222'];
  case 7
    comb = ['1111111'; '1212121'; '2121212'; '2222222'];
  otherwise
    error('Not implemented');
end
nComb = size(comb, 1);

% Initialize output data
KNO3 = cell(nComb, 1);
if nargin == 5
  KCl = cell(nComb, 1);
  ratio = cell(nComb, 1);
end

% Loop over all combinations
for k = 1:nComb
  KNO3{k} = zeros(nGenes, lenComb);
  if nargin == 5
    KCl{k} = zeros(nGenes, lenComb);
  end
  c = comb(k, :);
  for j = 1:lenComb
    if (c(j) == '1')
      KNO3{k}(:, j) = dataKNO3{1}(:, j);
      if nargin == 5
        KCl{k}(:, j) = dataKCl{1}(:, j);
        ratio{k}(:, j) = dataKNO3{1}(:, j) - dataKCl{1}(:, j);
      end
    else
      KNO3{k}(:, j) = dataKNO3{2}(:, j);
      if nargin == 5
        KCl{k}(:, j) = dataKCl{2}(:, j);
        ratio{k}(:, j) = dataKNO3{2}(:, j) - dataKCl{2}(:, j);
      end
    end
  end
end

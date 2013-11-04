% GRN_StatisticallySignificantLinks  Computate statistically signif. links
%
% Given a set of same-size matrices representing gene-tf interactions
% (links), compute the average matrix of links, and check each link value
% whether it is statistically significant at a specified <p_level>.
% The distribution of average link values is obtained in a non-parametric
% way by random permutations of observed link matrices.
%
% Syntax:
%   [pNet, pNetBinary, net] = ...
%     GRN_StatisticallySignificantLinks(nets, p_level)
% Inputs:
%   nets:    cell array of <K> matrices of size <N> x <M>
%   p_level: scalar between 0 and 0.5, typically 0.001
% Outputs:
%   pNet:       matrix of size <N> x <M> of P-values
%   pNetBinary: boolean matrix of size <N> x <M> (is there a link or not?)
%   net:        matrix of size <N> x <M> of average link values

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
% Version 1.0, New York, 24 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [pNet, pNetBinary, net] = ...
  GRN_StatisticallySignificantLinks(nets, p_level)

sizeNet = size(nets{1});
n_genes = sizeNet(1);
n_tfs = sizeNet(2);

% Initialize the scrambled nets
n_scrambles = 1000;
netsScrambled = zeros(n_genes, n_tfs, n_scrambles);
net = zeros(sizeNet);

% Loop on all the supplied nets
fprintf(1, 'Randomly permuting nets');
n_models = numel(nets);
for k = 1:n_models
  fprintf(1, '.');
  netK = nets{k};

  % Compute 1000 scrambled versions of that GRN
  % and add them to the scrambled nets
  for l = 1:n_scrambles
    netL = GRN_ScrambleMat(netK);
    netsScrambled(:, :, l) = netsScrambled(:, :, l) + netL;
  end

  % Compute the sum of the links
  net = net + nets{k};
end
fprintf(1, '\n');

% Compute the averages (over models) of the (scrambled) net(s)
net = net / n_models;
netsScrambled = netsScrambled / n_models;

% P-values of the average GRN net
% Compute the matrix of significance of the average GRN net
fprintf(1, 'Computing p-values\n');
pNet = zeros(sizeNet);
for i = 1:n_genes
  fprintf(1, 'Gene %4d/%4d\n', i, n_genes);
  for j = 1:n_tfs
    
    % Get the list of scrambled edge values for gene <i> and TF <j>
    listS = squeeze(netsScrambled(i, j, :));
    
    % Compute P-value for edge (i, j)
    w_ij = net(i, j);
    if w_ij > 0
      p_ij = sum(listS > w_ij) / n_scrambles;
    else
      p_ij = sum(listS < w_ij) / n_scrambles;
    end
    pNet(i, j) = p_ij;
  end
end

% Compute the significant nets at <p_level>
pNetBinary = (pNet < p_level);

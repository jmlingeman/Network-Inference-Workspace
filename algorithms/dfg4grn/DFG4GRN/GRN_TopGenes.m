% GRN_TopGenes  Compute top regulators and regulated genes
%
% Syntax:
%   [nIn, nOut, topIn, topOut, strIn, strOut] = GRN_TopGenes(pNet, params)
% Inputs:
%   
% Outputs:
%   pNet:   matrix of size <N> x <M> of P-values
%   params: parameters struct
% Outputs:
%   nIn:    vector of size 1 x <N> of # regulators for each gene
%   nOut:   vector of size <N> x 1 of # regulated targets by gene
%   topIn:  vector of size 1 x <N> of # regulators for each gene
%   topOut: vector of size <N> x 1 of # regulated targets by gene
%   strIn:  string with top 10 (or less) regulated genes
%   strOut: string with top 10 (or less) gene regulators

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
% Version 1.0, New York, 29 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [nIn, nOut, topIn, topOut, strIn, strOut] = ...
  GRN_TopGenes(pNet, params)

n_genes = size(pNet, 1);
p_level = params.p_level;
geneNames = params.labels_y;
n_tfs = params.n_tfs;

% Number of regulations for each gene
if (params.verbosity_level > 0)
  fprintf(1, 'At %.3f%% significance:\n', 1 - p_level);
end
nIn = zeros(1, n_genes);
nOut = zeros(n_genes, 1);
n_genes_remove = 0;
for i = 1:n_genes
  nIn(i) = sum(pNet(i, :) < p_level);
  nOut(i) = 0;
  if (params.verbosity_level > 0)
    fprintf(1, '%s: %d inlinks', geneNames{i}, nIn(i));
  end
  if (i <= n_tfs)
    nOut(i) = sum(pNet(:, i) < p_level);
    if (params.verbosity_level > 0)
      fprintf(1, ', %d outlinks', nOut(i));
    end
  end
  if (nIn(i) == 0) && (nOut(i) == 0)
    if (params.verbosity_level > 0)
      fprintf(1, ' [remove]');
    end
    n_genes_remove = n_genes_remove + 1;
  end
  if (params.verbosity_level > 0)
    fprintf(1, '\n');
  end
end
if (params.verbosity_level > 0)
  fprintf(1, '%d genes could be removed at %.3f%% significance\n', ...
    n_genes_remove, 1 - p_level);
end

% Top regulators
[dummy, topIn] = sort(nIn, 'descend');
[dummy, topOut] = sort(nOut, 'descend');
strIn = '';
strOut = '';
for i = 1:(min(10, n_genes))
  j = topIn(i);
  strIn = sprintf('%s%s;', strIn, geneNames{j});
end
for i = 1:(min(10, n_genes))
  j = topOut(i);
  strOut = sprintf('%s%s;', strOut, geneNames{j});
end
if (params.verbosity_level > 0)
  fprintf(1, 'Top regulated: %s\n', strIn);
  fprintf(1, 'Top regulators: %s\n', strOut);
end

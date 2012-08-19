% GRN_AnalyzeNets
%   Analyze the GRN network from a series of experiments, and compute the
%   common (core) network
%
%   Looks for all files in <pathResults>:
%   * network matrices (Lush files in *_s.mat)
%   * statistics on the correct signs of deltas,
%     on both the training and testing sets
%    (files in *_deltaPastDYNAtr.csv and *_deltaPastDYNAte.csv)
%
%   The network matrices are of size <nGenes> x <nTFs>, 
%   where the first <nTFs> genes are transcription factors.
%
% Syntax:
%   sumNet = ...
%    GRN_AnalyzeNets(pathResults, geneNames, TFnames[, nTFperGene])
%
% Inputs:
%   <pathResults>  : (string) path to the directory will all the results
%   <geneNames>    : cell of length <nGenes> with gene names
%   <TFnames>      : cell of length <nTFs> with transcription factors names
%   [<nTFperGene>] : expected number of TFs per gene (default 4)
%
% Return:
%   <sumNet>  : network (matrix) of average interaction strengths 
%               between a TF and a gene
%   <pNet99>  : binary matrix of statistically significant connections
%               at 99% significance
%   <pNet95>  : binary matrix of statistically significant connections
%               at 95% significance
%   <signNet> : binary matrix of TF-gene interactions of constant sign
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
% Version 1.0, New York, 3 July 2009
% (c) 2009, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [sumNet, pNet999, pNet99, pNet95, signNet] = ...
  GRN_AnalyzeNets(pathResults, geneNames, TFnames, nTFperGene)


% Initialization
% --------------

% Number of genes and TFs
nGenes = length(geneNames);
nTFs = length(TFnames);

% Default number of TF per gene
if (nargin < 4)
  nTFperGene = 4;
end

% Load the nets (models)
global MODEL
load(pathResults);
n_models = length(MODEL);

% Initialize the sum of GRN matrices
sumNet = zeros(size(MODEL{1}.dynamic.best_w));

% Initialize the sum of signs of GRN matrices
sumSignsNet = zeros(size(MODEL{1}.dynamic.best_w));

% Initialize the scrambled nets
nScrambles = 1000;
for j = 1:nScrambles
  netsScrambled{j} = zeros(size(MODEL{1}.dynamic.best_w));
end


% Loop on all the resulting models
% --------------------------------
for k = 1:n_models

  % Get the GRN net and its top links
  nets{k} = MODEL{k}.dynamic.best_w;
  
  % Compute 100 scrambled versions of that GRN
  % and add them to the scrambled nets
  for l = 1:nScrambles
    netL = GRN_ScrambleMat(nets{k});
    netsScrambled{l} = netsScrambled{l} + netL;
  end
    
  % Compute the intersection of the top links, 
  % as well as the sum of the links
  sumNet = sumNet + nets{k};
  sumSignsNet = sumSignsNet + sign(nets{k});
end


% Average GRN net
% ---------------

% Plot the average GRN net
sumNet = sumNet / n_models;
try
  GRN_Plot(sumNet, ['Average regulation strength for ' pathResults], ...
    geneNames, TFnames);
catch
end
saveas(gcf, [pathResults '_AverageNet.pdf']);


% P-values of the average GRN net
% -------------------------------

% Compute the average of the scrambled nets
for k = 1:nScrambles
  netsScrambled{k} = netsScrambled{k} / n_models;
end

% Compute the matrix of significance of the average GRN net
pNet = zeros(size(MODEL{1}.dynamic.best_w));
for i = 1:nGenes
  for j = 1:size(pNet, 2)
    
    % Get the list of scrambled edge values for gene i and TF j
    listS = zeros(1, nScrambles);
    for k = 1:nScrambles
      listS(k) = netsScrambled{k}(i, j);
    end
    
    % Sort that list and compute P-value for edge (i, j)
    w_ij = sumNet(i, j);
    listS = sort(listS);
    if w_ij > 0
      p_ij = sum(listS > w_ij) / nScrambles;
    else
      p_ij = sum(listS < w_ij) / nScrambles;
    end
    pNet(i, j) = p_ij;
  end
end

% Compute the significant nets at 1% and 5%
pNet999 = (pNet < 0.001);
pNet99 = (pNet < 0.01);
pNet95 = (pNet < 0.05);

% Plot the significant links for the average GRN net
try
  GRN_Plot(pNet, ['P-values for links in ' pathResults], ...
    geneNames, TFnames);
catch
end
colormap('gray');
saveas(gcf, [pathResults '_pValues.pdf']);
try
  GRN_Plot(pNet95, ['95% significant regulations for ' pathResults], ...
    geneNames, TFnames);
catch
end
saveas(gcf, [pathResults '_0.95significant.pdf']);
try
  GRN_Plot(pNet99, ['99% significant regulations for ' pathResults], ...
    geneNames, TFnames);
catch
end
saveas(gcf, [pathResults '_0.99significant.pdf']);
try
  GRN_Plot(pNet999, ['99.9% significant regulations for ' pathResults], ...
    geneNames, TFnames);
catch
end
saveas(gcf, [pathResults '_0.999significant.pdf']);
fprintf(1, '%d/%d gene interactions at 95%% significance\n', ...
  sum(pNet95(:)), nGenes*nTFs);
fprintf(1, '%d/%d gene interactions at 99%% significance\n', ...
  sum(pNet99(:)), nGenes*nTFs);
fprintf(1, '%d/%d gene interactions at 99.9%% significance\n', ...
  sum(pNet999(:)), nGenes*nTFs);

% Number of regulations for each gene 
fprintf(1, 'At 99%% significance:\n');
nGenes2Remove = 0;
for i = 1:nGenes
  fprintf(1, '%s: ', geneNames{i});
  nIn = sum(pNet99(i, :));
  fprintf(1, '%d inlinks', nIn);
  nOut = 0;
  if (i <= nTFs)
    nOut = sum(pNet99(:, i));
    fprintf(1, ', %d outlinks', nOut);
  end
  if (nIn == 0) && (nOut == 0)
    fprintf(1, ' [remove]');
    nGenes2Remove = nGenes2Remove + 1;
  end
  fprintf(1, '\n');
end
fprintf(1, '%d genes could be removed at 99%% significance\n', ...
  nGenes2Remove);

% Number of regulations for each gene 
fprintf(1, 'At 99.9%% significance:\n');
nGenes2Remove = 0;
for i = 1:nGenes
  fprintf(1, '%s: ', geneNames{i});
  nIn = sum(pNet999(i, :));
  fprintf(1, '%d inlinks', nIn);
  nOut = 0;
  if (i <= nTFs)
    nOut = sum(pNet999(:, i));
    fprintf(1, ', %d outlinks', nOut);
  end
  if (nIn == 0) && (nOut == 0)
    fprintf(1, ' [remove]');
    nGenes2Remove = nGenes2Remove + 1;
  end
  fprintf(1, '\n');
end
fprintf(1, '%d genes could be removed at 99.9%% significance\n', ...
  nGenes2Remove);


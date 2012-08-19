% DFG_Params_Default_Arabidopsis  Initialize default values of parameters
%
% Syntax:
%   params = DFG_Params_Default_Arabidopsis(n_genes, n_tfs, geneNames)
% Inputs:
%   n_genes:   number of genes
%   n_tfs:     number of transcription factors, smaller than <n_genes>
%   geneNames: cell array of <n_genes> gene names

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

function params = DFG_Params_Default_Arabidopsis(n_genes, n_tfs, geneNames)

% Initialize the parameters
params = DFG_Params_Default(n_genes);

% Modify default parameters
params.lambda_w = 5e-3;
params.n_max_m_steps = 100;
params.n_max_e_steps = 100;
params.n_steps_save = 0;
params_n_steps_archive = 20;

% Select subset of TFs
if (nargin > 1)
  params.dynamic_connections(:, (n_tfs+1):end) = 0;
end

% Set gene names
if (nargin > 2)
  params.labels_y = geneNames;
end

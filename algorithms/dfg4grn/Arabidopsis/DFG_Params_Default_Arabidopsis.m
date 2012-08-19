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

% Revision 1: Corrected small typo with params.n_steps_archive
% Revision 2: Save number of transcription factors

function params = DFG_Params_Default_Arabidopsis(n_genes, n_tfs, geneNames)

% Initialize the parameters
params = DFG_Params_Default(n_genes);

% Select subset of TFs
if (nargin > 1)
  params.dynamic_connections(:, (n_tfs+1):end) = 0;
else
  n_tfs = n_genes;
end
params.n_tfs = n_tfs;

% Set gene names
if (nargin > 2)
  params.labels_y = geneNames;
end


% Limit the number of disk access and verbosity, no plot
params.n_steps_display = 0;
params.n_steps_save = 0;
params.n_steps_archive = 0;
params.verbosity_level = 1;

% Linear dynamics for Arabidopsis
params.dynamic_transfer = 'linear';
params.coeff_delta = 1;


% Default configuration (different scripts might have different config.)
params.normalize_data = 0;
params.pde_type = 'kinetic';
params.dynamic_algorithm = 'gradient';

% Do not use a correlation prior to initialize the GRN matrix
params.correlation_prior = 0;

% Be conservative with learning and inference
params.n_max_e_steps = 100;
params.n_max_m_steps = 100;
params.n_epochs = 100;


% Parameters that could be tuned: here are their default values
params.eta_w = 0.1;
params.eta_z = 0.01;

% Parameters that should be optimized
params.lambda_w = 1e-4;
params.gamma = 1;
params.tau = 6;



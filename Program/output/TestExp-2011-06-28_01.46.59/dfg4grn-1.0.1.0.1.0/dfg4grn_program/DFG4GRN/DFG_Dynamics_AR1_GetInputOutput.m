% DFG_Dynamics_AR1_GetInputOutput  Define input/output for AR1 dynamics
%
% Defines as input the concatenation of relevant subsequences from all 
% latent sequences, and same for the output.
%
% Syntax:
%   [zIn, zTarget] = DFG_Dynamics_AR1_GetInputOutput(z)
% Inputs:
%   z:       matrix of size <N> x <T> of frozen latent variables
% Outputs:
%   zIn:     sequence of inputs (each column is an input)
%   zTarget: sequence of outputs (each column is the corresponding output)

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
% Version 1.1, New York, 24 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function [zIn, zTarget] = DFG_Dynamics_AR1_GetInputOutput(z)

% Number of sequences an total number of samples
n_seq = length(z);
n_samples = 0;
for j = 1:n_seq
  n_samples = n_samples + size(z{j}, 2);
end
n_samples = n_samples - n_seq;

% Initialize the input/output matrices
dim_z = size(z{1}, 1);
zIn = zeros(dim_z, n_samples);
zTarget = zeros(dim_z, n_samples);

% Copy sequence by sequence into the inputs and outputs
k = 0;
for j = 1:n_seq
  len_j = size(z{j}, 2) - 1;
  ind = [1:len_j];
  zIn(:, k + ind) = z{j}(:, ind);
  zTarget(:, k + ind) = z{j}(:, 1 + ind);
  k = k + len_j;
end

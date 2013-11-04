% DFG_Params_Default_Debug_Tanh  Initialize default values of parameters
%
% Syntax:
%   params = DFG_Params_Default_Debug_Tanh()

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

function params = DFG_Params_Default_Debug_Tanh()

% Initialize the parameters
params = DFG_Params_Default(3);

% Modify default parameters
params.eta_w = 10;
params.lambda_w = 0.001;
params.n_max_m_steps = 100;
params.eta_z = 0.1;
params.n_max_e_steps = 100;
params.n_steps_save = 0;
params.n_steps_archive = 0;
params.dynamic_transfer = 'tanh';
params.use_conj_grad = 1;
params.verbosity_level = 0;
params.p_level = 0.7;

% Set gene names
for k = 1:3
  params.labels_y{k} = sprintf('G%d', k);
end
params.n_tfs = 3;


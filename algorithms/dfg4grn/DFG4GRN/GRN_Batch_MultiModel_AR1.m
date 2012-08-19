% GRN_Batch_MultiModel_AR1
%
% This is the entry point for learning multiple instance of a GRN. When the
% computation is done, the function generates 4 global variables:
%   MODEL
%   METER_LEARN
%   METER_INFER_TRAIN
%   METER_INFER_TEST
% These are cell arrays indexed by the number <model_num> of the instance.
%
% The handling of parameters is as following: for instance, to call
% the script on the Arabidopsis data, learn models,
% with kinetic coefficient tau=3.5,
% latent variable inference rate eta_z=0.1,
% and L1-regularization rate lambda=0.001, 
% then compute a statistically significant GRN,
% and then save results to file './Arabidopsis_test.mat',
% one needs to call:
%
% GRN_Batch_MultiModel_AR1(12, 'Script_Arabidopsis_LeaveNone', ...
%                          '.', 'Arabidopsis_test', ...
%                          'eta_z', 0.1, 'tau', 3.5, 'lambda', 0.001);
%
% If an alternative dataset is to be substituted to the one in the script,
% use argument pair ('gene_dataset_filename', FILENAME),
% as in ('gene_dataset_filename', 'DREAM3_InSilicoSize10_1.mat').
%
% Before starting this batch process, add the directory to the path
% using the ADDPATH function.
%
% Syntax:
%   GRN_Batch_MultiModel_AR1(n_models, ...
%                            script_name, dir_res, file_res, varargin)
% Inputs:
%   n_models:     number of models
%   script_name:  filename of the Matlab script that loads the data
%                 and sets the parameters
%   dir_res:      path (directory) of <file_res>
%   file_res:     filename of the file containing the results
%   varargin:     additional arguments, going by pairs
% Outputs:
%   params:       parameter struct

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
% Version 1.0, New York, 25 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: normalize data if required
% Revision 2: add <p_level> to <params>
% Revision 3: move data normalization to GRN_EvaluateModels_AR1
% Revision 4: handle file names
% Revision 5: Small change to add .mat to a filename if an ext is not
% provided. JL

function GRN_Batch_MultiModel_AR1(n_models, script_name, ...
  dir_res, file_res, varargin)

if ~isequal(lower(file_res((end-3):end)), '.mat')
    file_res = [file_res '.mat'];
end
if (isempty(dir_res) && ~isempty(file_res))
  [dir_res, file_res, file_ext] = fileparts(file_res);
  if (isempty(dir_res))
      dir_res = [file_res datestr(clock, 'yyyy-mm-dd_HH-MM-ss')];
  end
  file_res = [file_res file_ext];
elseif (~isempty(dir_res) && isempty(file_res))
  file_res = [datestr(clock, 'yyyy-mm-dd_HH-MM-ss') '.mat'];
end

% Create <n_models> using same script and data, and do not save results yet
for model_num = 1:n_models
  params = GRN_Batch_AR1(model_num, script_name, [], varargin);
end

% Compute statistics on the average model
GRN_EvaluateModels_AR1(params, script_name, dir_res, file_res, varargin);

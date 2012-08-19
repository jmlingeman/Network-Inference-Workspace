% DFG_MeterUpdate_Comparison2TargetGRN  Update the meter: similarity of GRN
%
% Compares the current dynamical model to a binary matrix of target GRN
% links, using 4 measures from information retrieval:
% Jaccard distance, F-measure, area under ROC and Precision-Recall curves.
%
% Syntax:
%   meter = ...
%     DFG_MeterUpdate_Comparison2TargetGRN(model_num, params, meter)
% Inputs:
%   model_num:  model number (default 1)
%   params:     parameters struct
%   meter:      meter structure (created if necessary)
% Outputs:
%   meter:      updated meter structure

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

function meter = ...
  DFG_MeterUpdate_Comparison2TargetGRN(model_num, params, meter)

global MODEL

% Compare to target GRN, if possible
if (~isempty(params.targetGRN) && isequal(params.dynamic_model, 'AR1'))
  % Compute Jaccard similarity and F1 measure at optimal ROC point,
  % and the AUC of the ROC
  [jaccard, F1, accuracy, auroc, aupr] = ...
    GRN_CompareNets(abs(MODEL{model_num}.dynamic.w), params.targetGRN);
  meter = DFG_MeterUpdate(meter, 'target_auroc', auroc, model_num);
  meter = DFG_MeterUpdate(meter, 'target_aupr', aupr, model_num);
  meter = DFG_MeterUpdate(meter, 'target_auccuracy', accuracy, model_num);
  meter = DFG_MeterUpdate(meter, 'target_F1', F1, model_num);
  meter = ...
    DFG_MeterUpdate(meter, 'target_Jaccard', jaccard, model_num);
  if (params.verbosity_level > 0)
    fprintf(1, 'AUROC=%.4f AUPR=%.4f Jaccard=%.4f F1=%.4f acc.=%.4f\n', ...
      auroc, aupr, jaccard, F1, accuracy);
  end
end
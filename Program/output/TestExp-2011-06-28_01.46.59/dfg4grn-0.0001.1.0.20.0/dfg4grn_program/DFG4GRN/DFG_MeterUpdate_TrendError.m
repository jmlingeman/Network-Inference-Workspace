% DFG_MeterUpdate_TrendError  Update the meter: trend errors
%
% Compares the trajectory (trend) taken by the predictions, computing the 
% fraction of correct sign predictions and the R2 Pearson correlation,
% and updates the meter.
%
% Syntax:
%   meter = ...
%     DFG_MeterUpdate_TrendError(z, zOut, params, meter, model_num)
% Inputs:
%   z:         matrix of current values of latent variables, size <M> x <T>
%              or cell array of such matrices
%   zOut:      matrix of output values, size <N> x <T-1>,
%              or cell array of such matrices
%   params:    parameters struct
%   meter:     meter structure (created if necessary)
%   model_num: model number (default 1)
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
  DFG_MeterUpdate_TrendError(z, zOut, params, meter, model_num)

indVars = params.indVars;
dYmean = params.dYmean;

% Statistics on the trend (over all sequences), selected variables (genes)
[eTrendSign, eTrendR2,eSignMat,indVars,x,xStar,dX,dXstar] = DFG_TrendError(z, zOut, dYmean, indVars);
e_trend_sign = mean(eTrendSign);
e_trend_r2 = mean(eTrendR2);
if (params.verbosity_level > 0)
  fprintf(1, 'sign=%.2f, r2=%.3f ', e_trend_sign, e_trend_r2);
end
meter = DFG_MeterUpdate(meter, 'error_trend_sign', e_trend_sign, model_num);
meter = DFG_MeterUpdate(meter, 'error_trend_r2', e_trend_r2, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_sign_mat', eSignMat, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_indvars', indVars, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'x', x, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'xStar', xStar, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'dX', dX, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'dXstar', dXstar, model_num);

% Statistics on the trend (over all sequences), all variables (genes)
[eTrendSign, eTrendR2,eSignMat,indVars,x,xStar,dX,dXstar] = DFG_TrendError(z, zOut, dYmean, []);
e_trend_sign = mean(eTrendSign);
e_trend_r2 = mean(eTrendR2);
if (params.verbosity_level > 0)
  fprintf(1, '(all: sign=%.2f, r2=%.3f)\n', e_trend_sign, e_trend_r2);
end
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_sign_all', e_trend_sign, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_r2_all', e_trend_r2, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_sign_mat_all', eSignMat, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'error_trend_indvars_all', indVars, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'x_all', x, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'xStar_all', xStar, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'dX_all', dX, model_num);
meter = ...
  DFG_MeterUpdate(meter, 'dXstar_all', dXstar, model_num);

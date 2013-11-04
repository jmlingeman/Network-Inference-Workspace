% DFG_MeterUpdate  Update the meter
%
% Adds a measurement <val> to <meter>. It updates field last_<field_name>
% (equivalent to meter.last_field_name = val), and it extends historical
% values by one element, e.g.: meter.last_field_name(n+1) = val).
%
% Syntax:
%   meter = DFG_MeterUpdate(meter, field_name, val, model_num)
% Inputs:
%   meter:      meter structure (created if necessary)
%   field_name: field name within <meter> (created if necessary)
%   val:        value added to the <field_name> vector in <meter> 
%   model_num:  model number (default 1)
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

function meter = DFG_MeterUpdate(meter, field_name, val, model_num)

% Create the <meter> structure if necessary
if isempty(meter)
  meter = {};
end
if ((length(meter) < model_num) || isempty(meter{model_num}))
  meter{model_num} = struct(field_name, []);
end

% Create field if necessary
if ~isfield(meter{model_num}, field_name)
  eval(['meter{model_num}.' field_name ' = [];']);
end

if ((strcmp(field_name, 'error_trend_sign_mat')) || ...
        (strcmp(field_name, 'error_trend_sign_mat_all')) || ...
        (strcmp(field_name,'error_trend_indvars')) || ...
        (strcmp(field_name , 'error_trend_indvars_all')) || ...
        (strcmp(field_name , 'x')) || ...
        (strcmp(field_name , 'x_all')) || ...
        (strcmp(field_name , 'xStar')) || ... 
        (strcmp(field_name , 'xStar_all')) || ...
        (strcmp(field_name , 'dXstar_all')) || ...
        (strcmp(field_name , 'dX_all')) || ...
        (strcmp(field_name , 'dXstar')) || ...
        (strcmp(field_name , 'dX')))
    eval(['meter{model_num}.' field_name ' = val;'])
else
% Update the history
n = eval(['length(meter{model_num}.' field_name ');']);
eval(['meter{model_num}.' field_name '(n+1) = val;']);

% Update the latest value
eval(['meter{model_num}.last_' field_name ' = val;']);
end

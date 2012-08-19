% DFG_Params_Modify  Modify parameters
%
% Syntax:
%   params = DFG_Params_Modify(params, varargin)
% Inputs:
%   params:   parameters struct
%   varargin: additional inputs, going by pair, e.g. ('eta_z', 1e-3)
% Outputs:
%   params:   parameters struct

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
% Version 1.0, New York, 18 July 2009
% (c) 2009, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: handle empty parameter names

function params = DFG_Params_Modify(params, varargin)

if (nargin == 1)
  return;
end
while (~isempty(varargin) && iscell(varargin{1}))
  varargin = varargin{1};
end
if isempty(varargin)
  return;
end

% Loop over the list of arguments and modify the parameters
for k = 2:2:length(varargin)
  var_name = varargin{k-1};
  var_val = varargin{k};
  if ~isempty(var_name)
    str = ['params.' var_name ' = '];
    if isnumeric(var_val)
      if (length(var_val) > 1)
        str = [str '[' num2str(var_val) ']'];
      else
        str = [str num2str(var_val) ';'];
      end
    else
      str = [str '''' var_val ''';'];
    end
    fprintf(1, 'Modifying parameter: %s\n', str);
    eval(str);
  end
end

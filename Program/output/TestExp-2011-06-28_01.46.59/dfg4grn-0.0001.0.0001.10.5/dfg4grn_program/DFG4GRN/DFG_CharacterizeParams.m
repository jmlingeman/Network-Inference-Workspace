% DFG_CharacterizeParams  Compute statistics on the parameters
%
% Syntax:
%   stats = DFG_CharacterizeParams(w, name, trace)
% Inputs:
%   w:     matrix with parameter weights
%   name:  character string (for debug)
%   trace: boolean value (print the debug string or not).
% Outputs:
%   stats: struct containing the following fields:
%          stats.min
%          stats.max
%          stats.normL1
%          stats.normL2
%          stats.sparsity

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

function stats = DFG_CharacterizeParams(w, name, trace)

stats = struct('min', min(min(w)), 'max', max(max(w)), ...
  'normL1', mean(mean(abs(w))), 'normL2', mean(mean(w.^2)), ...
  'sparsity', DFG_ComputeSparsity(w));
if (trace)
  fprintf(1, '%s min=%.3f L1=%.3f L2=%.3f max=%.3f, sparsity=%.3f', ...
    name, stats.min, stats.normL1, stats.normL2, stats.max, stats.sparsity);
end

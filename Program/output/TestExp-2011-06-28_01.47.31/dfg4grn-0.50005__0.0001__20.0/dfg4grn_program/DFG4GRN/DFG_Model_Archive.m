% DFG_Model_Archive  Archive the parameters of the model
%
% Archive the parameters of <n>-th <MODEL> into a special field, e.g.:
% MODEL{n}.dynamic.archive_w
% MODEL{n}.dynamic.archive_bias
% When the function is called many times, it keeps a history of archives
% (the fields are cell arrays with one cell per archive).
%
% Syntax:
%   DFG_Model_Archive(n)
% Inputs:
%   n:     model number (1 by default)

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

function DFG_Model_Archive(n)

global MODEL
switch (MODEL{n}.dynamic.architecture)
  case 'AR1'
    if isfield(MODEL{n}.dynamic, 'archive_w')
      len_archive = length(MODEL{n}.dynamic.archive_w);
    else
      len_archive = 0;
    end
    MODEL{n}.dynamic.archive_w{len_archive+1} = MODEL{n}.dynamic.w;
    MODEL{n}.dynamic.archive_bias{len_archive+1} = MODEL{n}.dynamic.bias;
end

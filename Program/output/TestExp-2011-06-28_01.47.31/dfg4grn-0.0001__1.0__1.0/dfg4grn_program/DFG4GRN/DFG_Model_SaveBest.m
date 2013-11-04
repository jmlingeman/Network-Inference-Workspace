% DFG_Model_SaveBest  Save the parameters of the best model
%
% Save the best value of weight parameters. For instance, at <epoch>,
% for the <n>-th <MODEL>, those will be stored as:
% MODEL{n}.dynamic.best_w
% MODEL{n}.dynamic.best_bias
% MODEL{n}.dynamic.best_epoch
% The function overwrites the current value of weights parameters.
%
% Syntax:
%   DFG_Model_SaveBest(epoch, n)
% Inputs:
%   epoch: current epoch
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

function DFG_Model_SaveBest(epoch, n)

global MODEL
switch (MODEL{n}.dynamic.architecture)
  case 'AR1'
    MODEL{n}.dynamic.best_w = MODEL{n}.dynamic.w;
    MODEL{n}.dynamic.best_bias = MODEL{n}.dynamic.bias;
    MODEL{n}.dynamic.best_epoch = epoch;
end
fprintf(1, 'Best model at epoch %d\n', epoch);

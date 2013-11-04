% DFG_Energy_Gaussian  Energy of the Gaussian regressor
%
% Given a matrix <x> of size <N> x <T>, i.e. <T> samples of dimension <N>,
% and a matrix <xStar> of same size <N> x <T> that approximates <x>,
% compute a vector of Gaussian energies of size 1 x <T> following energy:
% 1/2 * sum _n (x_{n, t} - xStar_{n, t})^2
% If some elements x_{n,t} are unknown, they can be specified in <xKnown>
% as xKnown(n, t) = 0.
%
% Syntax:
%   e = DFG_Energy_Gaussian(x, x_star, xKnown)
% Inputs:
%   x:      matrix of target variables of size <N> x <T>
%   xStar:  matrix of generated variables of size <N> x <T>
%   xKnown: matrix of indicator variables (1=known) of size <N> x <T>
% Output:
%   e:      energy of the Gaussian regressor, vector of size 1 x <T>

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

function e = DFG_Energy_Gaussian(x, xStar, xKnown)

if (nargin == 2)
  e = 0.5 * sum((x - xStar).^2, 1);
else
  e = 0.5 * sum(((x - xStar).^2) .* xKnown, 1);
end

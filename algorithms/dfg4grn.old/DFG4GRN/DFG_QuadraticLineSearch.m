% DFG_QuadraticLineSearch  Quadratic line search for best learning rate
%
% Syntax:
%   eta0 = DFG_QuadraticLineSearch(etas, energies)
% Inputs:
%   etas:     vector of learning rates of size <S> x 1
%   energies: vector of energies of size <S> x 1
% Output:
%   eta0:     learning rate that minimizes the energy, if the energy is
%             quadratic in the learning rate

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
% Version 1.0, New York, 12 March 2010
% (c) 2009, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

function eta0 = DFG_QuadraticLineSearch(etas, energies)

[p, s, mu] = polyfit(etas, energies, 2);
eta0 = (-0.5 * p(2) / p(1)) * mu(2) + mu(1);

% GRN_Plot  Plot a gene regulation network
%
% Syntax:
%   GRN_Plot(mat, titel, ylab, xlab)
% Inputs:
%   mat:   gene regulation influence matrix, of size <n_genes> x <n_tfs>
%   titel: title for the figure
%   ylab:  labels on Y axis (gene names), cell array of length <n_genes>
%   xlab:  labels on X axis (TFs names), cell array of length <n_tfs>

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

% Revision 1: change the colormaps

function GRN_Plot(mat, titel, ylab, xlab)

figure;

% Colormap
val_max = max(abs(mat(:)));
val_min = min(mat(:));
if (val_min < 0)
  mat = (mat / val_max) * 32 + 32;
  map = ...
    [(0:(1/31):1) ones(1, 32); ...
    (0:(1/31):1) (1:(-1/31):0); ...
    ones(1, 32) 1:(-1/31):0];
else
  mat = (mat / val_max) * 64;
  map = colormap('gray');
end
image(mat);
colormap(map');

title(titel, 'Interpreter', 'none');
ny = size(mat,1);
nx = size(mat,2);
set(gca, 'XTick', [1:nx], 'YTick', [1:ny], 'FontSize', 8);
set(gca, 'YTickLabel', ylab, 'XTickLabel', {});
grid on;
if isempty(xlab)
  xlab = ylab;
end
for k = 1:nx
  text(k, ny+0.51, xlab{k}, 'Rotation', 90, ...
    'HorizontalAlignment', 'right', 'FontSize', 8);
end
% set(gcf, 'Position', [50 50 1000 1000]);

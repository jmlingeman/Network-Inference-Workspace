% GRN_CompareNets  Compare two networks using Jaccard and F1 measures
%
% Network <targ> needs to be binary. Otherwise, it is trivially binarized
% (every link different from 0 is considered as 1).
% Network <pred> can be binary, in which case the accuracy,
% Jaccard similarity and F1 measure are computed,
% or real-valued, in which case the AUC of the ROC is calculated, as well
% as the F1 measure and Jaccard similarity at the point that maximizes F1.
%
% Syntax:
%   [jaccard, F1, accuracy, auroc, aupr] = GRN_CompareNets(pred, targ)
% Inputs:
%   pred: matrix of size <N> x <M> representing connections in net 1
%   targ: matrix of size <N> x <M> representing binary connections in net 2
% Outputs:
%   jaccard:  Jaccard distance between <pred> and <targ>
%             |intersection(pred, targ)| / |union(pred, targ)|
%   F1:       precision-recall measure of similiarity of <pred> 
%             w.r.t. <targ>, equal to 2 * prec * recall / (prec + recall)
%   accuracy: accuracy (fraction of true positives and true negatives)
%   auroc:   area under the curve for the ROC (true positive rate vs.
%            false positive rate)
%   aupr:    area under the curve for the precision-recall curve

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

% Revision 1: Added comparisons with non-binary predictions using AUC

function [jaccard, F1, accuracy, auroc, aupr] = GRN_CompareNets(pred, targ)

% Are <pred> and <targ> binary?
is_binary_pred = isequal(unique(pred), [0; 1]);
is_binary_targ = isequal(unique(targ), [0; 1]);

% Binarize the target
if ~is_binary_targ
  targ = (targ > 0);
end

if (is_binary_pred)
  [jaccard, F1, accuracy] = GRN_CompareNets_Binary(pred, targ);
  auroc = nan;
  aupr = nan;
else
  [jaccard, F1, accuracy, auroc, aupr] = GRN_CompareNets_IR(pred, targ);
end


% -------------------------------------------------------------------------
function [jaccard, F1, accuracy] = GRN_CompareNets_Binary(pred, targ)

% Compute the Jaccard coefficient
n_intersect = sum(sum(pred & targ));
n_union = sum(sum(pred | targ));
jaccard = n_intersect / n_union;

% Compute the information retrieval similarity (F1 measure)
n_tp = sum(sum((pred == targ) & pred));
n_fp = sum(sum((pred ~= targ) & pred));
n_fn = sum(sum((pred ~= targ) & ~pred));
precision = n_tp / (n_tp + n_fp);
recall = n_tp / (n_tp + n_fn);
F1 = 2 * precision * recall / (precision + recall);

% Compute the accuracy
accuracy = (n_tp + n_tn) / (n_tp + n_fp + n_tn + n_fn);


% -------------------------------------------------------------------------
function [best_jaccard, best_F1, best_accuracy, auroc, aupr] = ...
  GRN_CompareNets_IR(pred, targ)

% Start from highest predicted value and go to lowest predicted value
[pred, ind] = sort(pred(:), 'descend');
targ = targ(ind);
n_retrievals = length(ind);
n_targ = sum(targ);

% Compute the information retrieval similarity (F1 measure)
tp = cumsum(targ);
tpr = tp / n_targ;
fp = cumsum(1 - targ);
fpr = fp / (n_retrievals - n_targ);

% Measure the Area Under the Curve of the ROC
[fpr, indAUC] = sort(fpr);
tpr = tpr(indAUC);
auroc = sum((tpr(1:(end-1)) + tpr(2:end)) ./ 2 .* diff(fpr));
% [X,Y,THRE,AUC2,optroc] = PERFCURVE(double(targ),pred,1);

% Measure the Area Under the Curve of the Precision-Recall
fn = n_targ - tp;
tn = (n_retrievals - n_targ) - fp;
precision = tp ./ (tp + fp);
recall = tp ./ (tp + fn);
[recall, indPR] = sort(recall);
precision = precision(indPR);
aupr = sum((precision(1:(end-1)) + precision(2:end)) ./ 2 .* diff(recall));

% Compute the best F1 score
F1 = 2 * precision .* recall ./ (precision + recall);
[best_F1, ind_max] = max(F1);

% Compute the Jaccard similarity at the point that maximizes F1
tp_F1 = tp(ind_max);
tn_F1 = tn(ind_max);
best_jaccard = tp_F1 / (n_retrievals - tn_F1);

% Compute the accuracy
best_accuracy = (tp_F1 + tn_F1) / n_retrievals;

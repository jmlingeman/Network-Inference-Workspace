function [prec, sens, spec, pval] = matrixCompare(A, At, ntf)
%[prec, sens, spec, pval] = matrixCompare(A, Atruth, ntf)
%
%If either matrix is undirected, then both matrices will be converted 
%to undirected format by M = abs(M) + abs(M') method
%value magnitudes will be discarded and not used in the comparison

At = abs(At);
if isempty(find(A - A')) || isempty(find(At - At'))
	A = abs(A) + abs(A');
	A(find(A)) = 1;
	At = At + At';
	At(find(At)) = 1;
end

tp = length(intersect(find(A), find(At)));
fp = length(setdiff(find(A), find(At)));
fn = length(setdiff(find(At), find(A)));
tn = length(A)*length(A) - tp - fp - fn;

prec = tp/(tp + fp);
sens = tp/(tp + fn);
spec = tn/(fp + tn);
%hack: adjust for known TFs
if 1 ~= exist('ntf')
	ntf = length(At);
end
spaceSize = length(At) * ntf;
pval = 1 - hygecdf(max(tp - 1, 0), spaceSize, length(find(At)),  length(find(A)));

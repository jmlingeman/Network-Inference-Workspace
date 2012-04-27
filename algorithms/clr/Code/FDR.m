function [pID,pN] = FDR(p,q)
% [pID, pN] = FDR(p,q)
% 
% p   - vector of p-values
% q   - False Discovery Rate level
%
% pID - p-value threshold based on independence or positive dependence
% pN  - Nonparametric p-value threshold
%
% One may use a 2-D matrix as an input
% 
% One may also use a vector or a matrix which contains z-scores instead of
% p-values, with the following caveat:
%   If all significant z-scores are degenerate (normcdf(z) == 1 in matlab),
%   this mode will return Inf as the threshold
%	It is sufficient to have a single significant z-score in the reasonable range (<= 8
%	or so) to produce a valid threshold.
% 
% Also note that the z-score mode assumes that negative z-scores are not
% significant (right tailed significance)
%______________________________________________________________________________
% Based on FDR.m 1.3  by Tom Nichols
% Obtained online and modified by Boris Hayete
% 08/06/2007

%is 'p' a square matrix?
[R, C] = size(p);
if R ~= 1 && C ~= 1
	p = reshape(p, 1, R*C);
end

%does p contain z-scores or p-values?
zscoreMode = 0;
if (max(abs(p)) > 1)
	fprintf('Matrix p autodetected to contain z-scores!\n');
	fprintf('Converting one-tailed (negative z-scores are NOT significant!\n');
	zscoreMode = 1;
	p = 1 - normcdf(p);
end
p = sort(p(:));
V = length(p);
I = (1:V)';

cVID = 1;
cVN = sum(1./(1:V));

pID = p(max(find(p<=I/V*q/cVID)));
pN = p(max(find(p<=I/V*q/cVN)));
if isempty(pID)
	pID = 0;
end
if isempty(pN)
	pN = 0;
end
if zscoreMode == 1
	pID = norminv(1 - pID);
	pN = norminv(1 - pN);
end
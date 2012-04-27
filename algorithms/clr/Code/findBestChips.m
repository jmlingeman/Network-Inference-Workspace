function [bestChipIdx, prec, sens, setSize] = findBestChips(rows, numChips, REPEAT, netMethod)
%[bestChipIdx, prec, sens, setSize] = findBestChips(rows, numChips[, REPEAT=3, netMethod='rayleigh')
%
%similar to netSubsample, but finds the best set of N chips after REPEAT
%samples

if 1 ~= exist('REPEAT')
	REPEAT = 3;
end

if 1 ~= exist('netMethod')
	netMethod = 'rayleigh';
end

%--------------------------------------
%find condition clusters
%--------------------------------------
load reg_b3

%find coefficient of variation and pick top 1000 genes
cv = std(rows')./mean(rows');
[cvsorted, cvidx] = sort(cv, 'descend');
Y = 1 - normcdf(double(clr(rows(cvidx(1:1000), :)', netMethod)));
Y = Y - diag(diag(Y));
Y = double(Y);
Y = squareform(Y, 'tovector');

%Y = pdist(r', 'mahal');
Z = linkage(Y, 'complete');
%[H, T, PERM] = dendrogram(Z, length(v3i.conditions), 'orientation', 'left', 'labels', v3i.conditions);
prec = [];
sens = [];
setSize = {};
bestChipIdx = [];
for branches = numChips
    branches
	[H, T, PERM] = dendrogram(Z, branches, 'orientation', 'left');
	for r = 1:REPEAT
		%pick a random set, one node from each cluster
		exptIdx = [];
		for i = 1:max(T)
			idx = find(T == i);
			exptIdx = [exptIdx; idx(floor(rand*length(idx) + 1))];
		end
		bestChipIdx = [bestChipIdx exptIdx];
		z = clr(rows(:, exptIdx), 'rayleigh');
		if length(z) == 4217
			z = z(reg_b3.cds_idx, reg_b3.cds_idx);
		end
		z(:, reg_b3.zidxTest) = 0;
		[p, s] = matrixPvalue(z, reg_b3.Atest, length(reg_b3.tfidxTest), 95, .01, 100, 10);
		prec = [prec; p(1, :)];
		sens = [sens; s(1, :)];
		setSize{length(setSize) + 1} = num2str(branches);
	end
end

%maybe try scaling to 10 to get 10% of the range?
prec = round(prec * 100);
sens = sens * 100;
[r, c] = size(prec);
all = [];
allStd = [];
allBranch = [];
bestIdx = 0;
for i = 1:r/REPEAT
	s = [];
	for j = 1:REPEAT
		idx = find(prec((i - 1)*REPEAT + j, :) == 30);
		
		if isempty(idx)
			s = 0;
		else
			idx = idx(end); %take the right-most point on prec-sens chart
			%mean of prec at this sens
			s = [s, mean(sens((i - 1)*REPEAT + j, idx))];
			if s(end) == max(s)
				bestIdx = j;
			end
		end
	end
	all = [all, median(s)];
	allStd = [allStd, std(s)];
	allBranch = [allBranch, str2double(setSize{(i - 1)*REPEAT + 1})];
	bestChipIdx = bestChipIdx(:, bestIdx);
end
clf
errorbar(allBranch, all, allStd, 'r-o');
title('Number of distant chips vs sensitivity at 30% precision');
xlabel('Number of chips');
ylabel('Sensitivity at 30% precision');
legend('Performance for nformationally most-distant chips');
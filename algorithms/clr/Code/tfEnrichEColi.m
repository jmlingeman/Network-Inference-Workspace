function [tfs, foundGenes, pvalues] = tfEnrichEColi(genes, zThreshold, pvalCutoff)
%[tfs, foundGenes, pvalues] = tfEnrichEColi(genes, [zThreshold, pvalCutoff]);
%
%genes:
%  A cell array of gene names.  A gene name is one of canonical name, b-number, or probe set name (M3D build 3
%  reference set)
%
%zThreshold:
%  z-score threshold to prune the map at - defaults to findThreshold(z, 60,
%  reg_b3.Atest (the usual 60% precision threshold)
%  A negative threshold TURNS OFF the use of the computational map.
%
%pvalCutoff:
%  The p-value cutoff for reporting hits (not corrected for multiple testing).

load reg_b3
load tfs_b3
load E_coli_v3_Build_3.mat
load z

if 1 ~= exist('zThreshold')
	zThreshold = findThreshold(z, 60, reg_b3.Atest);
end

if 1 ~= exist('pvalCutoff')
	pvalCutoff = 0.05;
end

z = double(z);

zidx = setdiff(1:length(z), tfs_b3.cds_idx);
z(:, zidx) = 0;
zr = zeros(size(z));
zr(reg_b3.cds_idx, reg_b3.cds_idx) = reg_b3.A;
if zThreshold <= 0
	z(:) = 0; %clear the map to turn off its use
else
	z(z < zThreshold) = 0;
end
zj = or(z, zr);

cdsIdx = strmatch('CDS', data.probe_set_type);
gidx = zeros(size(genes));
for i = 1:length(genes)
	%is this a probe set name?
	curIdx = strmatch(genes{i}, data.probe_set_name(cdsIdx), 'exact');
	if isempty(curIdx)
		%is this gene a b-number?
		curIdx = strmatch(genes{i}, data.locus(cdsIdx), 'exact');
	end
	if isempty(curIdx)
		%is this a canonical gene name?
		curIdx = strmatch(genes{i}, data.gene_symbol(cdsIdx), 'exact');
	end
	if ~isempty(curIdx)
		gidx(i) = curIdx;
		continue
	end
end

keepidx = find(gidx);
if length(keepidx) < length(gidx)
	fprintf('Failed to look up %d genes (by b-number, canonical name, or probe set name)\n', length(gidx) - length(keepidx));
end
l = length(genes);
genes = genes(keepidx);
gidx = gidx(keepidx);
fprintf('Eliminated %d out of %d genes due to inability to match them to synonym list\n', l - length(gidx), l);

tfs = tfs_b3.cds_idx;
foundTFs = [];
impact = [];
while 1
	newCoverage = zeros(length(tfs), 1);
	for tfidx = 1:length(tfs)
		newCoverage(tfidx) = length(intersect(find(zj(:, tfs(tfidx))), gidx));
	end
	[imp, tfidx] = max(newCoverage);
	if imp == 0
		break
	end

	foundTFs = [foundTFs; tfs(tfidx)];
	tfs = setdiff(tfs, tfs(tfidx));
	impact = [impact; imp];
end
cds_idx = strmatch('CDS', data.probe_set_type);
tfs = data.friendly_name(cds_idx(foundTFs));
pvalues = [];
foundGenes = {};
%compute pvalues for these tfs
for tfidx = foundTFs'
	known = find(zj(:, tfidx));
	found = intersect(known, gidx);
	if isempty(found)
		pvalues = [pvalues 1];
		continue
	end
	pvalues = [pvalues, 1 - hygecdf(length(found) - 1, length(cds_idx), length(known), length(gidx) - 1)];
	foundGenes(length(foundGenes) + 1) = {data.friendly_name(cds_idx(found))'};
end

%find 2-TF overlaps
for tf1idx = 1:length(foundTFs)-1
	known1 = find(zj(:, foundTFs(tf1idx)));
	found1 = intersect(known1, gidx);
	for tf2idx = tf1idx+1:length(foundTFs)
		known2 = find(zj(:, foundTFs(tf2idx)));
		found2 = intersect(known2, gidx);
		jointKnown = intersect(known1, known2);
		jointFound = intersect(found1, found2);
		if length(jointFound)
			pvalues = [pvalues, 1 - hygecdf(length(jointFound) - 1, length(cds_idx), length(jointKnown), length(gidx) - 1)];
			foundGenes(length(foundGenes) + 1) = {data.friendly_name(cds_idx(jointFound))'};
			tfs{length(tfs) + 1} = [tfs{tf1idx}, '_AND_', tfs{tf2idx}];
		end
	end
end


%fprintf('The dataset is described by %d tfs which cover %d of %d genes (the rest did not have a match)\n', length(tfs), length(genes));
%fprintf('Their impact:\n');
%for i = 1:length(foundTFs)
%	fprintf('%s => %d\n', tfs{i}, impact(i));
%end
keepidx = find(pvalues <= pvalCutoff);
%Now sort by p-value
[pvalues, pidx] = sort(pvalues(keepidx), 'ascend');
tfs = tfs(keepidx(pidx));
pvalues = pvalues';
foundGenes = foundGenes(keepidx(pidx));

%plot the submap
%tfidx = foundTFs(keepidx);
%curgidx = find(sum(zj(:, tfidx)'));
%curgidx = intersect(gidx, curgidx);
%curgidx = union(tfidx, curgidx);

%subA = zj(curgidx, curgidx);
%tic;
%gDisplay(subA, all.genes(curgidx), 'mi', 'neato', 1, 'Enriched network', 'enrichedNetwork.ps2');
%toc;


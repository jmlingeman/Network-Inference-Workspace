function genes = mapGenes(genes, dataset, A, threshold, distance, addSign, comment, fname)
%mapGenes(genes, dataset, A, threshold, distance, addSign, comment, fname)
%
%maps genes into A and calls gDisplay to write the file 'fname'
%with
%
%dataset: a matlab dataset structure with the following expected fields:
%   genes: a cell array of genes
%   rma: a genes x experiments normalized array of expression data
%      If your data is not rma-normalized, create a field named rma
%      anyway and copy your data into it
%A: clr-generated connectivity matrix
%threshold: a threshold used for pruning; corresponds to the desired
%   precision/sensitivity tradeoff
%distance: indirection distance
%addSign: guess type of correlation based on correlation
%comment: 'comment', if any
%   giving 'genes' by, e.g., 'ent' will pull out all ent genes (like 'ent*')
%   'genes' may be given as {'*'} (all genes with connectivity)
%fname: filename to save to
%
%No more than 500 genes may be mapped.  Comment the code to turn
%off if you know what you are doing.

if 1 ~= exist('distance')
	distance = 1;
end

if 1 ~= exist('comment')
	comment = 'Ad-hoc list';
end

if 1 ~= exist('fname')
	fname = 'gviz.tmp.ps2';
end

if isa(A, 'single')
	A = double(A);
end
if threshold <= 1 && threshold >= 0
	%invert p-value matrix
	m = max(max(A));
	A = m - A;
	threshold = m - threshold;
end

masterList = dataset.genes;
if strcmp('*', genes)
	if length(find(A)) > 2000
		fprintf('more than 2000 edges, cannot use the wildcard!\n');
		return
	end
		
	B = A;
	B(B ~= 0) = 1;
	s = sum(B) + sum(B');
	if length(find(s)) > 500
		fprintf('the network is sparse, but it has more than 500 genes!\n');
		return
	end
	clear B
	genes = masterList(find(s));
	fprintf('Wildcard accepted, charting %d genes\n', length(genes));
end

if issparse(A)
	if threshold > 0
		idx = find(A);
		idx2 = find(A >= threshold);
		val = A(idx2);
		A(idx) = 0;
		A(idx2) = val;
	end
else
	if threshold > 0
		A(A < threshold) = 0;
	end
	A = sparse(double(A)); %save RAM
end

allidx = [];
for d = 1:distance
	%find the genes
	for i = 1:length(genes)
		idx = strmatch(genes{i}, masterList);
		if isempty(idx)
			fprintf('no match for %s in the master list\n', genes{i});
			pause
			continue
		end
		allidx = [allidx; idx];
	end
		
	sReg = []; sRegBy = [];
	%find what they regulate
	if length(allidx) == 1
		sReg = A(:, allidx);
	else
		sReg = sum(A(:, allidx), 2);
	end
	%find their regulators
	if length(allidx) == 1
		sRegBy = A(allidx, :);
	else
		sRegBy = sum(A(allidx, :));
	end
	allidx = [allidx; find(sReg); find(sRegBy)'];
	allidx = unique(allidx);
	genes = masterList(allidx);
end

if length(genes) > 500
	fprintf('Too many genes: %d; limit of 500 exceeded!\n', length(genes));
	return
elseif length(genes) > 120
	fprintf('Number of genes is %d (> 120), using dot (bad layout)\n', length(genes));
	type = 'dot';
else
	type = 'neato';
end

A = A(allidx, allidx);

%normalize to max == 1
A(find(A)) = A(find(A)) ./ max(max(abs(A)));

if addSign
	rma = double(dataset.rma);
	%add correlation-driven signs to arrows
	R = corrcoef(rma(allidx, :)');
	%R(find(abs(R) < 0.5)) = 0;
	A = A .* sign(R);
end

gDisplay(A, masterList(allidx), 'mi', type, 0, comment, fname);

function [z, M] = sigRoulston(X, b, useSpline)
%[SIG, MI] = sigRoulston(X, b, useSpline)
%
%Compute Roulston significance of mutual information
%
%Accepts:
%X - data matrix, genes x experiments
%b - number of bins; this value needs to be a divisor of #experiments; the
%    script will try to adjust this value if it's not
%useSpline - boolean value; if omitted or evaluates to true, uses spline
%    estimators for significance estimation, else classical shannon formula
%    from Mark Roulston's paper
%Returns:
%z - roulston significance score
%M - optionally, mutual information matrix for further work

if 1 ~= exist('useSpline', 'var')
	useSpline = 1;
end

[G, E] = size(X);
%step 0: check that E mod b == 0
if mod(E, b) ~= 0
	fprintf('E must be divisible by b exactly!\n');
	fprintf('Trying to adjust!\n');
	oldb = b;
	for i = 1:b
		if mod(E, b + i) == 0
			fprintf('Re-setting b from %d to %d\n', b, b + i);
			b = b + i;
			break
		end
	end
	if oldb == b
		fprintf('Failed to find a good number of bins!\n');
		return
	end
end

%step 2: compute the classical Shannon entropy over the re-scaled variables
if useSpline == 0
	%step 1: discretize each value into b bins, uniformly
	for i = 1:G
		[foo, idx] = sort(X(i, :), 'ascend');
		for j = 1:b
			X(i, idx(E/b * (j - 1) + 1 : E/b * j)) = j;
		end
	end

	H = zeros(G, G);
	for i = 1:G-1
		for j = i:G
			h2d = zeros(b, b);
			for k = 1:E
				h2d(X(i, k), X(j, k)) = h2d(X(i, k), X(j, k)) + 1/E;
			end
			nzidx = find(h2d);
			h2d(nzidx) = h2d(nzidx) .* log(h2d(nzidx));
			H(i, j) = -sum(sum(h2d));
			H(j, i) = H(i, j);
		end
	end
else
	M = MI(X, b, 3, 3);
	Ent = diag(M);
	M = M - diag(diag(M));
	H = repmat(Ent, 1, G) + repmat(Ent, 1, G)' - M;
end
B = b*b;
%step 3: compute Roulston's statistic over the entropies
z = sqrt(B/2)*((2*E*log2(B) - H)/(B - 1) - 1);

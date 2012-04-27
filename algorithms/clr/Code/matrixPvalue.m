function [prec, sens, spec, pval, pval2] = matrixPvalue(varargin)
%[prec, sens, spec, pval] = matrixPvalue(A1, A2, A3, ..., Atruth, ntf, [start, incr, finish, percent cutoff])
%A1..An - matrices to compare
%Atruth - the truth matrix
%ntf - number of transcription factors in the matrices A1...An
%start..incr..finish - threshold region to sample

if length(varargin) < 2
	fprintf('not enough arguments\n');
	return
end

if length(varargin) > 3
	start = varargin{end - 3};
	incr = varargin{end - 2};
	finish = varargin{end - 1};
	precCutoff = varargin{end};
	ntf = varargin{end - 4};
	At = varargin{end - 5};
	N = length(varargin) - 6;
else
	start = 20;
	incr = 1;
	finish = 100;
	precCutoff = 5;
	ntf = varargin{end};
	At = varargin{end - 1};
	N = length(varargin) - 1;
end
At = sparse(At);	


prec = [];
sens = [];
spec = [];
pval = [];
sparsity = [];
lastPercentDone = 0;
for aidx = 1:N
	A = varargin{aidx};
	%normalize A to be between 0 and 100
	%A = A - min(min(A));
	%A = A/max(max(A))*100;
	%A(A < start) = 0;
	vals = A(find(A));
	[goo, validx] = sort(vals, 'ascend');
	realStart = vals(validx(round(start/100*length(vals))));
	A(A < realStart) = 0;
	if length(find(A))/length(A)^2 < .05
		if isa(A, 'single')
			A = double(A);
		end
		
		A = sparse(A);
	end
	for cutoff = start:incr:finish
		percentDone = ((aidx - 1) + (cutoff - start)/(finish - start))/N*100;
		if percentDone - lastPercentDone > 1
			fprintf('%d percent done\n', percentDone);
			lastPercentDone  = percentDone;
		end
		i = round((cutoff - start)/incr + 1);
		idx = find(A);
		vidx = A(idx);
		realCutoff = vals(validx(round(cutoff/100*length(vals))));
		zidx = find(vidx < double(realCutoff));
		A(idx(zidx)) = 0;
		[prec(aidx, i), sens(aidx, i), spec(aidx, i), ...
                 pval(aidx, i)] = matrixCompare(A, At, ntf);
		sparsity(aidx, i) = length(find(A))/length(A)^2;
	end
	zIdx = find(prec(aidx, :)*100 < precCutoff);
	nzIdx = setdiff(1:length(prec), zIdx);
	%cheat: replace all values from that point on with the last passing
	%point; in other words, repeat the last point a lot of times!
	if length(nzIdx) && length(zIdx) && min(nzIdx) < size(prec, 2)
		prec(aidx, zIdx) = prec(aidx, min(nzIdx) + 1);
		sens(aidx, zIdx) = sens(aidx, min(nzIdx) + 1);
		spec(aidx, zIdx) = spec(aidx, min(nzIdx) + 1);
		pval(aidx, zIdx) = pval(aidx, min(nzIdx) + 1);
	end
end

subplot(5, 1, 1);
p = semilogy(start:incr:finish, pval', 's');
xlabel('threshold');
ylabel('pvalue');
subplot(5, 1, 2);
p = plot(1 - spec', sens', '^');
xlabel('1 - specificity');
ylabel('sensitivity');
hold on
plot(0:0.01:1, 0:0.01:1, 'o');
hold off
subplot(5, 1, 3);
p = plot((1 - sparsity)', sens', '^');
xlabel('1 - sparsity')
ylabel('sensitivity')
subplot(5, 1, 4);
p = plot(start:incr:finish', sens', '^');
xlabel('threshold')
ylabel('sensitivity')
subplot(5, 1, 5);
p = plot(sens'*100, prec'*100, 'o');
xlabel('sensitivity');
ylabel('precision');

labels = {};
for i = 1:N
	labels{i} = int2str(i);
end
legend(labels);

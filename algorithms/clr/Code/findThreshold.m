function [t, prec] = findThreshold(A, percent, Atruth)
%[t, prec] = findThreshold(A, percentAccuracy, [truthMatrix])
%
%Finds the threshold, t, at which A 
%has roughly the precision of 'percent' percent vs regulon
%'prec' returns the actual precision (useful when there's no close
%convergence)


load reg_b3
load tfs_b3

if 1 ~= exist('Atruth', 'var')
	Atruth = reg_b3.A;
end

G = length(A);
if G == length(Atruth)
	a = A;
elseif G == 4217
	a = A(reg_b3.cds_idx, reg_b3.cds_idx);
else
	a = A(reg_b3.ps_idx, reg_b3.ps_idx);
end
zidx = setdiff(1:length(reg_b3.genes), reg_b3.tfidxTest);
a(:, zidx) = 0;

%low = min(min(A));
%high = max(max(A));

[values, valIdx] = sort(reshape(a, 1, length(a)^2), 'ascend');
lowIdx = 1;
highIdx = length(valIdx);

%binary search for 'percent' quality
while 1
	acur = a;
	%cur = (high + low)/2;
	
	%curIdx = round((highIdx + lowIdx)/2);
	curIdx = round(lowIdx + (highIdx - lowIdx)/2);
	cur = values(curIdx);
	
	acur(acur < cur) = 0;
	
	prec = matrixCompare(acur, Atruth, length(reg_b3.tfidx)) * 100;
	if abs(prec - percent) < .1
		t = cur;
		return
	elseif prec > percent
		%high = cur;
		highIdx = curIdx;
	else
		%low = cur;
		lowIdx = curIdx;
	end
	if abs(lowIdx - highIdx) <= 1;
		fprintf('Boundaries met without convergence; at last threshold %d, precision was %f\n', cur, prec);
		t = cur;
		return;
	end
end
	
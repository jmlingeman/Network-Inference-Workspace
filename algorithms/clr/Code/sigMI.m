function sMI = sigMI(X, SAMPLE)
%sMI = sigMI(X, SAMPLE [=30])

if 1 ~= exist('SAMPLE')
	SAMPLE = 30;
end

[G, E] = size(X);
MI = mi(X, 10, 3, 3);

allMI = zeros(G, G, SAMPLE);
for s = 1:SAMPLE
	tic;
	s
	for g = 1:G
		X(g, :) = X(g, randperm(E));
	end
	allMI(:, :, s) = splineMI(X);
	%allMI(:, :, s) = mi(X, 'c');
	toc;
end

sMI = (MI - mean(allMI, 3))./std(allMI, 0, 3);
for g = 1:G
	sMI(g, g) = 0;
end

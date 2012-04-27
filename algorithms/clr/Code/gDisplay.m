function gDisplay(A, g, mode, type, showDiag, label, fName)
%gDisplay(A, g, mode, type, showDiag, label, fName)
%
%displays connectivity matrix A (NxN) for genes g (1:N) using 'dot'
%(GraphViz)
%mode: vi or mi

if 1 ~= exist('type')
	type = 'fdp';
end

if 1 == exist('fName')
	format = regexp(fName, '\w{3}$', 'match');
	format = format{1};
else
	format = 'svgz';
end

if 1 ~= exist('showDiag')
	showDiag = 1;
elseif ~showDiag
	for i = 1:length(A)
		A(i, i) = 0;
	end
end

%pre-process gene names to remove non-word characters
for i = 1:length(g)
	c = g{i};
	pos = regexp(c, '\W', 'start');
	c(pos) = '_';
	%if gene starts with a digit, add a 'g' symbol in front of it
	if isempty(regexp(c(1), '[a-zA-Z]', 'match'))
		c = ['g', c];
	end
	g{i} = c;
end
		


if 1 ~= exist('label')
	label = '';
end

if 1 ~= exist('fName')
	fName = strcat('gviz_tmp.', format);
end

%determine whether the network is undirected
directed = 0;
for i = 1:(length(A) - 1)
	for j = (i + 1):length(A)
		if abs(A(i, j) - A(j, i)) > 1e-10
			directed = 1;
			break;
		end
	end
end
fprintf('directed? %d\n', directed);

%if not directed, make lower triangular
if ~directed
	for i = 1:(length(A) - 1)
		for j = (i + 1):length(A)
			A(i, j) = 0;
		end
	end
end

N = length(A);
if directed
	out = {'digraph Network{'};
else
	out = {'graph Network{'};
end

out(2) = {['rank=source; overlap=scale; splines=true; pack=true; dpi=600; label="', label, '";']};
%out(2) = {['rank=source; label="', label, '";']};
%node section

if directed
	s = sum(abs(A));
	
	for i = 1:N
		if s(i)
			out(length(out) + 1) = {['"', char(g(i)), '" [style=filled,width=0.5,fillcolor=grey90,color=black]']};
		else
			out(length(out) + 1) = {['"', char(g(i)), '" [color=black,width=0.5]']};
		end
	end
end
%edge section
for c = 1:N
	regulated = find(A(:, c));
	%"5th Edition" [sides=9, distortion="0.936354", orientation=28, skew="-0.126818", color=salmon2];
	if length(regulated) == 0
		continue;
	end
	for r = 1:length(regulated)
		if mode == 'vi'
			weight = 1/A(regulated(r), c);
		else
			weight = A(regulated(r), c);
		end
		if abs(weight) >= 2 %then ppi
			style = ', style=dotted';
			weight = weight - sign(weight) * 2; %subtract ppi
		else
			style = '';
		end
		if (weight > 0)
			color = ['[color=firebrick3];'];
			%color = ['[color=firebrick3];'];
		elseif ~isreal(weight)
			color = ['[color=MediumOrchid4];'];
		else
			color = ['[color=navyblue];'];
			%color = ['[color=navyblue, weight=', num2str(abs(weight)), style, ', arrowhead=tee];'];
			%color = ['[color=navyblue, arrowhead=tee];'];
			
		end
		if directed
			out(length(out) + 1) = {[char(g(c)), '->', char(g(regulated(r))), ' ', color]};
		else
			out(length(out) + 1) = {[char(g(c)), '--', char(g(regulated(r))), ' ', color]};
		end
	end
end
out(length(out) + 1) = {'}'};

fid = fopen('gviz_tmp.dot', 'w');
for i = 1:length(out)
	fprintf(fid, '%s\n', char(out(i)));
end
fclose(fid);

%use fdp
string = [type, ' -o', fName, ' -T', format, ' gviz_tmp.dot'];
system(string);
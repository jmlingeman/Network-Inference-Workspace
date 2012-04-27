function [c, X, Y] = geneVsGene(genes, data, controlGene)
%[c, X, Y] = geneVsGene(genes, data, controlGene)
%
%genes: a cell array of two genes to plot the relationship for
%data: the usual data matrix
%control gene: an optional control gene
%blue points: control gene below its median
%red points: control gene above its median

allidx = [];
if iscell(genes)
	for i = 1:length(genes)
		idx = strmatch(genes{i}, data.genes);
		if isempty(idx)
			fprintf('%s not found\n', genes{i});
		end
		allidx = [allidx idx];
	end
else
	allidx = genes;
end

if 1 == exist('controlGene')
	idx = strmatch(controlGene, data.genes);
	c = data.rma(idx, :);
	m = median(c);
	off = find(c < m);
	on = find(c >= m);
else
	off = [];
	on = 1:length(data.rma(1, :));
end

%fig = plot(data.rma(allidx(1), off)', data.rma(allidx(2), off)', 'bo',
%data.rma(allidx(1), on)', data.rma(allidx(2), on)', 'ro');
fig = figure;
%hpatch = scatter('v6', data.rma(allidx(1), off)', data.rma(allidx(2), off)', 'bo');
hold on;
idx = [off on];
for i = 1:length(idx)
	if i <= length(off)
		color = 'bo';
	else
		color = 'ro';
	end
	l = plot(data.rma(allidx(1), idx(i))', data.rma(allidx(2), idx(i))', color, 'Tag', [data.chip_name{idx(i)}, ': ', data.conditions{idx(i)}]);
	set(l,'ButtonDownFcn',@myupdatefcn);
end
hold off;

xlabel(data.genes{allidx(1)}, 'Interpreter', 'none');
ylabel(data.genes{allidx(2)}, 'Interpreter', 'none');

X = data.rma(allidx(1), :);
Y = data.rma(allidx(2), :);
c = corr(X', Y');

% Click on line to select data point

function txt = myupdatefcn(empt,event_obj)
tag = get(empt,'Tag');
h = legend(tag);
set(h, 'Interpreter', 'none');
%fprintf('tag: %s\n', tag);
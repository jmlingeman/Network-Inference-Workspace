function [sigTerms, sigTermIds, sigGenes, sigPvalues] = enrich(genes, fdrCutoff, minTermDepth)
%[sigTerms, sigTermIds, sigGenes, sigPvalues] = enrich(genes, [fdrCutoff = 0.05, minTermDepth = 3])
%
%THIS UTILITY REQUIRES MATLAB v7.3 OR LATER!!!
%
%genes: a cell array of genes, b-numbers, or probe set names (M3D build 3
%format)
%
%fdrCutoff: an optional false discovery rate at which to cut off reporting
%p-values; defaults to 0.05
%
%minTermDepth: a cutoff for throwing out terms that are too general;
%defaults to 3.  This adjustment happens after FDR has been calculated.
%
%the logic follows that found at:
%http://www.mathworks.com/products/bioinfo/demos.html?file=/products/demos/shipping/bioinfo/geneontologydemo.html
%
%Ontology automatically updated via the Mathworks GO class
%
%E. coli gene to term annotation needs to be periodically obtained from
%here:
%http://www.ebi.ac.uk/GOA/proteomes.html

if 1 ~= exist('genes')
	fprintf('Enrichment set is empty - specify some genes!\n');
end

if 1 ~= exist('fdrCutoff') || fdrCutoff < 0 || fdrCutoff > 1
	fdrCutoff = 0.05;
end

if 1 ~= exist('minTermDepth')
	minTermDepth = 3;
end

load E_coli_v3_Build_3
cacheFileName = 'GOCache.obo';
annotFileName = '18.E_coli_K12.goa';
annotFilePath = which(annotFileName);
cacheFilePath = which(cacheFileName);
if isempty(cacheFilePath)
	separator = '/';
	idx = strfind(annotFilePath, separator);
	if isempty(idx) %must be windows
		separator = '\';
		idx = strfind(annotFilePath, separator);
	end
	cacheFilePath = [annotFilePath(1:idx(end)), cacheFileName];
end

%how recent is the annotation file?
x = dir(annotFilePath);
if datenum(date) -  datenum(x.date) > 60
	fprintf('Annotation file seems to be very old; check if there''s a new one here:\n');
	fprintf('http://www.ebi.ac.uk/GOA/proteomes.html\n');
end

%how recent is the ontology file?
reload = 0;
if ~exist(cacheFilePath) 
	reload = 1;
else
	x = dir(cacheFilePath);
	if datenum(date) -  datenum(x.date) > 60
		reload = 1;
	end
end
if reload
	fprintf('Cache file absent or old; updating (this may take up to 2-3 minutes)\n');
	GO = geneont('live', true, 'ToFile', cacheFilePath); % this step takes a while
else
	GO = geneont('File', cacheFilePath); % this step takes a while	
end

gidx = strmatch('CDS', data.probe_set_type);

%determine ids of the query genes
queryIds = zeros(1, length(genes));
for i = 1:length(genes)
	%try full probeset
	idx = strmatch(genes{i}, data.probe_set_name(gidx), 'exact');
	if length(idx) == 1
		queryIds(i) = idx;
		continue
	end
	%try gene name
	idx = strmatch(genes{i}, data.gene_symbol(gidx), 'exact');
	if length(idx) == 1
		queryIds(i) = idx;
		continue
	elseif length(idx) > 1
		fprintf('Multiple matches for %s\n', genes{i});
		continue
	end
	%try bnum
	idx = strmatch(genes{i}, data.locus(gidx), 'exact');
	if length(idx) == 1
		queryIds(i) = idx;
		continue
	elseif length(idx) > 1
		fprintf('Multiple matches for %s\n', genes{i});
		continue
	end
end	

get(GO)
annotation = goannotread('18.E_coli_K12.goa');
genes  = {annotation.DB_Object_Symbol}; % yeast gene list
bnums  = {annotation.DB_Object_Name};
go     = [annotation.GOid];             % associated GO terms
aspect = {annotation.Aspect};           % ontologies
uids = cell(size(genes));

%extract bnumbers
for i = 1:length(bnums)
	tokens = regexp(bnums{i}, '([b|B]\d{4})', 'tokens');
	tokens2 = regexp(bnums{i}, '(^[a-z]{3}[A-Z])', 'tokens');
	if ~isempty(tokens)
		uids{i} = tokens{1}{1};
	else
		if ~isempty(tokens2)
			uids{i} = tokens2{1}{1};
		else
			fprintf('Neither name nor b-number identified for %s\n', genes{i});
		end
	end
end
m = GO.Terms(end).id;          % gets the last term id
genesChipCount = zeros(m,1);    % a vector of GO term counts for the entire chip.
genesQueryCount = zeros(m,1); % a vector of GO term counts for interesting genes.
termsToGenes = cell(m, 1);
for i = 1:numel(gidx)	
	bnum = data.locus{gidx(i)};
	idx = strcmpi(uids, bnum);  % lookup gene
	if isempty(find(idx, 1)) %try by gene name
		gene = data.gene_symbol{gidx(i)};
		idx = strcmpi(uids, gene);  % lookup gene
	end
    goid = go(idx);                 % get the respective GO ids
	try %it seems there's a rare error condition in getrelative
		goid = getrelatives(GO, goid);
	catch %ignore it
		continue
	end
	
    % update vector counts
    genesChipCount(goid) = genesChipCount(goid) + 1;
    if (any(i == queryIds))
       genesQueryCount(goid) = genesQueryCount(goid) +1;
	   for j = find(goid')
		   termsToGenes{goid(j)} = [termsToGenes{goid(j)} gidx(i)];
	   end
    end
end

pvalues = hygepdf(genesQueryCount,max(genesChipCount),...
                  max(genesQueryCount),genesChipCount);
pt = FDR(pvalues, fdrCutoff);
[dummy idx] = sort(pvalues);
tIdx = find(dummy <= pt, 1, 'last');
fprintf('Found %d significant entries\n', length(find(dummy <= pt)));
% create a report
report = sprintf('GO Term      p-val  counts  definition\n');
sigTerms = {};
sigTermIds = {};
sigPvalues = [];
sigIdx = [];
sigGenes = {};
for i = 1:tIdx
	if length(getancestors(GO, idx(i))) < minTermDepth
		continue
	end
	sigIdx(end + 1) = idx(i);
    term = idx(i);
	%report = sprintf('%s%s\t%-1.4f\t%-d / %-d\t%s...\n', report, ...
    %                char(num2goid(term)), pvalues(term),...
    %                genesQueryCount(term), genesChipCount(term),...
    %                GO(term).Term.definition(2:60));
	sigTermIds{end + 1} = char(num2goid(term));
	sigTerms{end + 1} = GO(term).Term.definition;
	sigPvalues(end + 1) = dummy(i);
	sigGenes{end + 1} = data.friendly_name(unique(termsToGenes{term}))';
end
%turn into column vectors
sigGenes = sigGenes';
sigTermIds = sigTermIds';
sigTerms = sigTerms';
sigPvalues = sigPvalues';

%display graphical summary
disp(report);
subGO = GO(getancestors(GO,sigIdx));
[cm acc rels] = getmatrix(subGO);
BG = biograph(cm,get(subGO.Terms,'name'));
for i=1:numel(acc)
    pval = pvalues(acc(i));
    color = [(1-pval).^(10),pval.^(1/10),0.3];
    set(BG.Nodes(i),'Color',color);
    set(BG.Nodes(i),'Label',num2str(acc(i))) % add info to datatips
end

view(BG);
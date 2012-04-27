function [genes, gidx] = regSynonym_b3(genes)
load E_coli_v3_Build_3.mat
load reg_b3

[bNums, found] = genesToBnums(genes);

fprintf('failed to look up %d b-numbers (out of %d)\n', length(genes) - length(find(found)), length(genes));
mapping = zeros(size(found));
%igidx = union(strmatch('IG_', data.probe_set_name), strmatch('AFFX', ...
%                                                  data.probe_set_name));
%gidx = setdiff(1:length(data.probe_set_name), igidx);
cdsIdx = strmatch('CDS', data.probe_set_type)';
for i = find(found)
	status = 0;
	for j = 1:length(cdsIdx)
		if strfind(data.locus{cdsIdx(j)}, bNums{i})
			mapping(i) = j;
			status = 1;
			break
		end
	end
	if ~status
		fprintf('Could not find a match for %s (%s)\n', bNums{i}, reg_b3.genes{i});
	end

end
genes = bNums;
genes(find(mapping)) = data.friendly_name(mapping(find(mapping)));
gidx = mapping;

%use omniome to translate genes to b-numbers
function [bNums, found] = genesToBnums(genes)
bNums = {};
found = [];
mysql('open', 'atp.bu.edu', 'reader', 'reader', 'regulon5_6');
for i = 1:length(genes)
	gene = genes{i};
	if regexp(gene, 'b\d+{4}')
		bNums{i} = gene;
		found = 1;
		continue
	end
	%find b-number the old way (using omniome)
	%query = ['select blattner_id from omniome.ncbi_genes where gene_symbol = ''', gene, ''''];
	query = ['select OS.object_synonym_name as bNumber from gene G, object_synonym OS where G.gene_id = OS.object_id and gene_name = ''', gene, ''' and OS.object_synonym_name regexp ''^b[[:digit:]]{4}$'''];	query = ['select OS.object_synonym_name as bNumber from gene G, object_synonym OS where G.gene_id = OS.object_id and gene_name = ''', gene, ''' and OS.object_synonym_name regexp ''^b[[:digit:]]{4}$'''];
	result = mysql(query);
	if isempty(result)
		query = ['select object_synonym_name as bNumber from object_synonym where object_synonym_name regexp ''^b[[:digit:]]{4}$'' and object_id in (select object_id from object_synonym where object_synonym_name = ''', gene, ''');'];
		result = mysql(query);
		if isempty(result)
			bNums{i} = '';
			found(i) = 0;
		else
			bNums{i} = result(1).bNumber;
			found(i) = 1;
			if length(result) > 1
				5;
			end
		end
	else
		bNums{i} = result(1).bNumber;
		found(i) = 1;
	end
end
mysql('close')
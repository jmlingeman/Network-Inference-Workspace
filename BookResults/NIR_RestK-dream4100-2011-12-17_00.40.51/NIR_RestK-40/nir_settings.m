% This file runs NIRest with a file, and saves the output.

% First read in file into an NxM matrix.

rfile = textread('/home/jesse/Workspace/School/MastersThesis/Program/output//NIR_RestK-dream4100-2011-12-17_00.40.51//NIR_RestK-40//data//insilico_size100_1_knockouts.tsv', '%s', 'delimiter', '\n');

% Split the file on spaces
rfile = regexp(rfile, ' ', 'split');

% Read in experiment headers
exp_names = regexp(rfile{1}, '\t', 'split');
exp_names = exp_names{1};

% Now read in gene names and exp values
geneNames = cell(length(rfile)-1, 1);
ratios = zeros(length(geneNames), length(exp_names));
for i=2:size(rfile)
    l = regexp(rfile{i}, '\t', 'split');
    l = l{1};
    geneNames{i-1} = l{1}; % Read in gene name
    for j = 2:length(l)
        ratios(i-1,j-1) = str2num(l{j});
    end
end

dones = zeros(length(geneNames));
m = length(geneNames);
dones(1:m+1:end) = 1;
pert = dones;
while size(pert, 2) < size(ratios,2)
     pert = [pert dones];
end
if size(pert, 2) > size(ratios, 2)
    pert = pert(:,1:size(ratios,2));
end


out = nir(ratios, pert, 5, 40);
out
%out = nirest(ratios);

% Now save the output so we can read it back in, col header is exp and
% row header is gene names
outfile = fopen('../output/nir_output.txt', 'w');
% fprintf exp name header
for i=1:length(geneNames)
   fprintf(outfile, '%s\t', geneNames{i});
end
fprintf(outfile, '\n');
for i=1:size(out,1)
   fprintf(outfile, '%s\t', geneNames{i});
   for j=1:size(out,2)
       fprintf(outfile, '%f\t', out(i,j));
   end
   fprintf(outfile, '\n');
end
fclose(outfile)

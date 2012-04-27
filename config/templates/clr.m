% Params n and k
n = '{{num_bins}}';
if(strcmp(n, ''))
   n = 10;
else
   n = str2num(n);
end

k = '{{spline_degree}}';
if(strcmp(k, ''))
   k = 3;
else
   k = str2num(k);
end

t = '{{clr_type}}';
if(strcmp(t,''))
    t = 'normal';
end

% First read in file into an NxM matrix.
rfile = textread('{{ratio_files}}', '%s', 'delimiter', '\n');

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

% Calculate MI values
[Astouffer, MI] = clr(ratios, 'stouffer', n, k);

% Now run CLR
out = clr(MI, t);

% TODO: Add this functionality
%remove non-TF data
% zidx = setdiff(1:length(reg_b3.ps_idx), reg_b3.tfidxTest);
% Anormal(:, zidx) = 0;
% Arayleigh(:, zidx) = 0;
% Abeta(:, zidx) = 0;
% Astouffer(:, zidx) = 0;
% Akde(:, zidx) = 0;
% Aplos(:, zidx) = 0;

% Now save the output so we can read it back in, col header is exp and
% row header is gene names
outfile = fopen('../output/clr_output.txt', 'w');
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
fclose(outfile);

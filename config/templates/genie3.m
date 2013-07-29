%This script shows examples of how to call the functions genie3(),
%genie3_single(), and get_link_list().
%
%
%Author:
%Van Anh HUYNH-THU
%Department of Electrical Engineering and Computer Science, Systems and
%Modeling
%GIGA-Research, Bioinformatics and Modeling
%University of Liege, Belgium
%Email: vahuynh@ulg.ac.be

% Modified by Jesse Lingeman to support automatic configuration.

%% Load expression file

% First read in file into an NxM matrix.
%rng('shuffle')
rfile = textread('{{ratio_files}}', '%s', 'delimiter', '\n', 'bufsize', 20000);

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

expr_matrix = ratios'; % Transpose it to it is experiments x genes

%% Optional parameters of genie3:

% Indexes of genes that are used as input genes
input_idx = 1:length(geneNames);

% Random Forest procedure
tree_method = '{{tree_method}}';

% Parameter K is set to the total number of input genes
K = '{{k}}';

% Ensembles of 500 trees are grown
nb_trees = {{num_trees}};

% Run genie3
VIM = genie3(expr_matrix,input_idx,tree_method,K,nb_trees);



%% To run get_link_list with its default parameters:
%get_link_list(VIM);

% Write all putative edges to a file
%get_link_list(VIM,1:size(VIM,1),{},0,'ranking_edges.txt');

% Indexes of genes that are used as input genes
%input_idx = [1 2 3];
%get_link_list(VIM,input_idx,{},0);

% Get the names of the genes
%names = textread('genenames.txt','%s');

% Write the first 5 edges
get_link_list(VIM,1:size(VIM,1),geneNames,0,'../output/ranked_edges.txt');

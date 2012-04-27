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

%% Random expression matrix with 10 genes and 20 experiments
expr_matrix = randn(20,10);



%% To run genie3 with its default parameters:
VIM = genie3(expr_matrix);

%% Optional parameters of genie3:

% Indexes of genes that are used as input genes
input_idx = [2 5 6 8 9];

% Random Forest procedure
tree_method = 'RF';

% Parameter K is set to the total number of input genes
K = 'all';

% Ensembles of 500 trees are grown
nb_trees = 500;

% Run genie3
VIM2 = genie3(expr_matrix,input_idx,tree_method,K,nb_trees);



%% To run genie3_single with its default parameters:
i = 5; % Index of the target gene

vi = genie3_single(expr_matrix,i);

%% Optional parameters of genie3_single are the same as for genie3:

% Indexes of genes that are used as input genes
input_idx = [1 2 3];

% Random Forest procedure
tree_method = 'ET';

% Parameter K is set to 2
K = 2;

% Ensembles of 100 trees are grown
nb_trees = 100;

% Run genie3_single
vi2 = genie3_single(expr_matrix,i,input_idx,tree_method,K,nb_trees);

%% To run get_link_list with its default parameters:
get_link_list(VIM);

% Write all putative edges to a file
get_link_list(VIM,1:size(VIM,1),{},0,'ranking_edges.txt');

% Indexes of genes that are used as input genes
input_idx = [1 2 3];
get_link_list(VIM,input_idx,{},0);

% Get the names of the genes
names = textread('genenames.txt','%s');

% Write the first 5 edges
get_link_list(VIM,1:size(VIM,1),names,5);
function VIM = genie3(expr_matrix,input_idx,tree_method,K,nb_trees)
%Computation of tree-based weights for all putative edges.
%
%VIM = genie3(expr_matrix) learns p tree models from expr_matrix, where p
%is the number of columns (genes) in expr_matrix, and assigns a weight to
%each edge directed from any gene in expr_matrix to any other gene.
%expr_matrix is a matrix containing expression values. Each line
%corresponds to an experiment and each column corresponds to a gene.
%VIM is a matrix of size p x p. VIM(i,j) is the weight of edge directed
%from the ith gene of expr_matrix to the jth gene. VIM(i,i) is set to zero
%for all i.
%
%VIM = genie3(expr_matrix,input_idx) only uses as input
%genes the genes whose index (as ordered in expr_matrix) is in input_idx.
%input_idx is a vector of length <= p. VIM(i,:) such that i is not in
%input_idx is set to zero. The default vector contains the indexes of all
%genes in expr_matrix.
%
%VIM = genie3(expr_matrix,input_idx,tree_method) specifies
%which tree precedure is used. Available methods are:
%   * 'RF' - Random Forests (Default method)
%   * 'ET' - Extra Trees
%
%VIM = genie3(expr_matrix,input_idx,tree_method,K)
%specifies the number K of randomly selected attributes at each node of one
%tree. Possible values of K:
%   * 'sqrt' - K = square root of the number of input genes (Default value)
%   * 'all' - K = number of input genes
%   * any numerical value
%
%VIM =
%genie3(expr_matrix,input_idx,tree_method,K,nb_trees)
%specifies the number of trees grown in an ensemble. Default value:
%1000.
%
%
%Author:
%Van Anh HUYNH-THU
%Department of Electrical Engineering and Computer Science, Systems and
%Modeling
%GIGA-Research, Bioinformatics and Modeling
%University of Liege, Belgium
%Email: vahuynh@ulg.ac.be

%%
tic;
%rng('shuffle')

%% Check input arguments
error(nargchk(1,5,nargin));

nb_genes = size(expr_matrix,2);

if nargin > 1 && sum(ismember(input_idx,1:nb_genes)) ~= length(input_idx)
    error('Input argument input_idx must be a vector containing integers between 1 and p, where p is the number of genes in expr_matrix.')
end

if nargin > 2 && sum(strcmp(tree_method,{'RF' 'ET'})) == 0
    error('Input argument tree_method must be ''RF'' or ''ET''.')
end

if nargin > 3 && isa(K,'char') && sum(strcmp(K,{'sqrt' 'all'})) == 0
    error('Input argument K must be ''sqrt'', ''all'' or a numerical value.')
end

if nargin > 4 && ~isa(nb_trees,'numeric')
    error('Input argument nb_trees must be an integer.')
end


%% Learn an ensemble of trees for each gene and compute importance


VIM = zeros(nb_genes,nb_genes);

for i=1:nb_genes
    if nargin == 1
        VIM(i,:) = genie3_single(expr_matrix,i);
    elseif nargin == 2
        VIM(i,:) = genie3_single(expr_matrix,i,input_idx);
    elseif nargin == 3
        VIM(i,:) = genie3_single(expr_matrix,i,input_idx,tree_method);
    elseif nargin == 4
        VIM(i,:) = genie3_single(expr_matrix,i,input_idx,tree_method,K);
    else
        VIM(i,:) = genie3_single(expr_matrix,i,input_idx,tree_method,K,nb_trees);
    end
end

VIM = VIM';

%%
toc;

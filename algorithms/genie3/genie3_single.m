function vi = genie3_single(expr_matrix,output_idx,input_idx,tree_method,K,nb_trees)
%Computation of tree-based weights for putative edges directed towards a 
%specified target gene.
%
%vi = genie3_single(expr_matrix,output_idx) learns a tree model from
%expr_matrix and assigns a weight to each edge directed from a putative
%regulator to the target gene. 
%   * expr_matrix is a matrix containing expression values. Each line
%   corresponds to an experiment and each column corresponds to a gene. 
%   * output_idx is the (column) index of the target gene in expr_matrix.
%vi is a vector of length p, where p is the number of columns in 
%expr_matrix. vi(i) is the weight of edge directed from the ith gene of
%expr_matrix to the target gene. vi(output_idx) is set to zero.
%
%vi = genie3_single(expr_matrix,output_idx,input_idx) only uses as input
%genes the genes whose index (as ordered in expr_matrix) is in input_idx. 
%input_idx is a vector of length <= p. vi(i) such that i is not in 
%input_idx is set to zero. The default vector contains the indexes of all 
%genes in expr_matrix.
%
%vi = genie3_single(expr_matrix,output_idx,input_idx,tree_method) specifies
%which tree precedure is used. Available methods are:
%   * 'RF' - Random Forests (Default method)
%   * 'ET' - Extra Trees
%
%vi = genie3_single(expr_matrix,output_idx,input_idx,tree_method,K)
%specifies the number K of randomly selected attributes at each node of one
%tree. Possible values of K:
%   * 'sqrt' - K = square root of the number of input genes (Default value)
%   * 'all' - K = number of input genes
%   * any numerical value
%
%vi =
%genie3_single(expr_matrix,output_idx,input_idx,tree_method,K,nb_trees)
%specifies the number of trees grown in the ensemble. Default value:
%1000.
%
%Author:
%Van Anh HUYNH-THU
%Department of Electrical Engineering and Computer Science, Systems and
%Modeling
%GIGA-Research, Bioinformatics and Modeling
%University of Liege, Belgium
%Email: vahuynh@ulg.ac.be


%% Check input arguments
error(nargchk(2,6,nargin));

if length(output_idx) ~= 1
    error('Input argument output_idx must be one integer.')
end

if ~ismember(output_idx,1:size(expr_matrix,2))
    error('Input argument output_idx must be an integer between 1 and p, where p is the number of genes in expr_matrix.')
end
   
if nargin > 2 && sum(ismember(input_idx,1:size(expr_matrix,2))) ~= length(input_idx)
    error('Input argument input_idx must be a vector containing integers between 1 and p, where p is the number of genes in expr_matrix.')
end

if nargin > 3 && sum(strcmp(tree_method,{'RF' 'ET'})) == 0
    error('Input argument tree_method must be ''RF'' or ''ET''.')
end

if nargin > 4 && isa(K,'char') && sum(strcmp(K,{'sqrt' 'all'})) == 0
    error('Input argument K must be ''sqrt'', ''all'' or a numerical value.')
end

if nargin > 5 && ~isa(nb_trees,'numeric')
    error('Input argument nb_trees must be an integer.')
end

%% Data must be in single precision when used to build a tree model
expr_matrix = single(expr_matrix);
nb_samples = size(expr_matrix,1); % number of experiments
nb_genes = size(expr_matrix,2); % number of genes

%% Output vector
fprintf('\nTarget gene %d...\n',output_idx);
output = expr_matrix(:,output_idx);
% Output is normalized to have zero mean and unit variance
output_norm = (output - mean(output)) / std(output,1);


%% Indexes of input genes
if nargin >= 3
    input_idx = unique(input_idx);
else
    % Default: all genes are putative regulators
    input_idx = 1:nb_genes;
end

input_idx = setdiff(input_idx,output_idx);
nb_inputs = length(input_idx);


%% Tree parameters

% Default parameters: Random Forests, K=sqrt(number of input genes),
% 1000 trees in the ensemble
if nargin < 4 || (nargin >= 4 && strcmp(tree_method,'RF'))
    ok3ensparam = init_rf();
    print_method = 'Random Forests';
    print_K = round(sqrt(nb_inputs));
    if nargin >= 5
        if strcmp(K,'all')
            ok3ensparam=init_rf(nb_inputs);
            print_K = nb_inputs;
        elseif isa(K,'numeric')
            ok3ensparam=init_rf(round(K));
            print_K = K;
        end
    end
elseif nargin >= 4 && strcmp(tree_method,'ET')
    ok3ensparam = init_extra_trees();
    print_method = 'Extra Trees';
    print_K = round(sqrt(nb_inputs));
    if nargin >= 5
        if strcmp(K,'all')
            ok3ensparam=init_extra_trees(nb_inputs);
            print_K = nb_inputs;
        elseif isa(K,'numeric')
            ok3ensparam=init_extra_trees(round(K));
            print_K = K;
        end
    end
end

% Number of trees in the ensemble
if nargin < 6
    ok3ensparam.nbterms = 1000;
    print_ntrees = 1000;
else
    ok3ensparam.nbterms = nb_trees;
    print_ntrees = nb_trees;
end

fprintf('Tree method = %s, K = %d, %d trees\n',print_method,print_K,print_ntrees);
fprintf('\n');

    

%% Learning of tree model
[tree,varimp]=rtenslearn_c(expr_matrix(:,input_idx),output_norm,int32(1:nb_samples),[],ok3ensparam,0);
vi = zeros(1,nb_genes);
vi(input_idx) = varimp';
vi = vi / nb_samples;

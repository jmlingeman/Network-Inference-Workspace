% Leave-out-last analysis, 76 genes, RMA data
% gradient state-space optimization of kinetic equation, linear dynamics
% no data normalization

% Load the 76-gene MAS5 dataset
if ~exist('gene_dataset_filename', 'var')
  % Default one
  gene_dataset_filename = 'Arabidopsis_n76.mat';
  % We could also load the other RMA dataset
end
load(gene_dataset_filename);

% Select data for leave-out-1 analysis
% Return <yTrain>, <yTest>, <knownTrain>, <knownTest>,
% <deltaTtrain> and <deltaTtest>
Script_Arabidopsis_DataLeave1;

% Initialize the default parameters:
% * gradient state-space optimization
% * kinetic equation
% * linear dynamics
% * no data normalization
params = DFG_Params_Default_Arabidopsis(76, 67, geneNames);

% Compute consistent genes
params = Script_Arabidopsis_ConsistentGenes(params, yTrain, yTest);

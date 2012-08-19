% Load the dataset
load('Arabidopsis_n76.mat');

% Initialize the parameters
params = DFG_Params_Default_Arabidopsis(76, 67, geneNames);

% Subdivide the variables into training and testing
for k = 1:4
  yTrain{k} = KNO3norm{k}(:, 1:(end-1));
  yTest{k} = KNO3norm{k}(:, (end-1):end);
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Select the variables for sign evaluation
for k = 1:4
  sigY{k} = sign(diff(KNO3norm{k}(:, 6:7), 1, 2));
end
params.indVars = (sigY{1} == sigY{2}) & (sigY{2} == sigY{3}) & ...
  (sigY{3} == sigY{4}) & (sigY{4} == sigY{1});
params.indVars = find(params.indVars);

% Time steps
deltaT = cell(1, 4);
for k = 1:4
  deltaTtrain{k} = [3 3 3 3 3];
  deltaTtest{k} = [5];
end

% Transfer function
params.dynamic_transfer = 'tanh';

% Learning rate
params.eta_z = 0.05;
params.eta_w = 1;
params.m_step_depth = 7;

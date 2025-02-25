% Load the dataset
load('Arabidopsis_n76.mat');
n_seq = 4;

% Initialize the parameters, so that there are no TFs
params = DFG_Params_Default_Arabidopsis(76, 76, geneNames);
params.n_steps_display = 1;

% Do not use standardized values, use un-standardized instead
KNO3norm = KNO3;


% Subdivide the variables into training and testing
for k = 1:n_seq
  yTrain{k} = KNO3norm{k};
  yTest{k} = KNO3norm{k};
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
end

% Select the variables for sign evaluation
for k = 1:n_seq
  sigY{k} = sign(diff(KNO3norm{k}(:, 6:7), 1, 2));
end
params.indVars = (sigY{1} == sigY{2}) & (sigY{2} == sigY{3}) & ...
  (sigY{3} == sigY{4}) & (sigY{4} == sigY{1});
params.indVars = find(params.indVars);

% Time steps
deltaT = cell(1, n_seq);
for k = 1:n_seq
  deltaTtrain{k} = [3 3 3 3 3 5];
  deltaTtest{k} = deltaTtrain{k};
end

% Learning rate
params.eta_z = 0.1; % tau=4: 0.2,  tau=9: 0.2, tau=15: 0.2
params.eta_w = 0.1;  % tau=4: 0.01, tau=9: 0.1, tau=15: 0.2
params.m_step_depth = 7;

% Number of epochs
params.n_epochs = 25;

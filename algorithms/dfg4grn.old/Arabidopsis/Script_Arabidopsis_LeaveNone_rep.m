% Load the dataset
load('Arabidopsis_n76.mat');
n_seq_train = 7;
n_seq_test = 4;

% Initialize the parameters
params = DFG_Params_Default_Arabidopsis(76, 67, geneNames);

% Do not use standardized values, use un-standardized instead
KNO3norm = KNO3;

% Subdivide the variables into training and testing
yTrain{1} = KNO3norm{1}(:, 1:7);
yTrain{2} = KNO3norm{2}(:, 1:3);
yTrain{3} = KNO3norm{3}(:, 1:2);
yTrain{4} = KNO3norm{4}(:, 1:2);
yTrain{5} = KNO3norm{2}(:, 5:7);
yTrain{6} = KNO3norm{3}(:, 4:7);
yTrain{7} = KNO3norm{4}(:, 5:7);
for k = 1:n_seq_train
  knownTrain{k} = ones(size(yTrain{k}));
  deltaTtrain{k} = [3];
end
yTest = yTrain;
knownTest = knownTrain;
deltaTtest = deltaTtrain;

% Select the variables for sign evaluation
for k = 1:n_seq_test
  sigY{k} = sign(diff(KNO3norm{k}(:, 6:7), 1, 2));
  sigYbefore{k} = sign(diff(KNO3norm{k}(:, 5:6), 1, 2));
end
params.indVars = (sigY{1} == sigY{2}) & (sigY{2} == sigY{3}) & ...
  (sigY{3} == sigY{4}) & (sigY{4} == sigY{1});
params.indVars = find(params.indVars);

% Evaluate the naive trend prediction
n_total = length(params.indVars) * n_seq_test;
n_correct = 0;
for k = 1:n_seq_test
  n_correct = n_correct + ...
    sum(sigYbefore{k}(params.indVars) == sigY{k}(params.indVars));
end
fprintf(1, 'Naive prediction trend error: %.2f%%\n', ...
  100 * n_correct / n_total);

% Learning rate
params.eta_z = 0.1; % tau=4: 0.2,  tau=9: 0.2, tau=15: 0.2
params.eta_w = 0.1;  % tau=4: 0.01, tau=9: 0.1, tau=15: 0.2
params.m_step_depth = 7;

% Number of epochs
params.n_epochs = 25;

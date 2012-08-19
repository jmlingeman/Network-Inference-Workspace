% Load the dataset
load('Arabidopsis_n550.mat');
n_seq = 4;

% Initialize the parameters
params = DFG_Params_Default_Arabidopsis(550, 67, geneNames);

% Do not use standardized values, use un-standardized instead
KNO3norm = KNO3;

% Subdivide the variables into training and testing
for k = 1:n_seq
  yTrain{k} = KNO3norm{k}(:, 1:(end-1));
  yTest{k} = KNO3norm{k}(:, (end-1):end);
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Select the variables for sign evaluation
for k = 1:n_seq
  sigY{k} = sign(diff(KNO3norm{k}(:, 6:7), 1, 2));
  sigYbefore{k} = sign(diff(KNO3norm{k}(:, 5:6), 1, 2));
end
if (n_seq == 4)
  params.indVars = (sigY{1} == sigY{2}) & (sigY{2} == sigY{3}) & ...
    (sigY{3} == sigY{4}) & (sigY{4} == sigY{1});
  params.indVars = find(params.indVars);
else
  params.indVars = 1:length(sigY{1});
end

% Evaluate the naive trend prediction
n_total = length(params.indVars) * n_seq;
n_correct = 0;
for k = 1:n_seq
  n_correct = n_correct + ...
    sum(sigYbefore{k}(params.indVars) == sigY{k}(params.indVars));
end
fprintf(1, 'Naive prediction trend error: %.2f%%\n', ...
  100 * n_correct / n_total);

% Time steps
deltaT = cell(1, n_seq);
for k = 1:n_seq
  deltaTtrain{k} = [3 3 3 3 3];
  deltaTtest{k} = [3];
end

% Learning rate
params.eta_z = 0.1;
params.eta_w = 0.1;
params.m_step_depth = 7;

% Number of epochs
params.n_epochs = 200;

% Create a dataset
global MODEL
MODEL{1}.dynamic.bias = zeros(3,1);
MODEL{1}.dynamic.connections = true(3);
w = [1 0.5 0; 0.2 0.3 0.5; 0 -1 0];
MODEL{1}.dynamic.w = w;
MODEL{1}.dynamic.transfer = 'tanh';
MODEL{1}.dynamic.pde_type = 'kinetic';
z = rand(3, 20);
for k = 2:20
  z(:, k) = DFG_Dynamics_AR1_Forward(z(:, k-1), 1, 0.5, 0.5, 1);
end

% Number of genes and sequences
n_seq = 1;
n_genes = 3;

% Initialize the parameters
params = DFG_Params_Default_Debug_Tanh();
params.indVars = 1:n_genes;

% Subdivide the variables into training and testing
yTrain = {z};
yTest = {z};
knownTrain = {ones(size(z))};
knownTest = {ones(size(z))};
knownTest{1}(:, end) = 0;

% Time steps
deltaTtrain = {ones(1, 19)};
deltaTtest = {ones(1, 19)};

% Target GRN
params.targetGRN = (abs(w) > 0);

% Parameters
params.tau = 2;
params.gamma = 1;

% Number of epochs
params.n_epochs = 10;

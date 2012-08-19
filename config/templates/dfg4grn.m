% First we must read in the data set provided and format it in the style
% that DFG4GRN expects.
%udata = load(unclustered_data_fn);
data = struct;


global geneNames
files = '{{ratio_files}}';
prior_file = '{{prior_file}}';
prior_type = '{{prior_type}}';
dex_file = '{{dex_file}}';
sprintf('%s ', files)
files = regexp(files,' ','split');

% Read in replicates, each of which should be in a separate file
ratios = cell(1, length(files));
for f=1:length(files)
    sprintf('%s', files{f})
    % Read in the data file
    rfile = textread(files{f}, '%s', 'delimiter', '\n');

    % Split the file on spaces
    rfile = regexp(rfile, ' ', 'split');

    % Read in experiment headers
    exp_names = regexp(rfile{1}, '\t', 'split');
    exp_names = exp_names{1};

    % Now read in gene names and exp values
    geneNames = cell(length(rfile)-1, 1);
    ratios{f} = zeros(length(geneNames), length(exp_names));
    for i=2:size(rfile)
        l = regexp(rfile{i}, '\t', 'split');
        l = l{1};
        geneNames{i-1} = l{1}; % Read in gene name
        for j = 2:length(l)
            ratios{f}(i-1,j-1) = str2num(l{j});
        end
    end
end


deltaT = [ {{delta_t}} ];
n_test_points = {{num_test_points}};

if length(deltaT) == 1
    d = zeros(size(ratios{1},2)-1,1);
    d(1:size(ratios{1},2)-1) = deltaT(1);
    deltaT = d';
end

% Set up our deltas
delta_train = deltaT(1:length(deltaT) - n_test_points);
delta_test = deltaT(length(deltaT) - n_test_points : length(deltaT));


% Read in our list of transcription factors
TFNames = textread('{{tfs_file}}', '%s', ...
    'delimiter', '\n', 'whitespace', '');

% Now create a list of non-tfs
nonTFNames = cell(size(geneNames) - size(TFNames));
k = 1;
for i = 1:length(geneNames)
    found = 0;
    for j = 1:length(TFNames)
        strcmp(geneNames{i},TFNames{j})
        if strcmp(geneNames{i},TFNames{j}) == 1
            found = 1;
            break
        end
    end
    if found == 0
        nonTFNames{k} = geneNames{i};
        k = k+1;
    end
end

save('{{mat_ratio_file}}');
clear;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Load the dataset
load('ratios.mat');
n_seq = length(ratios);

% Initialize the parameters
params = DFG_Params_Default_Arabidopsis(length(geneNames), length(TFNames), geneNames);

% params.dynamic_connections(logical(eye(size(params.dynamic_connections)))) = 0;

% Subdivide the variables into training and testing
% Subdivide the variables into training and testing
if ~strcmp(prior_file, '') && ~strcmp(prior_file, 'None');
  %DFG_Params_Modify(params, 'dynamic_connections', prior);
  params.prior_file = prior_file;
  if strcmp(prior_type, 'weights')
    params.file_prior_weights = 1;
  elseif strcmp(prior_type, 'connections')
    params.file_prior_connections = 1;
  elseif strcmp(prior_type, 'both')
    params.file_prior_weights = 1;
    params.file_prior_connections = 1;
  end
end
params.use_dex = {{use_dex}}
if params.use_dex && ~strcmp(dex_file, '') && ~strcmp(dex_file, 'None')
    params.dex_file = dex_file;
end
for k = 1:n_seq
  yTrain{k} = ratios{k}(:, 1:(end-1));
  yTest{k} = ratios{k}(:, (end-1):end);

  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Select the variables for sign evaluation

% for k = 1:n_seq
%   sigY{k} = sign(diff(ratios{k}(:, size(ratios{k},2)-1:size(ratios{k},2)), 1, 2));
%   sigYbefore{k} = sign(diff(ratios{k}(:, size(ratios{k},2)-2:size(ratios{k},2)-1), 1, 2));
% end
% if (n_seq == 4)
%   params.indVars = (sigY{1} == sigY{2}) & (sigY{2} == sigY{3}) & ...
%     (sigY{3} == sigY{4}) & (sigY{4} == sigY{1});
%   params.indVars = find(params.indVars);
% else
%   params.indVars = 1:length(sigY{1});
% end
%
% % Evaluate the naive trend prediction
% n_total = length(params.indVars) * n_seq;
% n_correct = 0;
% for k = 1:n_seq
%   n_correct = n_correct + ...
%     sum(sigYbefore{k}(params.indVars) == sigY{k}(params.indVars));
% end
% fprintf(1, 'Naive prediction trend error: %.2f%%\n', ...
%   100 * n_correct / n_total);

% Time steps
for k = 1:n_seq
  deltaTtrain{k} = deltaT(1:length(deltaT) - n_test_points);
  deltaTtest{k} = deltaT(length(deltaT) - (n_test_points - 1): length(deltaT));
end

% Learning rate
%params.eta_z = 0.1;
%params.lambda_w = 0.1;
%params.tau = 3;

params.lambda_w = {{lambda_w}}
params.eta_z = {{eta_z}}
params.tau = {{tau}}
params.m_step_depth = 10;
params.gamma = 0.01;
inferelator = {{inferelator}};
params.dex_target = '{{dex_target}}';
if(inferelator)
    params.gamma = 0;
    params.lambda_w = 0.005;
    params.eta_z = 0.1;
end

% Use LARS
params.dynamic_algorithm = 'lars';

% Number of epochs
params.n_epochs = 35;

% Compute consistent genes
params = Script_Arabidopsis_ConsistentGenes(params, yTrain, yTest);
params.n_steps_display = 10;

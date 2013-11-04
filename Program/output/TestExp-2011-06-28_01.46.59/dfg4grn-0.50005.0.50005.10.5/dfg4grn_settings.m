% First we must read in the data set provided and format it in the style
% that DFG4GRN expects.
%udata = load(unclustered_data_fn);
data = struct;
deltaT = [ [50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50] ];
n_test_points = 1;

% Set up our deltas
delta_train = deltaT(1:length(deltaT) - n_test_points);
delta_test = deltaT(length(deltaT) - n_test_points : length(deltaT));

files = '/home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-0.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-1.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-2.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-3.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-4.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-5.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-6.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-7.csv /home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//Dream4-Noise-8.csv';
sprintf('%s ', files)
files = regexp(files,' ','split');

% Read in replicates, each of which should be in a separate file
ratios = cell(1, length(files))
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
        l = regexp(rfile{i}, '\t', 'split')
        l = l{1}
        geneNames{i-1} = l{1}; % Read in gene name
        for j = 2:length(l)
            ratios{f}(i-1,j-1) = str2num(l{j});
        end
    end
end


% Read in our list of transcription factors
TFNames = textread('/home/jesse/Workspace/School/MastersThesis/Program/output//TestExp-2011-06-28_01.46.59//dfg4grn-0.50005.0.50005.10.5//data//transcription_factor_list.csv', '%s', ...
    'delimiter', '\n', 'whitespace', '');

% Now create a list of non-tfs
nonTFNames = cell(size(geneNames) - size(TFNames));
k = 1;
for i = 1:size(geneNames)
    found = 0;
    for j = 1:size(TFNames)
        if strcmp(geneNames(i),TFNames(j)) == 1
            found = 1;
            break
        end
    end
    if found == 1
        nonTFNames(k) = geneNames(i);
        k = k+1;
    end
end


save('ratios.mat');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Load the dataset
load('ratios.mat');
n_seq = length(ratios);

% Initialize the parameters
params = DFG_Params_Default_Arabidopsis(length(geneNames), length(TFNames), geneNames);

% Subdivide the variables into training and testing
for k = 1:n_seq
  yTrain{k} = ratios{k}(:, 1:(end-1));
  yTest{k} = ratios{k}(:, (end-1):end);
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Select the variables for sign evaluation

for k = 1:n_seq
  sigY{k} = sign(diff(ratios{k}(:, size(ratios{k},2)-1:size(ratios{k},2)), 1, 2));
  sigYbefore{k} = sign(diff(ratios{k}(:, size(ratios{k},2)-2:size(ratios{k},2)-1), 1, 2));
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
for k = 1:n_seq
  deltaTtrain{k} = deltaT(1:length(deltaT) - n_test_points);
  deltaTtest{k} = deltaT(length(deltaT) - (n_test_points - 1): length(deltaT));
end

% Learning rate
params.eta_z = 0.1;
params.eta_w = 0.1;
params.m_step_depth = 7;

% Number of epochs
params.n_epochs = 4;

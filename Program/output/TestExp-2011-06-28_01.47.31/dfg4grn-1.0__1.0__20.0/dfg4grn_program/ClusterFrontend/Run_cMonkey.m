
% Launch cMonkey for the data that we have, return list of genes

current_dir = pwd; % Save the current directory for when we're done
cmonkey_dir = '../cMonkey';

cd cmonkey_dir;

% Set up params to use the transcription factors as priors
params.tf_priors = true;
params.tf_priors_file = 'AtRegNet.confirmed.sif';

main_script = 'R_scripts/main.R';
cluster_file = ['output/' org_name 'ClusterGeneList.csv']
r_cmd = ['R --vanilla < ' main_script];

if exist(output_file) == 0
    clusters = textread(cluster_file, '%s', 'delimiter', '\n', ...
                'whitespace', '');
else

    % Running cMonkey because output file doesn't already exist
    % While the file doesn't exist, wait
    while exist(output_file) == 0
       fprintf(1, '.')
       sleep(5);
    end

    % Now read in the file and adjust the input data appropriately
    clusters = textread(cluster_file, '%s', 'delimiter', '\n', ...
                'whitespace', '');
end

% So now that the clusters are read in, we have to grab the dataset we're
% using and average those values to get clusters.





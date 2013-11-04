%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% ClusterGenes
% This file contains the script to launch cMonkey from within Matlab.
% The goal is to be able to call this script, which will call cMonkey.
% This script will attempt to set up cMonkey for use based on your data
% by pulling in data from known data repositories.  Right now, it just
% launches a pre-configured cMonkey run with the gene list we are using
% here in GRN.
% Params:
%   out_dir - this is what to call the test.  The results will be
%              saved in a folder with this name.
%   num_models - this is the number of models to run per hyperparamter
%                selection.  This should be between 20 and 100.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [geneNames, KNO3, tfs, deltaT] = ...
        GRN_ClusterGenes(unclustered_data_fn, cluster_fn)
    udata = load(unclustered_data_fn);
    cdata = struct;
    
    % Copy over the data we don't need to format.
    cdata.tfs = udata.tfs;
    cdata.deltaT = udata.deltaT;

    % Read in out clusters.
    cdata.clusters = textread(cluster_fn, '%s', ...
        'delimiter', '\n', 'whitespace', '');
    
    % Index the cluster gene names with the gene names present in the data
    % NOTE: NEED TO FIX THIS FOR GENES IN MULTIPLE CLUSTERS
    cdata.geneClustIndex = cell(length(udata.geneNames),1);
    for i = 1:length(udata.geneNames)
        for j = 1:length(cdata.clusters)
            if findstr(upper(udata.geneNames{i}), ...
                    upper(cdata.clusters{j}))
                sprintf('Adding gene %s to cluster %d', udata.geneNames{i}, j)
                cdata.geneClustIndex{i} = [cdata.geneClustIndex{i} j] ;
            end
        end
    end

    % Now that the genes have been indexed to their cluster, create
    % clustered expression data and a clustered TF matrix

    % TODO: Since our example data uses only KNO3, we're just going to that
    % for now.  In the future, this needs to be a parameter.
    cdata.KNO3 = cell(length(udata.KNO3),1);
    for i = 1:length(cdata.KNO3)
        cdata.KNO3{i} = zeros(length(cdata.clusters), size(udata.KNO3{1},2));
    end
    for rep = 1:length(cdata.KNO3) % Loop over each replicate
        for time = 1:size(cdata.KNO3{i},2) % And each time point
            for c = 1:length(cdata.clusters)
               clusterSum = 0;
               count = 0;
               for g = 1:length(cdata.geneClustIndex)
                  if any(cdata.geneClustIndex{g} == c)
                      clusterSum = clusterSum + udata.KNO3{rep}(g,time);
                      count = count + 1;
                  end
               end
               cdata.KNO3{rep}(c,time) = clusterSum / count; % Take average of values
            end
        end
    end

    % Now we need to make the transcription factors.  We are just going to
    % bring them in each as their own cluster.

    for i = 1:length(cdata.KNO3)
        cdata.KNO3{i} = [udata.KNO3{i}(1:length(udata.tfs),:)...
            ; cdata.KNO3{i}];
    end
    
    % Creating a new geneNames matrix for the output
    
    cdata.geneNames = cell(length(cdata.tfs) + length(cdata.clusters),1);
    cdata.geneNames(1:length(cdata.tfs)) = cdata.tfs;
    for i = 1:length(cdata.clusters)
        cdata.geneNames{length(udata.tfs) + i} = ['cluster' int2str(i)];
    end
    
    save([cluster_fn '.mat'], 'cdata');
    
    geneNames = cdata.geneNames;
    KNO3 = cdata.KNO3;
    tfs = cdata.tfs;
    deltaT = cdata.deltaT;
end
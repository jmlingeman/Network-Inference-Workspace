%--------------------------------------------------------------------------
% Distributed replica removal analysis.  Once the grid search or sim
% annealing returns, cut out a time point from a replica and reanalyze,
% keeping the change in error from the original model.
%--------------------------------------------------------------------------
function results = GRN_DistribRepReplace(data, tau, eta_w, eta_z, out_dir) 

% Generate new models, send off to SGE to compute N number of times

mkdir([outdir '/rep_data']);

orig_data = data;
for i = 1:length(data)
    for j = 1:length(data{i})
        data{i}(:,j) = 0;
        
        save([outdir '/rep_data/rep' i '-' j], data);
        
        
        data = orig_data;
    end
end

% Leave-out-last analysis, 76 genes, RMA data
% gradient state-space optimization of kinetic equation, linear dynamics
% no data normalization

% Call default script
Script_Arabidopsis76_Leave1;

% Use random walk dynamics
% Remember not to optimize for tau
params.pde_type = 'delta';

% Normalize the data
params.normalize_data = 1;

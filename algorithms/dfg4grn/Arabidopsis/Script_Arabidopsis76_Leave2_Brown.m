% Leave-out-two-last analysis, 76 genes
% gradient state-space optimization of kinetic equation, linear dynamics
% no data normalization

% Call default script
Script_Arabidopsis76_Leave2;

% Use random walk dynamics
% Remember not to optimize for tau
params.pde_type = 'delta';

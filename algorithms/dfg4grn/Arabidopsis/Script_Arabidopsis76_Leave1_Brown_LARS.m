% Leave-out-last analysis, 76 genes, RMA data
% LARS state-space optimization of random walk equation, linear dynamics
% no data normalization

% Call default script
Script_Arabidopsis76_Leave1;

% Use random walk dynamics
% Remember not to optimize for tau
params.pde_type = 'delta';

% Use LARS gradient for LASSO optimization
params.dynamic_algorithm = 'lars';

% Leave-out-last analysis, 76 genes, RMA data
% LARS state-space optimization of kinetic equation, linear dynamics
% no data normalization (implicit in LARS)

% Call default script
Script_Arabidopsis76_Leave1;

% Use LARS gradient for LASSO optimization
params.dynamic_algorithm = 'lars';
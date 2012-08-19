% Leave-out-two-last analysis, 76 genes
% LARS state-space optimization of kinetic equation, linear dynamics
% no data normalization (implicit in LARS)

% Call default script
Script_Arabidopsis76_Leave2;

% Use LARS gradient for LASSO optimization
params.dynamic_algorithm = 'lars';

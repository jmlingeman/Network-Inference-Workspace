function [Aest Pest] = nirest(E,topd,restk)

%%%%%%%%%%%%%%%% NIRest - NIR with P estimation %%%%%%%%%%%%%%%%%%%%%%%%%%
%   Network Inference by Linear Regression with estimation of            %
%   perturbation matrix P                                                %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 

% Input parameters:
% E: ........ the expression matrix of size N x M, where N=number of 
%             genes, M=number of expts.
% topd:...... top solutions with min least square error (default value 5)
% restk:..... maximum connectivity considered in the network (default 10)

% Output Parameters:
% Aest: ...... estimated A matrix.
% Pest: ...... estimated matrix of perturbations 


warning off MATLAB:SingularMatrix
warning off MATLAB:nearlySingularMatrix
warning off MATLAB:divideByZero


tic

%@
%@ set parameter values
%@
if 1~=exist('topd')  % Optional ridge regression parameter
    topd = 5;
end

if 1~=exist('restk')  % Optional ridge regression parameter
    restk = 2;
end


fprintf('\n******************************************************************\n');
fprintf('***  NIRest v2.0  [topd: %d, restk: %d]\n',topd, restk);
fprintf('******************************************************************\n');


[no_genes, no_expts] = size(E);

if no_expts > no_genes
    
    % selection of experiments based on std
    [B,colsToKeep] = sort(std(E),'descend');
    
    E = E(:,colsToKeep(1:no_genes));
    no_expts = no_genes;
    
end

E2 = E;

E_nonoise = E;
noiselevel = 0.1;



method = 5;

fprintf('>>> nirest2 - using method %d for guessing P \n', method);

switch method
    
    %@
    %@ preassigned P
    %@
    case 1
        Pguess = load(['NetsAndExperiments/100g/10spars/static/10nl/global/Pert_', num2str(nid) ,'.txt']);
        
        
        %@                                   %@
        %@ estimate P using corrcoeff matrix  %
        %@                                   %@
        
    case 2
        % corrcoeff method - no selection at all
        Alasso = zeros(no_genes,no_genes);
        [R,P] = corrcoef(E');
        Alasso = R;
        Pguess = -Alasso*E;
        
    case 3
        % corrcoeff method - selection based on absolute value of p-value
        Alasso = zeros(no_genes,no_genes);
        [R,P] = corrcoef(E');
        for g = 1:no_genes
            IX = find(P(g,:)<0.1);
            Alasso(g,IX) = R(g,IX);
        end
        Pguess = -Alasso*E;
        
    case 4
        % corrcoeff method - selection based on relative value of p-value
        Alasso = zeros(no_genes,no_genes);
        [R,P] = corrcoef(E');
        for g = 1:no_genes
            ave_p = mean(P(g,:));
            std_p = std(P(g,:));
            p_thre = ave_p - 3*std_p;
            IX = find(P(g,:)<p_thre);
            Alasso(g,IX) = R(g,IX);
        end
        Pguess = -Alasso*E;
        
    case 5
        % corrcoeff method - selection based on p-value and predetermined number of elems per row
        Alasso = zeros(no_genes,no_genes);
        [R,P] = corrcoef(E');
        keepers = 1.0*restk;  % # of elems per row to keep
        for g = 1:no_genes
            [B,IX] = sort(P(g,:));
            Alasso(g,IX(1:keepers)) = R(g,IX(1:keepers));
        end
        Pguess = -Alasso*E;
        
        
        
        
    otherwise
        disp('>>>>>> nirest2 - Unknown method.')
        
end


%@
%@ find A
%@
[Anir,std_Anir] = nir(E,Pguess,topd,restk,E_nonoise,noiselevel);

Aest = Anir;
    Pest = Pguess;
   


toc










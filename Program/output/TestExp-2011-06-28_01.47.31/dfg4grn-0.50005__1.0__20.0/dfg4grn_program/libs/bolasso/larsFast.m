function W = larsFast(X,y)
%#eml
% Author: Karl Skoglund, IMM, DTU, kas@imm.dtu.dk
% Reference: 'Least Angle Regression' by Bradley Efron et al, 2003.
% 
% Modified heavily by Matthew Dunham to make eml complient. 
% See lars.m for documentation: this version performs the same operations as
% lars(X,y,'lasso',0,1,[],0);


%% LARS variable setup
[n p] = size(X);
totalNvars = min(n-1,p); 
iterMax = 8*totalNvars; % Maximum number of iterations

beta = zeros(3*totalNvars, p); %more space than we need, but we'll trim later
position = zeros(n, 1); % current "position" as LARS travels towards lsq solution

inactiveSet = 1:p; icounter = p;
activeSet = zeros(1,p); acounter = 0;

Gram = X'*X; 

lassoCondition = 0; 
iter = 0; 
currentNvars = 0; 


%% LARS main loop
while (currentNvars < totalNvars) && (iter < iterMax)
    iter = iter + 1;
    correlation = X'*(y - position);
      
    maxCorr = 0;
    j = 0;
    for i=1:icounter
       val = max(abs(correlation(inactiveSet(i))));
       if(val > maxCorr)
           maxCorr = val;
           j = i;
       end
    end
    
    
    
    j = inactiveSet(j);

    if ~lassoCondition % if a variable has been dropped, do one iteration with this configuration (don't add new one right away)
        acounter = acounter + 1;
        activeSet(acounter) = j;
        ndx = 0;
        for i=1:numel(inactiveSet)
            if(inactiveSet(i) == j)
                ndx = i;
                break;
            end
        end
        for i=ndx:numel(inactiveSet)-1
            inactiveSet(i) = inactiveSet(i+1);
        end
        inactiveSet(end) = 0;
        icounter = icounter -1;
        currentNvars = currentNvars + 1;
    end
    
    
    s = sign(correlation(activeSet(1:acounter))); % get the signs of the correlations

    S = s*ones(1,currentNvars);
    GA1 = inv(Gram(activeSet(1:acounter),activeSet(1:acounter)).*S'.*S)*ones(currentNvars,1);
    AA = 1/sqrt(sum(GA1));
    w = AA*GA1.*s; % weights applied to each active variable to get equiangular direction
    u = X(:,activeSet(1:acounter))*w; % equiangular direction (unit vector)

    if currentNvars == totalNvars % if all variables active, go all the way to the lsq solution
        gamma = maxCorr/AA;
    else
        tmpNDX = inactiveSet(1:icounter);
        a = X'*u; % correlation between each variable and eqiangular vector
        temp = [(maxCorr - correlation(tmpNDX))./(AA - a(tmpNDX)); (maxCorr + correlation(tmpNDX))./(AA + a(tmpNDX))];

        minTemp = realmax;
        for i=1:numel(temp)
            if(temp(i) < minTemp && temp(i) > 0)
                minTemp = temp(i);
            end
        end
        gamma = min([minTemp; maxCorr/AA]);
    end

    % LASSO modification

    lassoCondition = 0;
    temp2 = -beta(iter,activeSet(1:acounter))./w';
    minTemp2 = realmax;
    for i=1:numel(temp2)
        if(temp2(i) < minTemp2 && temp2(i) > 0)
            minTemp2 = temp2(i);
        end
    end
    [gamma_tilde] = min([minTemp2,gamma]);


    for i=1:numel(temp2)
        if(temp2(i) == gamma_tilde)
            j = i;
            break;
        end
    end

    if gamma_tilde < gamma,
        gamma = gamma_tilde;
        lassoCondition = 1;
    end

    position = position + gamma*u;
    beta(iter+1,activeSet(1:acounter)) = beta(iter,activeSet(1:acounter)) + gamma*w';


    % If LASSO condition satisfied, drop variable from active set
    if lassoCondition == 1
        icounter = icounter + 1;
        inactiveSet(icounter) = activeSet(j);
        for i=j:numel(activeSet)-1
            activeSet(i) =  activeSet(i+1);
        end
        activeSet(end) = 0;
        acounter = acounter -1;
        currentNvars = currentNvars - 1;
    end

end

% trim beta
if size(beta,1) > iter+1
    W = beta(1:iter+1, :);
else
    W = beta;
end

if iter == iterMax
    disp('LARS warning: Forced exit. Maximum number of iteration reached.');
end

end
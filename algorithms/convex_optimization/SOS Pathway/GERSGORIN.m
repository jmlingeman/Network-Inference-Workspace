function f = GERSGORIN(NetSize,ExpSize,A_init,X,U,S,t)

file_name = strcat('UNSTABLE_SOS_',num2str(1000*t),'t');
%load(file_name);
A = A_init;


Big = 1e+2;
epsilon = 1e-3;
for i = 1 : NetSize
    for j = 1 : NetSize
        if S(i,j) == 1;
            Lower(i,j) = 2*epsilon;
            Upper(i,j) = Big;
        elseif S(i,j) == -1;
            Lower(i,j) = -Big;
            Upper(i,j) = -2*epsilon;
        elseif S(i,j) == 0
            Lower(i,j) = -epsilon/2;
            Upper(i,j) = epsilon/2;
        else
            Lower(i,j) = -Big;
            Upper(i,j) = Big;
        end
    end
end

% Start Optimization
iter = 0;
v_iter = 0;
converged = 0;
A_old = ones(NetSize,NetSize);

% Initialize Cardinality Minimization Weights
W = ones(NetSize,NetSize);

% Initialize Gersgorin Weights
d = ones(NetSize,1);
D = (1./d)*d';

while converged == 0
    iter = iter + 1;
    strcat('GERSGORIN:     t = ',num2str(t),'     Iter = ',num2str(iter))
    
    cvx_begin quiet
    variable A(NetSize,NetSize);
    variable T(NetSize,NetSize);
    variable G(NetSize,ExpSize);
    variable gama(1,1);
    variable C(NetSize,NetSize);
    minimize (t*sum(sum(T)) + (1-t)*gama^2);
    subject to;
    % Cardinality Minimization Constraints
    T >= -W.*A;
    T >= W.*A;
    T >= 1e-5;
    T <= Big;
    % Sign Constraints
    A >= Lower;
    A <= Upper;
    % Error Fit Constraints
    A*X+U <= G;
    A*X+U >= -G;
    sum(sum(G)) <= gama;
    G >= 1e-5;
    G <= Big;
    % Stability Constraints (Gersgorin Disks)
    (~eye(NetSize)).*A <= (~eye(NetSize)).*D.*C;
    (~eye(NetSize)).*A >= -(~eye(NetSize)).*D.*C;
    sum((~eye(NetSize)).*C,2) <= -1e-3 - diag(A);
    C >= 1e-5;
    C <= Big;
    cvx_end
    
    % Update Cardinality Weights
    W = 1e-2./(1e-2+abs(A));
    
    % Update Gersgorin Weights
    if iter == 1*v_iter+1
        v_iter = v_iter + 1;
        d = diag(abs(A)) - sum((~eye(NetSize)).*abs(A),2);
        av = sum(d)/NetSize;
        z = 1e-2;
        d = (d<av).*(z./(z-(d-av)))+(d>=av).*(1+(d-av)./(z+(d-av)));
        D = (1./d)*d';
    end
    
    A_sparse{iter} = A.*(abs(A)>epsilon);
    conv_rate(iter) = norm(A_sparse{iter}-A_old);
    A_old = A_sparse{iter};
    
    % Check Convergence (Monotonic or Periodic)
    if iter == 30
        converged = 1;
        period = 1;
        'Slow convergence: Maximum number of iterations is reached.'
    else
        j = iter;
        while (j >= 2) & (converged == 0)
            j = j - 1;
            if norm(A_sparse{iter}-A_sparse{j}) <= 1e-4
                period = iter-j;
                if period == 1
                    converged = 1;
                else
                    if j-period+1 >= 1
                        count = 1;
                        for l = j-period+1 : j-1
                            if norm(A_sparse{l}-A_sparse{l+period}) <= 1e-4
                                count = count + 1;
                            end
                        end
                        if count == period;
                            converged = 1;
                        end
                    end
                end
            end
        end
    end
    
end
% A_sparse{length(A_sparse)}


file_name = strcat('GERSGORIN_SOS_',num2str(1000*t),'t');
save(file_name,'NetSize','ExpSize','A_init','A','A_sparse','X','U','S','conv_rate','t');



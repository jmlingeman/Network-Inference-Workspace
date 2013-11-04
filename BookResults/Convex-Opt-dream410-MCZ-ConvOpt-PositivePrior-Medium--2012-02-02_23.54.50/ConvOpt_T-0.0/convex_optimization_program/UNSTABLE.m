function f = UNSTABLE(NetSize,ExpSize,A_init,X,U,S,t)

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
converged = 0;
A_old = ones(NetSize,NetSize);

% Initialize Cardinality Minimization Weights
W = ones(NetSize,NetSize);

while converged == 0
    iter = iter + 1;
    strcat('UNSTABLE:     t = ',num2str(t),'     Iter = ',num2str(iter))
    
    cvx_begin quiet
    variable A(NetSize,NetSize);
    variable T(NetSize,NetSize);
    variable G(NetSize,ExpSize);
    variable gama(1,1);
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
    cvx_end
    
    % Update Cardinality Weights
    W = 1e-2./(1e-2+abs(A));
    
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

file_name = strcat('UNSTABLE_SOS_',num2str(1000*t),'t');
save(file_name,'NetSize','ExpSize','A_init','A','A_sparse','X','U','S','conv_rate','t');



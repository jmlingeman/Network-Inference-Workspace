function f = GenerateNets(NetSize,Connect,Samples)


for k = 1 : Samples
    k
    
    clear A_init;
    
    strcat('NetSize = ',num2str(NetSize),'  Sample = ',num2str(k));
        
    % Generate a random sparcity pattern S with desired connectivity
    S=ones(NetSize,NetSize);
    for i = 1 : NetSize
        idx = randperm(NetSize);
        S(i,idx(1:floor(NetSize*(1-Connect)))) = 0;
        S(i,i) = 1;
    end;
    
    % Generate a random network A with the desired sparsity pattern
    A = S.*randn(NetSize,NetSize);
    
    % Generate upper and lower bounds on the entries of A (for nonzero
    % entries: -100 <= a_ij <= 100, while for zero entries: 0 <= a_ij <= 0)
    L = -100*S-1e-3*(~S);
    U = 100*S+1e-3*(~S);
    
    % Stabilize A (perturb it by a matrix D)
    cvx_begin quiet
    variable D(NetSize,NetSize);
    variable e(1,1);
    variable g(1,1);
    minimize norm(D);
    subject to;
    % Stability constraints
    e*eye(NetSize)-.5*(A+D+A'+D') == semidefinite(NetSize);
    .5*(A+D+A'+D')-g*eye(NetSize) == semidefinite(NetSize);
    g - e <= -1e-5;
    e <= -1e-2;
    % Sign constraints
    A+D <= U;
    A+D >= L;
    cvx_end
    
    A_init=(A+D).*S;
    A = A_init;
    
    % Check Network Stability
    if max(real(eig(A_init)))>=0
        real(eig(A_init))
        error('Matrix A_init is unstable');
    end    
    
    file_name = strcat('SAMPLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(k));
    save(file_name,'NetSize','Connect','A_init');
    
end

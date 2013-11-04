function f = UNSTABLE(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t)


for k = 1 : Samples
    
    clear A_init A A_sparse X U S conv_rate period;
        
    file_name = strcat('SAMPLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(k));
    load(file_name);
    A = A_init;   
    
    % Generate and Perturb Data with Noise
    m = floor(NetSize/ExpSize); r = NetSize - m*ExpSize; U = [];
    for i = 1 : m; U = [U;eye(ExpSize)]; end;
    if r > 0; U = [U;eye(r,ExpSize)]; end;
    U = U/10;
%     U = eye(NetSize,ExpSize)/100;
    Xr = -inv(A)*U;
    X = Xr + Noise*Xr.*randn(NetSize,ExpSize);
    
    % Create Sign Pattern
    mask = (rand(NetSize,NetSize) < APrioriSigns);
    S = sign(A).*mask + 10*(~mask);
    
    
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
        strcat('UNSTABLE:     ExpSize = ',num2str(ExpSize),'  Signs = ',num2str(APrioriSigns),'  Noise = ',num2str(Noise),'  t = ',num2str(t),'  Sample = ',num2str(k),'  Iter = ',num2str(iter))
        
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
                                if norm(A_sparse{l}-A_sparse{l+period})
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
    
    file_name = strcat('UNSTABLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
    save(file_name,'NetSize','Connect','ExpSize','Noise','APrioriSigns','A_init','A','A_sparse','X','U','S','conv_rate','period','t');
    
end

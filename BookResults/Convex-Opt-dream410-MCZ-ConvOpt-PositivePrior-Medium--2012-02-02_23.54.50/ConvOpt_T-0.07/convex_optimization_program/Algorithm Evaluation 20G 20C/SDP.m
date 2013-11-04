function f = SDP(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples, t)


for k = 1 : Samples
    
    clear A_init A A_sparse X U S conv_rate period;
    
    file_name = strcat('UNSTABLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
    load(file_name);
    
    if max(real(eig(A_sparse{size(A_sparse,2)}))) < -1e-5
        
        strcat('SDP:     ExpSize = ',num2str(ExpSize),'  Signs = ',num2str(APrioriSigns),'  Noise = ',num2str(Noise),'  t = ',num2str(t),'  Sample = ',num2str(k))
        strcat('Exiting optimization. Matrix is stable.')
        file_name = strcat('SDP_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
        save(file_name,'NetSize','Connect','ExpSize','Noise','APrioriSigns','A_init','A','A_sparse','X','U','S','conv_rate','period','t');
        
    else
        
        A = A_sparse{size(A_sparse,2)};
        A_unstable = A;
        
        strcat('SDP:     ExpSize = ',num2str(ExpSize),'  Signs = ',num2str(APrioriSigns),'  Noise = ',num2str(Noise),'  t = ',num2str(t),'  Sample = ',num2str(k),'  Iter = ',num2str(0))
        
        
        % Stabilize A
        flag = zeros(1,3);
        flag(1) = 1;
        cvx_begin quiet
        variable L(NetSize,NetSize);
        variable P(NetSize,NetSize) symmetric;
        minimize (norm(L*X));
        subject to;
        -A'*P - L' - P*A - L == semidefinite(NetSize);
        P - eye(NetSize) == semidefinite(NetSize);
        cvx_end
        
        [A,P,flag] = Check_And_Restabilize(A,A_unstable,P,flag,NetSize);
        A_stable = A;
        
        
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
        P_iter = 0;
        converged = 0;
        A_old = ones(NetSize,NetSize);
        
        % Initialize Cardinality Minimization Weights
        W = ones(NetSize,NetSize);
        
        while converged == 0
            iter = iter + 1;
            strcat('SDP:     ExpSize = ',num2str(ExpSize),'  Signs = ',num2str(APrioriSigns),'  Noise = ',num2str(Noise),'  t = ',num2str(t),'  Sample = ',num2str(k),'  Iter = ',num2str(iter))
            
            cvx_begin quiet
            variable A(NetSize,NetSize);
            variable T(NetSize,NetSize);
            variable gama(1,1);
            variable G(NetSize,ExpSize);
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
            % Stability Constraint
            -(A+1e-2*eye(NetSize))'*P - P*(A+1e-2*eye(NetSize)) == semidefinite(NetSize);
            cvx_end
            
            
            % Check Success and Stability of Optimization
            [A,P,flag_new] = Check_And_Restabilize(A,A_unstable,P,flag,NetSize);
            A_stable = A;
            
            % If successful continue, otherwise, restart optimization
            if sum(flag) ~= sum(flag_new)
                
                flag = flag_new;
                
                iter = 0;
                P_iter = 0;
                converged = 0;
                A_old = ones(NetSize,NetSize);
                
                % Initialize Cardinality Minimization Weights
                W = ones(NetSize,NetSize);
                
            else
                
                flag = flag_new;
                
                % Update Lyapunov Matrix
                if iter == -1 %iter == 1*P_iter+1
                    P_iter = P_iter + 1;
                    cvx_begin quiet
                    variable P(NetSize,NetSize) symmetric;
                    minimize 0
                    subject to;
                    -A'*P - P*A == semidefinite(NetSize);
                    P - eye(NetSize) == semidefinite(NetSize);
                    cvx_end
                end
                
                % Update Cardinality Weights
                W = 1e-2./(1e-2+abs(A));
                
                A_sparse{iter} = A.*(abs(A)>epsilon);
                conv_rate(iter) = norm(A_sparse{iter}-A_old);
                A_old = A_sparse{iter};
                
                % Check Convergence (Monotonic or Periodic)
                if iter == 45
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
            
            
        end
        
        file_name = strcat('SDP_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
        save(file_name,'NetSize','Connect','ExpSize','Noise','APrioriSigns','A_init','A','A_sparse','X','U','S','conv_rate','period','t');
        
    end
    
end
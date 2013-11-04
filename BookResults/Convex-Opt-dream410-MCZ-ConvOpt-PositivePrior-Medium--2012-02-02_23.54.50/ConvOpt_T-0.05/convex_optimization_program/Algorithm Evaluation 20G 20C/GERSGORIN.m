function f = GERSGORIN(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples, t)


for k = 1 : Samples
    
    clear A_init A A_sparse X U S conv_rate period;
    
    file_name = strcat('UNSTABLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));    load(file_name);
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
        strcat('GERSGORIN:     ExpSize = ',num2str(ExpSize),'  Signs = ',num2str(APrioriSigns),'  Noise = ',num2str(Noise),'  t = ',num2str(t),'  Sample = ',num2str(k),'  Iter = ',num2str(iter))
        
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
            if t == 0; z = 1e-1; else; z = 1e-2; end;
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
    
    file_name = strcat('GERSGORIN_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
    save(file_name,'NetSize','Connect','ExpSize','Noise','APrioriSigns','A_init','A','A_sparse','X','U','S','conv_rate','period','t');
    
end

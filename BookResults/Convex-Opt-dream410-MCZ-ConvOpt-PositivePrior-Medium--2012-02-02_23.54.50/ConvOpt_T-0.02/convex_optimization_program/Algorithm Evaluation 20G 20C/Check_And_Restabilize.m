function [A_new,P_new,flag_new] = Check_And_Restabilize(A,A_unstable,P,flag,NetSize);

flag_new = flag;

if (isnan(sum(sum(P))) == 1) | (isnan(sum(sum(A))) == 1)
    if (sum(flag) == 1)
        % If Method 1 failed, reoptimize with Method 2.
        flag_new(2) = 1;
        'Optimization failed. Restabilizing original matrix and reoptimizing (Method 2).'
        cvx_begin quiet
        variable L(NetSize,NetSize);
        variable P_new(NetSize,NetSize) symmetric;
        minimize 0;
        subject to;
        -A_unstable'*P_new - L' - P_new*A_unstable - L - 1e-3*eye(NetSize) == semidefinite(NetSize);
        P_new - eye(NetSize) == semidefinite(NetSize);
        cvx_end
        if (isnan(sum(sum(P_new))) == 1)
            % If Method 2 optimization failed, restabilize with Method 3.
            flag_new(3) = 1;
            'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
            cvx_begin quiet
            variable D(NetSize,NetSize);
            minimize norm(D);
            subject to;
            1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
            cvx_end
            if (isnan(sum(sum(D))) == 1)
                % If Method 3 optimization failed, exit.
                flag_new
                error('Exit. Matrix can not be stabilized.')
            else
                A = A_unstable + D;
                % If Method 3 optimization succeeded, check stability
                if max(real(eig(A))) < -1e-5
                    % Matrix is stable
                    A_new = A;
                    P_new = eye(NetSize);
                elseif max(real(eig(A))) >= -1e-5
                    % If Method 3 is unstable, exit.
                    real(eig(A.*(abs(A)>epsilon)))
                    flag_new
                    error('Exit. Matrix can not be stabilized.')
                end
            end
        else
            % If Method 2 optimization succeeded, check stability
            A = A_unstable + inv(P_new)*L;
            if max(real(eig(A))) < -1e-5
                % Matrix is stable
                A_new = A;
                P_new = P_new;
            elseif max(real(eig(A))) >= -1e-5
                % If Method 2 is unstable, restabilize with Method 3.
                flag_new(3) = 1;
                'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
                cvx_begin quiet
                variable D(NetSize,NetSize);
                minimize norm(D);
                subject to;
                1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
                cvx_end
                if (isnan(sum(sum(D))) == 1)
                    % If Method 3 optimization failed, exit.
                    flag_new
                    error('Exit. Matrix can not be stabilized.')
                else
                    A = A_unstable + D;
                    % If Method 3 optimization succeeded, check stability
                    if max(real(eig(A))) < -1e-5
                        % Matrix is stable
                        A_new = A;
                        P_new = eye(NetSize);
                    elseif max(real(eig(A))) >= -1e-5
                        % If Method 3 is unstable, exit.
                        real(eig(A.*(abs(A)>epsilon)))
                        flag_new
                        error('Exit. Matrix can not be stabilized.')
                    end
                end
            end
        end
    elseif (sum(flag) == 2)
        % If Method 2 failed, reoptimize with Method 3.
        flag_new(3) = 1;
        'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
        cvx_begin quiet
        variable D(NetSize,NetSize);
        minimize norm(D);
        subject to;
        1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
        cvx_end
        if (isnan(sum(sum(D))) == 1)
            % If Method 3 optimization failed, exit.
            flag_new
            error('Exit. Matrix can not be stabilized.')
        else
            A = A_unstable + D;
            % If Method 3 optimization succeeded, check stability
            if max(real(eig(A))) < -1e-5
                % Matrix is stable
                A_new = A;
                P_new = eye(NetSize);
            elseif max(real(eig(A))) >= -1e-5
                % If Method 3 is unstable, exit.
                real(eig(A.*(abs(A)>epsilon)))
                flag_new
                error('Exit. Matrix can not be stabilized.')
            end
        end
    elseif (sum(flag) == 3)
        % If Method 3 optimization failed, exit.
        flag_new
        error('Exit. Matrix can not be stabilized.')
    end
else
    % Check Stability
    if (max(real(eig(A))) < -1e-5)
        % Matrix is stable
        A_new = A;
        P_new = P;
    elseif (max(real(eig(A))) >= -1e-5) & (sum(flag) == 1)
        % If Method 1 is unstable, stabilize with Method 2.
        flag_new(2) = 1;
        'Optimization failed. Restabilizing original matrix and reoptimizing (Method 2).'
        cvx_begin quiet
        variable L(NetSize,NetSize);
        variable P_new(NetSize,NetSize) symmetric;
        minimize 0;
        subject to;
        -A_unstable'*P_new - L' - P_new*A_unstable - L - 1e-3*eye(NetSize) == semidefinite(NetSize);
        P_new - eye(NetSize) == semidefinite(NetSize);
        cvx_end
        if (isnan(sum(sum(P_new))) == 1)
            % If Method 2 optimization failed, restabilize with Method 3.
            flag_new(3) = 1;
            'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
            cvx_begin quiet
            variable D(NetSize,NetSize);
            minimize norm(D);
            subject to;
            1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
            cvx_end
            if (isnan(sum(sum(D))) == 1)
                % If Method 3 optimization failed, exit.
                flag_new
                error('Exit. Matrix can not be stabilized.')
            else
                A = A_unstable + D;
                % If Method 3 optimization succeeded, check stability
                if max(real(eig(A))) < -1e-5
                    % Matrix is stable
                    A_new = A;
                    P_new = eye(NetSize);
                elseif max(real(eig(A))) >= -1e-5
                    % If Method 3 is unstable, exit.
                    real(eig(A.*(abs(A)>epsilon)))
                    flag_new
                    error('Exit. Matrix can not be stabilized.')
                end
            end
        else
            A = A_unstable + inv(P_new)*L;
            % If Method 2 optimization succeeded, check stability
            if max(real(eig(A))) < -1e-5
                % Matrix is stable
                A_new = A;
                P_new = P_new;
            elseif max(real(eig(A))) >= -1e-5
                % If Method 3 is unstable, restabilize with Method 3.
                flag_new(3) = 1;
                'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
                cvx_begin quiet
                variable D(NetSize,NetSize);
                minimize norm(D);
                subject to;
                1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
                cvx_end
                if (isnan(sum(sum(D))) == 1)
                    % If Method 3 optimization failed, exit.
                    flag_new
                    error('Exit. Matrix can not be stabilized.')
                else
                    A = A_unstable + D;
                    % If Method 3 optimization succeeded, check stability
                    if max(real(eig(A))) < -1e-5
                        % Matrix is stable
                        A_new = A;
                        P_new = eye(NetSize);
                    elseif max(real(eig(A))) >= -1e-5
                        % If Method 3 is unstable, exit.
                        real(eig(A.*(abs(A)>epsilon)))
                        flag_new
                        error('Exit. Matrix can not be stabilized.')
                    end
                end
            end
        end
        
    elseif (max(real(eig(A))) >= -1e-5) & (sum(flag) == 2)
        % If Method 2 is unstable, stabilize with Method 3.
        flag_new(3) = 1;
        'Optimization failed. Restabilizing original matrix and reoptimizing (Method 3).'
        cvx_begin quiet
        variable D(NetSize,NetSize);
        minimize norm(D);
        subject to;
        1e-3*eye(NetSize)-.5*(D+A_unstable+D'+A_unstable') == semidefinite(NetSize);
        cvx_end
        if (isnan(sum(sum(D))) == 1)
            % If Method 3 optimization failed, exit.
            flag_new
            error('Exit. Matrix can not be stabilized.')
        else
            A = A_unstable + D;
            % If Method 3 optimization succeeded, check stability
            if max(real(eig(A))) < -1e-5
                % Matrix is stable
                A_new = A;
                P_new = eye(NetSize);
            elseif max(real(eig(A))) >= -1e-5
                % If Method 3 is unstable, exit.
                real(eig(A.*(abs(A)>epsilon)))
                flag_new
                error('Exit. Matrix can not be stabilized.')
            end
        end
        
    elseif (max(real(eig(A))) >= -1e-5) & (sum(flag) == 3)
        % If Method 3 is unstable, exit.
        real(eig(A.*(abs(A)>epsilon)))
        flag_new
        error('Exit. Matrix can not be stabilized.')
    end
end





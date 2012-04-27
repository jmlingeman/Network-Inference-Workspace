function f = RESULTS_UNSTABLE(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t)

res = [];
for k = 1 : Samples
    
    clear A_init A A_sparse X U S conv_rate period false_p false_n false_z false_IDs best_ID best_indx;
    
    file_name = strcat('UNSTABLE_',num2str(NetSize),'G_',num2str(100*Connect),'C_',num2str(ExpSize),'E_',num2str(100*Noise),'N_',num2str(100*APrioriSigns),'S_',num2str(1000*t),'t_',num2str(k));
    load(file_name);
    iter = size(A_sparse,2);
    
    for i = 1 : period
        j = iter - i + 1;
        false_p(i) = sum(sum(((sign(A_sparse{j})==ones(NetSize,NetSize))&(~(sign(A_init)==ones(NetSize,NetSize))))));
        false_n(i) = sum(sum(((sign(A_sparse{j})==-ones(NetSize,NetSize))&(~(sign(A_init)==-ones(NetSize,NetSize))))));
        false_z(i) = sum(sum(((sign(A_sparse{j})==zeros(NetSize,NetSize))&(~(sign(A_init)==zeros(NetSize,NetSize))))));
        false_IDs(i) = false_p(i) + false_n(i) + false_z(i);
    end
    [best_FIDs, best_indx] = min(false_IDs);
    if best_FIDs == 0
        false_Zs_wrt_FIDs = 1;
    else
        false_Zs_wrt_FIDs = false_z(best_indx)/best_FIDs;
    end
    Sign_Satisfaction = (sum(sum(~(S==10)))==sum(sum((S==sign(A_sparse{iter-best_indx+1})))));
    Stability_Satisfaction = (max(real(eig(A_sparse{iter-best_indx+1}))) < 1e-5);
    ID_Error = sum(sum(abs(A_sparse{iter-best_indx+1}*X+U)));
    Min_ID_Error = sum(sum(abs(A_init*X+U)));
    PerCent_Error_Difference = ID_Error/Min_ID_Error;
    Net_Connectivity = 1- sum(sum(zeros(NetSize,NetSize)==A_sparse{iter-best_indx+1}))/NetSize^2;
    
    res = [res ; k, false_p(best_indx), false_n(best_indx), false_z(best_indx), best_FIDs, false_Zs_wrt_FIDs, ...
        Sign_Satisfaction, Stability_Satisfaction, ID_Error, PerCent_Error_Difference, Net_Connectivity];  
    
end

% res

av_FPs = mean(res(:,2));
std_FPs = std(res(:,2),1);
av_FNs = mean(res(:,3));
std_FNs = std(res(:,3),1);
av_FZs = mean(res(:,4));
std_FZs = std(res(:,4),1);
av_FIDs = mean(res(:,5));
std_FIDs = std(res(:,5),1);
av_FZs_wrt_FIDs = mean(res(:,6));
std_FZs_wrt_FIDs = std(res(:,6),1);
av_Signs = mean(res(:,7));
std_Signs = std(res(:,7),1);
av_Stability = mean(res(:,8));
std_Stability = std(res(:,8),1);
av_ER = mean(res(:,9));
std_ER = std(res(:,9),1);
av_ER_diff = mean(res(:,10));
std_ER_diff = std(res(:,10),1);
av_Connect = mean(res(:,11));
std_Connect = std(res(:,11),1);

f = [av_FPs, std_FPs, ...
    av_FNs, std_FNs, ...
    av_FZs, std_FZs, ...
    av_FIDs, std_FIDs, ...
    av_FZs_wrt_FIDs, std_FZs_wrt_FIDs, ...
    av_Signs, std_Signs, ...
    av_Stability, std_Stability, ...
    av_ER, std_ER, ...
    av_ER_diff, std_ER_diff, ...
    av_Connect std_Connect];



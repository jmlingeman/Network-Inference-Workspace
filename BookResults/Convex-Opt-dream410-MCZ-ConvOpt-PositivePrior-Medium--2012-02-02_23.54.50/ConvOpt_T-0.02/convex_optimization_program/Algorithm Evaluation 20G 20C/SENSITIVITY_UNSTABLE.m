function f = SENSITIVITY_UNSTABLE(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t)

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
    
    Tr_Signs = sign(A_init);
    Id_Signs = sign(A_sparse{iter-best_indx+1});
    P = ones(NetSize,NetSize);
    N = -ones(NetSize,NetSize);
    Z = zeros(NetSize,NetSize);
    
    True_PNs = sum(sum(((Tr_Signs==P)&(Id_Signs==P))|((Tr_Signs==N)&(Id_Signs==N))));
    True_Zs = sum(sum((Tr_Signs==Z)&(Id_Signs==Z)));
    False_PNs = sum(sum(((Tr_Signs==Z)&(Id_Signs==P))|((Tr_Signs==Z)&(Id_Signs==N))));
    False_Zs = sum(sum(((Tr_Signs==P)&(Id_Signs==Z))|((Tr_Signs==N)&(Id_Signs==Z))));
    
    Sensitivity = True_PNs/(True_PNs+False_Zs);
    Specificity = 1 - True_Zs/(False_PNs+True_Zs);
    
    res = [res ; k, Sensitivity, Specificity];  
    
end

% res

av_Sensitivity = mean(res(:,2));
std_Sensitivity = std(res(:,2),1);
av_Specificity = mean(res(:,3));
std_Specificity = std(res(:,3),1);

f = [av_Sensitivity, std_Sensitivity, av_Specificity, std_Specificity];



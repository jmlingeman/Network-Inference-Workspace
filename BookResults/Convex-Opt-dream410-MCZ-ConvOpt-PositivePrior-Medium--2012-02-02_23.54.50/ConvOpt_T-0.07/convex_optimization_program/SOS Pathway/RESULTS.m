function [res_u, res_g, res_s] = RESULTS(t_vec)

res_u = []; res_g = []; res_s = [];

for t = t_vec
    
    % UNSTABLE
    file_name = strcat('UNSTABLE_SOS_',num2str(1000*t),'t');
    load(file_name);
    iter = size(A_sparse,2);
    % Identification
    false_p = sum(sum(((sign(A_sparse{iter})==ones(NetSize,NetSize))&(~(sign(A_init)==ones(NetSize,NetSize))))));
    false_n = sum(sum(((sign(A_sparse{iter})==-ones(NetSize,NetSize))&(~(sign(A_init)==-ones(NetSize,NetSize))))));
    false_z = sum(sum(((sign(A_sparse{iter})==zeros(NetSize,NetSize))&(~(sign(A_init)==zeros(NetSize,NetSize))))));
    false_ids = false_p+false_n+false_z;
    if false_ids == 0
        false_Zs_wrt_FIDs = 1;
    else
        false_Zs_wrt_FIDs = false_z/false_ids;
    end
    Sign_Satisfaction = (sum(sum(~(S==10)))==sum(sum((S==sign(A_sparse{iter})))));
    Stability_Satisfaction = (max(real(eig(A_sparse{iter}))) < 1e-5);
    ID_Error = sum(sum(abs(A_sparse{iter}*X+U)));
    Min_ID_Error = sum(sum(abs(A_init*X+U)));
    PerCent_Error_Difference = ID_Error/Min_ID_Error;
    Net_Connectivity = 1 - sum(sum(zeros(NetSize,NetSize)==A_sparse{iter}))/NetSize^2;
    % Sensitivity
    Tr_Signs = sign(A_init);
    Id_Signs = sign(A_sparse{iter});
    P = ones(NetSize,NetSize);
    N = -ones(NetSize,NetSize);
    Z = zeros(NetSize,NetSize);
    True_PNs = sum(sum(((Tr_Signs==P)&(Id_Signs==P))|((Tr_Signs==N)&(Id_Signs==N))));
    True_Zs = sum(sum((Tr_Signs==Z)&(Id_Signs==Z)));
    False_PNs = sum(sum(((Tr_Signs==Z)&(Id_Signs==P))|((Tr_Signs==Z)&(Id_Signs==N))));
    False_Zs = sum(sum(((Tr_Signs==P)&(Id_Signs==Z))|((Tr_Signs==N)&(Id_Signs==Z))));
    Sensitivity = True_PNs/(True_PNs+False_Zs);
    Specificity = 1 - True_Zs/(False_PNs+True_Zs);
    % Results
    res_u = [res_u ; t, Sensitivity, Specificity, false_p, false_n, false_z, ...
        false_ids, Sign_Satisfaction, Stability_Satisfaction, ID_Error, ...
        PerCent_Error_Difference, Net_Connectivity];
    
    
    % GERSGORIN
    file_name = strcat('GERSGORIN_SOS_',num2str(1000*t),'t');
    load(file_name);
    iter = size(A_sparse,2);
    % Identification
    false_p = sum(sum(((sign(A_sparse{iter})==ones(NetSize,NetSize))&(~(sign(A_init)==ones(NetSize,NetSize))))));
    false_n = sum(sum(((sign(A_sparse{iter})==-ones(NetSize,NetSize))&(~(sign(A_init)==-ones(NetSize,NetSize))))));
    false_z = sum(sum(((sign(A_sparse{iter})==zeros(NetSize,NetSize))&(~(sign(A_init)==zeros(NetSize,NetSize))))));
    false_ids = false_p+false_n+false_z;
    if false_ids == 0
        false_Zs_wrt_FIDs = 1;
    else
        false_Zs_wrt_FIDs = false_z/false_ids;
    end
    Sign_Satisfaction = (sum(sum(~(S==10)))==sum(sum((S==sign(A_sparse{iter})))));
    Stability_Satisfaction = (max(real(eig(A_sparse{iter}))) < 1e-5);
    ID_Error = sum(sum(abs(A_sparse{iter}*X+U)));
    Min_ID_Error = sum(sum(abs(A_init*X+U)));
    PerCent_Error_Difference = ID_Error/Min_ID_Error;
    Net_Connectivity = 1 - sum(sum(zeros(NetSize,NetSize)==A_sparse{iter}))/NetSize^2;
    % Sensitivity
    Tr_Signs = sign(A_init);
    Id_Signs = sign(A_sparse{iter});
    P = ones(NetSize,NetSize);
    N = -ones(NetSize,NetSize);
    Z = zeros(NetSize,NetSize);
    True_PNs = sum(sum(((Tr_Signs==P)&(Id_Signs==P))|((Tr_Signs==N)&(Id_Signs==N))));
    True_Zs = sum(sum((Tr_Signs==Z)&(Id_Signs==Z)));
    False_PNs = sum(sum(((Tr_Signs==Z)&(Id_Signs==P))|((Tr_Signs==Z)&(Id_Signs==N))));
    False_Zs = sum(sum(((Tr_Signs==P)&(Id_Signs==Z))|((Tr_Signs==N)&(Id_Signs==Z))));
    Sensitivity = True_PNs/(True_PNs+False_Zs);
    Specificity = 1 - True_Zs/(False_PNs+True_Zs);
    % Results
    res_g = [res_g ; t, Sensitivity, Specificity, false_p, false_n, false_z, ...
        false_ids, Sign_Satisfaction, Stability_Satisfaction, ID_Error, ...
        PerCent_Error_Difference, Net_Connectivity];
    
    
    % SDP
    file_name = strcat('SDP_SOS_',num2str(1000*t),'t');
    load(file_name);
    iter = size(A_sparse,2);
    % Identification
    false_p = sum(sum(((sign(A_sparse{iter})==ones(NetSize,NetSize))&(~(sign(A_init)==ones(NetSize,NetSize))))));
    false_n = sum(sum(((sign(A_sparse{iter})==-ones(NetSize,NetSize))&(~(sign(A_init)==-ones(NetSize,NetSize))))));
    false_z = sum(sum(((sign(A_sparse{iter})==zeros(NetSize,NetSize))&(~(sign(A_init)==zeros(NetSize,NetSize))))));
    false_ids = false_p+false_n+false_z;
    if false_ids == 0
        false_Zs_wrt_FIDs = 1;
    else
        false_Zs_wrt_FIDs = false_z/false_ids;
    end
    Sign_Satisfaction = (sum(sum(~(S==10)))==sum(sum((S==sign(A_sparse{iter})))));
    Stability_Satisfaction = (max(real(eig(A_sparse{iter}))) < 1e-5);
    ID_Error = sum(sum(abs(A_sparse{iter}*X+U)));
    Min_ID_Error = sum(sum(abs(A_init*X+U)));
    PerCent_Error_Difference = ID_Error/Min_ID_Error;
    Net_Connectivity = 1 - sum(sum(zeros(NetSize,NetSize)==A_sparse{iter}))/NetSize^2;
    % Sensitivity
    Tr_Signs = sign(A_init);
    Id_Signs = sign(A_sparse{iter});
    P = ones(NetSize,NetSize);
    N = -ones(NetSize,NetSize);
    Z = zeros(NetSize,NetSize);
    True_PNs = sum(sum(((Tr_Signs==P)&(Id_Signs==P))|((Tr_Signs==N)&(Id_Signs==N))));
    True_Zs = sum(sum((Tr_Signs==Z)&(Id_Signs==Z)));
    False_PNs = sum(sum(((Tr_Signs==Z)&(Id_Signs==P))|((Tr_Signs==Z)&(Id_Signs==N))));
    False_Zs = sum(sum(((Tr_Signs==P)&(Id_Signs==Z))|((Tr_Signs==N)&(Id_Signs==Z))));
    Sensitivity = True_PNs/(True_PNs+False_Zs);
    Specificity = 1 - True_Zs/(False_PNs+True_Zs);
    % Results
    res_s = [res_s ; t, Sensitivity, Specificity, false_p, false_n, false_z, ...
        false_ids, Sign_Satisfaction, Stability_Satisfaction, ID_Error, ...
        PerCent_Error_Difference, Net_Connectivity];
    
end


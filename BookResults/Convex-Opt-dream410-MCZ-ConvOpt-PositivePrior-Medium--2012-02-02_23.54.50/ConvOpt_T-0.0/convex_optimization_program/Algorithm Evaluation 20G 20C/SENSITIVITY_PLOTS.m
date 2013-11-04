clc;
clear all;

% Size of Network/Genes
NetSize = 20;
% Sparsity
Connect = 20/100;
% Samples
Samples = 20;


% Parameter vector
t_vec = [0 .001 .005 .01 .05 .1 .2 .5];

% Performance as a function of t
f_u = []; f_g = []; f_s = [];
for ExpSize = [ceil(NetSize/3) NetSize]
    for APrioriSigns = [0 .3]
        for Noise = [.1 .5]
            f_u = [f_u ; ExpSize, APrioriSigns, Noise, 0];
            f_g = [f_g ; ExpSize, APrioriSigns, Noise, 0];
            f_s = [f_s ; ExpSize, APrioriSigns, Noise, 0];
            for t = t_vec
                r_u = SENSITIVITY_UNSTABLE(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
                r_g = SENSITIVITY_GERSGORIN(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
                r_s = SENSITIVITY_SDP(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
                f_u = [f_u ; r_u];
                f_g = [f_g ; r_g];
                f_s = [f_s ; r_s];
            end
        end
    end
end




% Plots of Sensitivity vs. 1-Specificity
NetSize = 20;
Connect = 20/100;
ExpSize = 7;
APrioriSigns = 0.3;
Noise = .5;

for i = 1 : length(t_vec)+1 : size(f_u,1)
    if sum(f_u(i,1:3) == [ExpSize APrioriSigns Noise]) == 3
        
        [av_Specificity_u, order] = sort(f_u(i+1:i+length(t_vec),3)');
        std_Specificity_u = f_u(i+1:i+length(t_vec),4)'; std_Specificity_u = std_Specificity_u(order);
        av_Sensitivity_u = f_u(i+1:i+length(t_vec),1)'; av_Sensitivity_u = av_Sensitivity_u(order);
        std_Sensitivity_u = f_u(i+1:i+length(t_vec),2)'; std_Sensitivity_u = std_Sensitivity_u(order);
        
        [av_Specificity_g, order] = sort(f_g(i+1:i+length(t_vec),3)');
        std_Specificity_g = f_g(i+1:i+length(t_vec),4)'; std_Specificity_g = std_Specificity_g(order);
        av_Sensitivity_g = f_g(i+1:i+length(t_vec),1)'; av_Sensitivity_g = av_Sensitivity_g(order);
        std_Sensitivity_g = f_g(i+1:i+length(t_vec),2)'; std_Sensitivity_g = std_Sensitivity_g(order);
        
        [av_Specificity_s, order] = sort(f_s(i+1:i+length(t_vec),3)');
        std_Specificity_s = f_s(i+1:i+length(t_vec),4)'; std_Specificity_s = std_Specificity_s(order);
        av_Sensitivity_s = f_s(i+1:i+length(t_vec),1)'; av_Sensitivity_s = av_Sensitivity_s(order);
        std_Sensitivity_s = f_s(i+1:i+length(t_vec),2)'; std_Sensitivity_s = std_Sensitivity_s(order);
        
        figure(1);
        hold on;
        h1 = plot(av_Specificity_u,av_Sensitivity_u,'-ro','MarkerSize',7,'LineWidth',1);
        for j = 1 : length(av_Specificity_u)
            ellipse(std_Specificity_u(j),std_Sensitivity_u(j),0,av_Specificity_u(j),av_Sensitivity_u(j),'r');
        end
        h2 = plot(av_Specificity_g,av_Sensitivity_g,'-gs','MarkerSize',7,'LineWidth',1);
        for j = 1 : length(av_Specificity_u)
            ellipse(std_Specificity_g(j),std_Sensitivity_g(j),0,av_Specificity_g(j),av_Sensitivity_g(j),'g');
        end
        h3 = plot(av_Specificity_s,av_Sensitivity_s,'-b^','MarkerSize',7,'LineWidth',1);
        for j = 1 : length(av_Specificity_u)
            ellipse(std_Specificity_s(j),std_Sensitivity_s(j),0,av_Specificity_s(j),av_Sensitivity_s(j),'b');
        end
        plot([0 1],[0 1],'--k','LineWidth',1);
        
        h = legend([h1 h2 h3],'Unstable','Gersgorin','SDP','Location','Best');
        set(h,'Interpreter','latex','FontSize',22);
        legend('boxoff');
        xlabel('1-Specificity','Interpreter','latex','FontSize',18);
        ylabel('Sensitivity','Interpreter','latex','FontSize',18);
        text(.7,.65,'line of no','Interpreter','latex','FontSize',12);
        text(.7,.6,'discrimination','Interpreter','latex','FontSize',12);
%         grid on;
        box on;
%         m = min([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']-[std_FZs_wrt_FIDs' std_FIDs' std_ER_diff' std_Connect']);
%         M = max([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']+[std_FZs_wrt_FIDs' std_FIDs' std_ER_diff' std_Connect']);
%         axis([min(t_vec)-.002 max(t_vec)+.002 m-2 M+2]);
        axis([0 1 0 1]);
        hold off;
                
    end
end










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
f = [];
for ExpSize = [ceil(NetSize/3) NetSize]
    for APrioriSigns = [0 .3]
        for Noise = [.1 .5]
            f = [f ; ExpSize, APrioriSigns, Noise, zeros(1,17)];
            for t = t_vec
                r = RESULTS_UNSTABLE(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
%                 r = RESULTS_GERSGORIN(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
%                 r = RESULTS_SDP(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples,t);
                f = [f ; r];
            end
        end
    end
end




% Plots of F.IDs, F.Zs, ER compared to max values as a function of t
NetSize = 20;
Connect = 20/100;
ExpSize =20;
APrioriSigns = 0.3;
Noise = .1;

for i = 1 : length(t_vec)+1 : size(f,1)
    if sum(f(i,1:3) == [ExpSize APrioriSigns Noise]) == 3
        
        av_FPs = 100*f(i+1:i+length(t_vec),1)/NetSize^2;
        std_FPs = 100*f(i+1:i+length(t_vec),2)/NetSize^2;
        av_FNs = 100*f(i+1:i+length(t_vec),3)/NetSize^2;
        std_FNs = 100*f(i+1:i+length(t_vec),4)/NetSize^2;
        av_FZs = 100*f(i+1:i+length(t_vec),5)/NetSize^2;
        std_FZs = 100*f(i+1:i+length(t_vec),6)/NetSize^2;
        av_FIDs = 100*f(i+1:i+length(t_vec),7)/NetSize^2;
        std_FIDs = 100*f(i+1:i+length(t_vec),8)/NetSize^2;
        av_FZs_wrt_FIDs = 100*f(i+1:i+length(t_vec),9);
        std_FZs_wrt_FIDs = 100*f(i+1:i+length(t_vec),10);
        av_Signs = 100*f(i+1:i+length(t_vec),11);
        std_Signs = 100*f(i+1:i+length(t_vec),12);
        av_Stability = 100*f(i+1:i+length(t_vec),13);
        std_Stability = 100*f(i+1:i+length(t_vec),14);
        av_ER = f(i+1:i+length(t_vec),15);
        std_ER = f(i+1:i+length(t_vec),16);
        av_ER_diff = 100*f(i+1:i+length(t_vec),17);
        std_ER_diff = 100*f(i+1:i+length(t_vec),18);
        av_Connect = 100*f(i+1:i+length(t_vec),19);
        std_Connect = 100*f(i+1:i+length(t_vec),20);
        
        count = 0;
        t = []; FIDs = []; FZs = []; ER = [];
        for c = [100*Connect : 1 : 100*Connect+20]
            count = count + 1;
            stop = 0; j = 1;
            while (stop == 0) & (j <= length(t_vec)-1)
                if ((av_Connect(j) <= c) & (c < av_Connect(j+1))) | ((av_Connect(j+1) <= c) & (c < av_Connect(j)))
                    t(count) = t_vec(j) + (c-av_Connect(j))*(t_vec(j+1)-t_vec(j))/(av_Connect(j+1)-av_Connect(j));
                    FIDs(count) = av_FIDs(j) + (t(count)-t_vec(j))*(av_FIDs(j+1)-av_FIDs(j))/(t_vec(j+1)-t_vec(j));
                    FZs_wrt_FIDs(count) = av_FZs_wrt_FIDs(j) + (t(count)-t_vec(j))*(av_FZs_wrt_FIDs(j+1)-av_FZs_wrt_FIDs(j))/(t_vec(j+1)-t_vec(j));
                    ER(count) = av_ER_diff(j) + (t(count)-t_vec(j))*(av_ER_diff(j+1)-av_ER_diff(j))/(t_vec(j+1)-t_vec(j));
                    STAB(count) = av_Stability(j) + (t(count)-t_vec(j))*(av_Stability(j+1)-av_Stability(j))/(t_vec(j+1)-t_vec(j));
                    CONN(count) = c;
                    stop = 1;
                end
                j = j + 1;
            end
        end
        [CONN' t' FIDs' FZs_wrt_FIDs' ER' STAB']
        
        figure(1);
        hold on;
%         plot_title = strcat('$n$=',num2str(NetSize),', $c$=',...
%             num2str(100*Connect),'\%, $m$=',num2str(ExpSize),...
%             ', $\sigma$=',num2str(100*APrioriSigns),'\%, $\nu$=',...
%             num2str(100*Noise),'\%');
%         title(plot_title,'Interpreter','latex','FontSize',17);
        
        errorbar(t_vec,av_FIDs,std_FIDs,'-go','MarkerSize',7,'LineWidth',1);
        errorbar(t_vec,av_FZs_wrt_FIDs,std_FZs_wrt_FIDs,'-rs','MarkerSize',7,'LineWidth',1);
        errorbar(t_vec,av_ER_diff,std_ER_diff,'-b^','MarkerSize',7,'LineWidth',1);
        errorbar(t_vec,av_Connect,std_Connect,'-kd','MarkerSize',7,'LineWidth',1);
        h = legend('FIDs','FZs','ER','Connect','Location','NorthWest');
        set(h,'Interpreter','latex','FontSize',22);
        legend('boxoff');
        grid on;
        box on;
        m = min([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']-[std_FZs_wrt_FIDs' std_FIDs' std_ER_diff' std_Connect']);
        M = max([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']+[std_FZs_wrt_FIDs' std_FIDs' std_ER_diff' std_Connect']);
        axis([min(t_vec)-.002 max(t_vec)+.002 m-2 M+2]);
        hold off;
                
    end
end










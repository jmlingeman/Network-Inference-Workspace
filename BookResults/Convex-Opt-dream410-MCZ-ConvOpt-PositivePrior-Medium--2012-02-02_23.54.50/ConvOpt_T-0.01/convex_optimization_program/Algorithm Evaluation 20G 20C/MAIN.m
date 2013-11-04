clc;
clear all;

% Size of Network/Genes
NetSize = 20;
% Sparsity
Connect = 20/100;
% Samples
Samples = 20;

% Generate Networks
% GenerateNets(NetSize,Connect,Samples);

% Parameter vector
t_vec = [0 .001 .005 .01 .05 .1 .2 .5];

% Performance as a function of t
f = [];
for Connect = [20/100]
    for ExpSize = NetSize %[ceil(NetSize/3) NetSize]
        for APrioriSigns = [0 .3]
            for Noise = [.1 .5]
                for t = t_vec
                    UNSTABLE(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples, t);
                    if (APrioriSigns == 0) & (Noise == .1)
                    else
                        GERSGORIN(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples, t);
                        SDP(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples, t);
                    end
                end
            end
        end
    end
end




% % Plot of weights w_ij
% x = [0 : .001 : 1];
% w1 = 1./(1+x);
% w2 = .1./(.1+x);
% w3 = .01./(.01+x);
% figure(1);
% hold on;
% plot(x,w1,'LineWidth',1);
% plot(x,w2,'LineWidth',1);
% plot(x,w3,'LineWidth',1);
% xlabel('$|a_{ij}|$','Interpreter','latex','FontSize',24);
% ylabel('$w_{ij}$','Interpreter','latex','FontSize',24);
% text(.05+.02,.01/(.01+.05),'$\delta = .01$','Interpreter','latex','FontSize',20);
% text(.2+.05,.1/(.1+.2),'$\delta = .1$','Interpreter','latex','FontSize',20);
% text(.4+.1,1/(1+.4),'$\delta = 1$','Interpreter','latex','FontSize',20);
% axis([0 1 0 1]);
% box on;
% hold off;


% % Plot of the Gersgorin weights v_i
% av = 0;
% x = [-1 : .001 : 1];
% v1 = (x<av).*(1./(1-(x-av)))+(x>=av).*(1+(x-av)./(1+(x-av)));
% v2 = (x<av).*(.1./(.1-(x-av)))+(x>=av).*(1+(x-av)./(.1+(x-av)));
% v3 = (x<av).*(.01./(.01-(x-av)))+(x>=av).*(1+(x-av)./(.01+(x-av)));
% figure(2);
% hold on;
% plot(x,v1,'LineWidth',1);
% plot(x,v2,'LineWidth',1);
% plot(x,v3,'LineWidth',1);
% xlabel('$|a_{ii}|-R_i(A)$','Interpreter','latex','FontSize',24);
% ylabel('$v_i$','Interpreter','latex','FontSize',24);
% x1 = .25;
% text(x1,-.08+(x1<av).*(1./(1-(x1-av)))+(x1>=av).*(1+(x1-av)./(1+(x1-av))),'$\delta = 1$','Interpreter','latex','FontSize',20);
% x2 = .5;
% text(x2,-.08+(x2<av).*(.1./(.1-(x2-av)))+(x2>=av).*(1+(x2-av)./(.1+(x2-av))),'$\delta = .1$','Interpreter','latex','FontSize',20);
% x3 = -.05;
% text(x3+.03,(x3<av).*(.01./(.01-(x3-av)))+(x3>=av).*(1+(x3-av)./(.01+(x3-av))),'$\delta = .01$','Interpreter','latex','FontSize',20);
% axis([-1 1 0 2]);
% box on;
% hold off;




% UNSTABLE(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples);
% GERSGORIN(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples);
% SDP(NetSize, ExpSize, Connect, APrioriSigns, Noise, Samples);

% RESULTS(NetSize,ExpSize,Connect,APrioriSigns,Noise,Samples);
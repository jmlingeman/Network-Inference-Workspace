clc;
clear all;



% Network
% Read in the data file
X_file = ['/Users/jesse/Workspace/School/MastersThesis/Program/output//Convex-Opt-dream410-MCZ-ConvOpt-PositivePrior-Medium--2012-02-02_23.54.50//ConvOpt_T-0.06//data//insilico_size10_1_knockouts.tsv'];
if ~isempty(X_file)
    % Read in the data file
    rfile = textread(X_file, '%s', 'delimiter', '\n');

    % Split the file on spaces
    rfile = regexp(rfile, ' ', 'split');

    % Read in experiment headers
    exp_names = regexp(rfile{1}, '\t', 'split');
    exp_names = exp_names{1};

    % Now read in gene names and exp values
    geneNames = cell(length(rfile)-1, 1);
    X = zeros(length(geneNames), length(exp_names));
    for i=2:size(rfile) % First row is header
        l = regexp(rfile{i}, '\t', 'split');
        l = l{1};
%         geneNames{i-1} = l{1}; % Read in gene name
        for j = 2:length(l)
            X(i-1,j-1) = str2num(l{j});
        end
    end
end

% Read in the initialization matrix
A_init_file = [''];
if ~isempty(A_init_file)
    % Read in the data file
    rfile = textread(A_init_file, '%s', 'delimiter', '\n');

    % Split the file on spaces
    rfile = regexp(rfile, ' ', 'split');

    % Now read in gene names and exp values
    geneNames = cell(length(rfile)-1, 1);
    A_init = zeros(length(geneNames));
    for i=2:size(rfile) % First row is header
        l = regexp(rfile{i}, '\t', 'split');
        l = l{1};
%         geneNames{i-1} = l{1}; % Read in gene name
        for j = 2:length(l)
            A_init(i-1,j-1) = str2num(l{j});
        end
    end
else
    A_init = ones(length(geneNames));
    n = length(geneNames);
    % Set diag to 0
    A(1:n+1:n*n) = 0;
end
% A_init=[-1 -1 -1 1 1 -1 1 0 0;
%     1 -1 -1 1 1 -1 1 0 0;
%     1 -1 -1 1 1 -1 1 0 0;
%     0 0 0 -1 0 0 1 0 1;
%     1 -1 -1 1 -1 -1 1 0 0;
%     1 -1 -1 1 1 -1 1 0 0;
%     1 -1 -1 1 1 -1 -1 1 0;
%     0 0 0 0 0 0 1 -1 0;
%     0 0 0 0 0 0 1 0 -1];





% Size of Network/Genes
NetSize = length(geneNames);
% Size of Data/Experiments
ExpSize = length(exp_names);

% Perturbation matrix
U = eye(NetSize,NetSize)/100;
while size(U,2) < size(X,2)
    U = [U U];
end


% X = [906 -132 -139 187 291 -61 -77 -17 -25;
%     212 383 -117 64 169 -87 39 125 84;
%     18 -107 10524 61 80 13 64 89 -70;
%     104 -50 -273 139 180 146 69 -4 275;
%     119 -97 56 315 2147 142 -68 135 113;
%     76 -189 -214 250 347 2017 -67 -172 -22;
%     -122 -47 -102 -107 -11 104 3068 365 217;
%     178 -183 36 -70 -34 -155 8 26633 87;
%     72 -128 73 81 305 51 -61 274 672]/1000;

% Sign Pattern
S_file = [''];
if ~isempty(S_file)
    % Read in the data file
    rfile = textread(S_file, '%s', 'delimiter', '\n');

    % Split the file on spaces
    rfile = regexp(rfile, ' ', 'split');

    % Read in experiment headers
    exp_names = regexp(rfile{1}, '\t', 'split');
    exp_names = exp_names{1};

    % Now read in gene names and exp values
    geneNames = cell(length(rfile)-1, 1);
    S = zeros(length(geneNames));
    for i=2:size(rfile) % First row is header
        l = regexp(rfile{i}, '\t', 'split');
        l = l{1};
%         geneNames{i-1} = l{1}; % Read in gene name
        for j = 2:length(l)
            S(i-1,j-1) = str2num(l{j});
        end
    end
    S(S==0) = 10;
else
    S = zeros(length(geneNames)) + 10;
    n = length(geneNames);
    % Set diag to 0
%     S(1:n+1:n*n) = 0;
end
% S=[10 -1 10 10 10 10 1 10 10;
%     1 -1 10 10 10 10 1 10 10;
%     1 -1 10 10 10 10 1 10 10;
%     10 10 10 10 10 10 1 10 1;
%     1 -1 10 10 10 10 1 10 10;
%     1 -1 10 10 10 10 1 10 10;
%     1 -1 10 10 10 10 10 1 10;
%     10 10 10 10 10 10 1 10 10;
%     10 10 10 10 10 10 1 10 10];


% t_vec = [0 .001 .005 .01 .05 .1 .2 .3 .4 .5 .6 .7 .8 .9 1];
% t_vec = [.001 .005 .01 .05 .1 .2 .3 .4 .5 .6 .7 .8 .9 1];
% t_vec = [0.001 .005]
t_vec = [0.06]

for t = t_vec
    UNSTABLE(NetSize, ExpSize, A_init, X, U, S, t);
    %GERSGORIN(NetSize, ExpSize, A_init, X, U, S, t);
    A = SDP(NetSize, ExpSize, A_init, X, U, S, t);
end

save('../output/convex_optimization_output.mat');
% Need a routine to go through the output data files and get the best A
%[res_u, res_g, res_s] = RESULTS(t_vec);


%% Plot of Sensitivity vs. 1-Specificity
%[Specificity_u, order] = sort(res_u(:,3)');
%Sensitivity_u = res_u(:,2)'; Sensitivity_u = Sensitivity_u(order);
%[Specificity_g, order] = sort(res_g(:,3)');
%Sensitivity_g = res_g(:,2)'; Sensitivity_g = Sensitivity_g(order);
%[Specificity_s, order] = sort(res_s(:,3)');
%Sensitivity_s = res_s(:,2)'; Sensitivity_s = Sensitivity_s(order);
%figure(1);
%hold on;
%h1 = plot(Specificity_u,Sensitivity_u,'-ro','MarkerSize',7,'LineWidth',1);
%h2 = plot(Specificity_g,Sensitivity_g,'-gs','MarkerSize',7,'LineWidth',1);
%h3 = plot(Specificity_s,Sensitivity_s,'-b^','MarkerSize',7,'LineWidth',1);
%plot([0 1],[0 1],'--k','LineWidth',1);
%h = legend([h1 h2 h3],'Unstable','Gersgorin','SDP','Location','Best');
%set(h,'Interpreter','latex','FontSize',22);
%legend('boxoff');
%xlabel('1-Specificity','Interpreter','latex','FontSize',18);
%ylabel('Sensitivity','Interpreter','latex','FontSize',18);
%text(.7,.65,'line of no','Interpreter','latex','FontSize',12);
%text(.7,.6,'discrimination','Interpreter','latex','FontSize',12);
%box on;
%axis([0 1 0 1]);
%hold off;



%% Plot of Performance vs. Parameter t
%Connect = 35/100;
%res = res_s

%av_FPs = 100*res(:,4)/NetSize^2;
%av_FNs = 100*res(:,5)/NetSize^2;
%av_FZs = 100*res(:,6)/NetSize^2;
%av_FIDs = 100*res(:,7)/NetSize^2;
%av_FZs_wrt_FIDs = 100*av_FZs./av_FIDs;
%av_Signs = 100*res(:,8);
%av_Stability = 100*res(:,9);
%av_ER = res(:,10);
%av_ER_diff = 100*res(:,11);
%av_Connect = 100*res(:,12);

%count = 0;
%t = []; FIDs = []; FZs = []; ER = [];
%for c = [100*Connect : 1 : 100*Connect+20]
    %count = count + 1;
    %stop = 0; j = 1;
    %while (stop == 0) & (j <= length(t_vec)-1)
        %if ((av_Connect(j) <= c) & (c < av_Connect(j+1))) | ((av_Connect(j+1) <= c) & (c < av_Connect(j)))
            %t(count) = t_vec(j) + (c-av_Connect(j))*(t_vec(j+1)-t_vec(j))/(av_Connect(j+1)-av_Connect(j));
            %FIDs(count) = av_FIDs(j) + (t(count)-t_vec(j))*(av_FIDs(j+1)-av_FIDs(j))/(t_vec(j+1)-t_vec(j));
            %FZs_wrt_FIDs(count) = av_FZs_wrt_FIDs(j) + (t(count)-t_vec(j))*(av_FZs_wrt_FIDs(j+1)-av_FZs_wrt_FIDs(j))/(t_vec(j+1)-t_vec(j));
            %ER(count) = av_ER_diff(j) + (t(count)-t_vec(j))*(av_ER_diff(j+1)-av_ER_diff(j))/(t_vec(j+1)-t_vec(j));
            %STAB(count) = av_Stability(j) + (t(count)-t_vec(j))*(av_Stability(j+1)-av_Stability(j))/(t_vec(j+1)-t_vec(j));
            %CONN(count) = c;
            %stop = 1;
        %end
        %j = j + 1;
    %end
%end
%[CONN' t' FIDs' FZs_wrt_FIDs' ER' STAB']

%figure(2);
%hold on;
%plot(t_vec,av_FIDs,'-go','MarkerSize',7,'LineWidth',1);
%plot(t_vec,av_FZs_wrt_FIDs,'-rs','MarkerSize',7,'LineWidth',1);
%plot(t_vec,av_ER_diff,'-b^','MarkerSize',7,'LineWidth',1);
%plot(t_vec,av_Connect,'-kd','MarkerSize',7,'LineWidth',1);
%h = legend('F.IDs','F.Zs','ER','Connect','Location','NorthWest');
%set(h,'Interpreter','latex','FontSize',22);
%legend('boxoff');
%grid on;
%box on;
%m = min([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']);
%M = max([av_FZs_wrt_FIDs' av_FIDs' av_ER_diff' av_Connect']);
%axis([min(t_vec)-.002 max(t_vec)+.002 m-2 M+2]);
%hold off;








function [recovered_network,std_A] = nir(data1,pert1,topd,restk,data_nonoise,noisel)

%data1:......... input (expression) data in the format no_genes X experiments
%pert1:......... perturbation to the system which is also no_genes X experiments format.
%                Each column contains the perturbation in the corresponding experiments to each gene.
%Parameters for forwardK topD method, to optimixe mulitple regression in
%large networks
%topd:.......... top solutions with min least square error (default value 5)
%restk:......... maximum connectivity considered in the network (default 10)
%Standard deviation of the data:
%data_nonoise:.. data without noise
%noise1:........ is the noiselevel in the system.

%The algorithms solves A*data1=pert1, estimating the best A
%A is estimated choosing the best possible solution.
%A contains the parameters that indicate the relationships among genes.
%In order to solve the system we assume that only restk connections exist
%among genes, i.e. each gene is only connected to restk other genes (A is a sparse matrix).
%To select which ones are the correcg restk genes, A is built row-wise, choosing the best topd combination
%of 2 genes, 3 genes up to restk genes. The best combinations are the ones
%that minimize the least square error, since data1 and pert1 are known.
%We always assume that a gene is self connected (diagonal in A is non
%zero), thus the starting point, row-wise, to move from 2 to restk connection is the element on the
%diagonal.
%ridge, is the ridge parameter for ridge regression.(default 0.1) here
%evaluated making use, (to solve of Y = AX) ridge = min(svd([X' Y(i,:)'])) hile solving for ith row of A.

no_genes = size(data1,1);

% this is needed because NIR could be called with no_genes<restk in the
% first iterations of mNIr
restk=min(restk,no_genes);

   % [aaa1, aaa2] = size(data1); [bbb1,bbb2] = size(pert1);
   % fprintf('>>> +++ NIR started --- data1: %d x %d; pert1: %d x %d; no_genes: %d; \n',...
   %                                              aaa1,aaa2,bbb1,bbb2,no_genes);


for m = 1:no_genes    % Solving for each row in A.
    %FFFFFFFFFFFF messo a 1 dà matrici singolari altrimenti
    ridge = 0.1;
    %ridge = min(svd([data1' pert1(m,:)']));
    %if  ~mod(m,10) % ~mod(m,20)
        %fprintf('>>> nir - m = %d\n',m);
    %end
    selected_genes = m;   % First parameter in A is self feed back (diagonal in A is nonzero).
    for k = 1:(restk -1) %iterate until you don't have restk interactions
        clear finalcomb
        select = k;
        error1 = [];
        comb1 = [];

        %%%%%%%%%%%%%% Evaluates all combination of k+1 gene
        for s = 1:size(selected_genes,1)
            genes = setdiff(1:no_genes,selected_genes(s,:));
            comb = nchoosek(genes,1);
            comb_temp = (combvec(comb',selected_genes(s,:)'))';
            comb1 = [comb1;comb_temp];
        end
        [a1,a2] = unique(sort(comb1')','rows');
        comb1 = comb1(a2,:); %all possible combinations of s parameters in the row
        clear a1 a2 comb comb_temp
        for i = 1:size(comb1,1)
            comb(i,:) = setdiff(1:no_genes,comb1(i,:));
        end
        comb1 = comb;    %%All possile combination that you don't want to use (because of setfiff above)
        %%%%%%%%%%%%

        total_comb = size(comb);

        %%%%%%%%% Evaluate least square for combination of k+1 genes
        for n=1:total_comb(1)  % For all combinations comb1, find the gene connection values and compute the least square error.
            r1 = data1;
            %r1 = (round(r1*100))/100;  % Rounding off till second place of decimal, this step is not necessary.
            a=comb(n,:);

            % Implementing the prior here.  Get the genes from the prior
            % matrix,
            r1(a,:)=[];  % removing the genes that are not connected to the gene.
%             break
            % p1 = pert1';
            p1 = pert1;
            %Multiple linear regression
              % [aaa1, aaa2] = size(p1); [bbb1,bbb2] = size(r1);
              % fprintf('>>> +++ iter %d of %d --- p1: %d x %d; r1: %d x %d; no_genes: %d; a: %d\n',...
              %                                   n,total_comb(1),aaa1,aaa2,bbb1,bbb2,no_genes,length(a));
            sol(n,:) = -p1(m,:)*r1'*inv(r1*r1' + ridge*eye(no_genes-length(a)));
            %Least square error
            %%%%%% DEBUG %%%%%%%
            %if (n < 0 | m<0)
                %m
                %n
            %end
            %%%%%% DEBUG %%%%%%%
            error(n) = sum((sol(n,:)*r1 + p1(m,:)).^2);
        end
        %%%%%%%%%

        error1 = [error1 error];   % Vector of least square error

        %Sort and then select the best topd combinations
        clear sol error
        [a1,a2] = sort(error1);
        a = comb1(a2(1:topd),:);
        finalcomb=a;
        clear selected_genes
        for d1 = 1:topd
            selected_genes(d1,:) = setdiff(1:no_genes,finalcomb(d1,:));
        end
        selected_genes;
    end

    %%%%%%%%%%  Reevaluate and store the best solution
    r1 = data1;
    a=setdiff(1:no_genes,selected_genes(1,:));
    r1(a,:)=[];
    %Calculates linear regression and inputs it in A
    aa = -p1(m,:)*r1'*inv(r1*r1' + ridge*eye(no_genes-length(a)));
    A(m,1:no_genes) = 0;
    A(m,selected_genes(1,:)) = aa;
    clear aa a
    %%%%%%%%%%


%     %%%%%%%%%% To compute Std on estimated A
%     A1 = A(m,:).^2;
%     r1 = data1;
%     r1 = (round(r1*100))/100;
%     a = setdiff(1:no_genes,selected_genes(1,:));
%     r1(a,:)=[];
%     sr = abs(data_nonoise)*noisel;
%     var_sr = sr.^2;
%     var_sr(a,:)=[];
%     A1(:,a) = [];
%     total_var = (A1*var_sr)';
%     var_A = inv(r1*r1')*r1*diag(total_var)*r1'*inv(r1*r1');
%     std1 = sqrt(diag(var_A))';
%     std_A(m,1:no_genes) = 0;
%     std_A(m,selected_genes(1,:)) = std1;
%     clear A1 std1 var_A a sr
%     %%%%%%%%%%%%%%%%%%%%%%%%%


end
recovered_network = A;
% std_A = std_A;




function mb=pchIAMB(data, target, domain_counts, statistic, threshold, machines)
%**********************************************************************
% Copyright (C), DSL 2005
%**********************************************************************

% Determine number of chunks of the data, which is equal to the number of
% machines used
num_chunks=length(machines);

% Get sizes of the data
[n, m]=size(data);

% 1. Determine indices of data-splits
for i=1:num_chunks-1
    indx_start=(i-1)*floor(m/num_chunks)+1;
    indx_end=i*floor(m/num_chunks);
    eval(sprintf('indx%d=unique([%d:%d m]);', i, indx_start, indx_end));
end
indx_start=(num_chunks-1)*floor(m/num_chunks)+1;
indx_end=m;
eval(sprintf('indx%d=unique([%d:%d]);', num_chunks, indx_start, indx_end));

% 2. Shift target to be last in both data and domain_counts (if the latter
% one is non-empty)
tmp=data(:,m);
data(:,m)=data(:,target);
data(:,target)=tmp;
if ~isempty(domain_counts)
    tmp=domain_counts(m);
    domain_counts(m)=domain_counts(target);
    domain_counts(target)=tmp;
end

% 3. Prepare data packages to be sent to slaves
for i=1:num_chunks
    eval(sprintf('data%d=data(:,indx%d);',i,i));
    if ~isempty(domain_counts)
        eval(sprintf('domain_counts%d=domain_counts(indx%d);',i,i));
    else
        eval(sprintf('domain_counts%d=[];',i));
    end
end

% 4. Initialize slaves (start on Matlab session per slave)
for i=1:num_chunks
    psetup(machines{i},1);
end

% 5. Send data to slaves
MPI_Send(1:length(machines),'m');
for i=1:num_chunks
 eval(sprintf('MPI_Send(i,''data%d'');',i,i));
 eval(sprintf('MPI_Send(i,''indx%d'');',i,i));
 eval(sprintf('MPI_Send(i,''domain_counts%d'');',i,i));
end

% 6. Run IAMB on all slaves and receive results
for i=1:num_chunks
    fprintf('Starting alg_IAMB on slave %d\n',i);
    eval(sprintf('MPI_IEval(%d,''mb%d = IAMB(data%d, find(indx%d==m), domain_counts%d, statistic, threshold);'');', i, i, i, i, i));
end
for i=1:num_chunks
    eval(sprintf('mb%d=MPI_Recv(%d,''mb%d'');',i,i,i));
end

% 7. Determine m1
un=[m];
for i=1:num_chunks
    eval(sprintf('un=[un indx%d(mb%d)];',i,i));
end
un=unique(un);
if ~isempty(domain_counts)
    dc=domain_counts(un);
else
    dc=[];
end
m1= IAMB(data(:,un), find(un==m),  dc, statistic, threshold);
m1=un(m1);

% 8. Redefine data splits
for i=1:num_chunks
    eval(sprintf('indx%d=unique([indx%d m1]);',i,i));
end

% 9. Prepare data packages to be sent to slaves
for i=1:num_chunks
    eval(sprintf('data%d=data(:,indx%d);',i,i));
    if ~isempty(domain_counts)
        eval(sprintf('domain_counts%d=domain_counts(indx%d);',i,i));
    else
        eval(sprintf('domain_counts%d=[];',i));
    end
end

% 10. Send data to slaves
for i=1:num_chunks
 eval(sprintf('MPI_Send(i,''data%d'');',i,i));
 eval(sprintf('MPI_Send(i,''indx%d'');',i,i));
 eval(sprintf('MPI_Send(i,''domain_counts%d'');',i,i));
end

% 11. Run alg_IAMB on all slaves and receive results
for i=1:num_chunks
    fprintf('Starting alg_IAMB on slave %d\n',i);
    eval(sprintf('MPI_IEval(%d,''mb%d = IAMB(data%d, find(indx%d==m), domain_counts%d, statistic, threshold);'');', i, i, i, i, i));
end
for i=1:num_chunks
    eval(sprintf('mb%d=MPI_Recv(%d,''mb%d'');',i,i,i));
end

% 12. Determine m2
un=[m];
for i=1:num_chunks
    eval(sprintf('un=[un indx%d(mb%d)];',i,i));
end
un=unique(un);
if ~isempty(domain_counts)
    dc=domain_counts(un);
else
    dc=[];
end
m2= IAMB(data(:,un), find(un==m),  d, threshold, statistic, threshold);
m2=un(m2);

% 13. Return mb 
mb=m2;

% 14. Kill all slaves
MPI_Finialize;
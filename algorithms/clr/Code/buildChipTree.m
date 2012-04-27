function buildChipTree(data)
%buildChipTree(data)
%
%build a dendrogram of all experiments
%data: the usual matlab structure (with typical fields) - see
%E_coli_v3_Build_3 for an example

%--------------------------------------
%find condition clusters
%--------------------------------------
%find coefficient of variation and pick top 1000 genes
cv = std(data.rma')./mean(data.rma');
[cvsorted, cvidx] = sort(cv, 'descend');
Y = 1 - normcdf(clr(data.rma(cvidx(1:1000), :)', 'rayleigh', 10, 3));

Y = Y - diag(diag(Y));
Y = squareform(Y, 'tovector');

%Y = pdist(r', 'mahal');
Z = linkage(Y, 'complete');
[H, T, PERM] = dendrogram(Z, length(Y), 'orientation', 'left', 'labels', data.conditions);
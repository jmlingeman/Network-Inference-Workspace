function covA = calc_cov(A,X,sX,P,sP,RIDGE,W);

% covA = calc_cov(A,X,sX,P,sP [, RIDGE, W]);
% X,sX,P,SP are N x M, where N=number of genes, M=number of expts.
% RIDGE is an optional ridge regression parameter
% W is an optional weight parameter.

warning off MATLAB:nearlySingularMatrix

[rows,N]=size(A);
covA=zeros(N,N,rows);
[N,M] = size(X);

if 1~=exist('RIDGE')
    RIDGE = 0;
end

if 1~=exist('W')
    W = eye(M);
end

Q = W*W';

for g=1:rows
    idx = find(A(g,:)~=0);
    vEta = sP(g,:).^2 + A(g,:).^2 * sX.^2;
%     vEta = sP(g,g).^2 + A(g,:).^2 * sX.^2;  % Deigo's way
    Z=X(idx,:);
    T=inv(Z*Q*Z'+RIDGE*eye(length(idx)))*Z*Q';
    covA(idx,idx,g) = T*diag(vEta)*T';
end


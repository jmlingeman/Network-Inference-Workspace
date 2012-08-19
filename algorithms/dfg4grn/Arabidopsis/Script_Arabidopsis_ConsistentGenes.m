function params = Script_Arabidopsis_ConsistentGenes(params, yTrain, yTest)

% Select the variables for sign evaluation
n_seq = length(yTrain);
n_genes = size(yTrain{1}, 1);
sigY = zeros(n_genes, n_seq);
sigYbefore = zeros(n_genes, n_seq);
for k = 1:n_seq
  sigY(:, k) = sign(diff(yTest{k}(:, 1:2), 1, 2));
  sigYbefore(:, k) = sign(diff(yTrain{k}(:, (end-1):end), 1, 2));
end

% Consistent genes change in the same way
params.indVars = (abs(sum(sigY, 2)) == n_seq);

% Evaluate the naive trend prediction on consistent genes
n_total = sum(params.indVars) * n_seq;
n_correct = 0;
for k = 1:n_seq
  n_correct = n_correct + ...
    sum(sigYbefore(params.indVars, k) == sigY(params.indVars, k));
end
fprintf(1, 'Naive prediction trend error (consistent): %.2f%%\n', ...
  100 * n_correct / n_total);

% Evaluate the naive trend prediction on all genes
n_total = n_genes * n_seq;
n_correct = 0;
for k = 1:n_seq
  n_correct = n_correct + sum(sigYbefore(:, k) == sigY(:, k));
end
fprintf(1, 'Naive prediction trend error (all): %.2f%%\n', ...
  100 * n_correct / n_total);

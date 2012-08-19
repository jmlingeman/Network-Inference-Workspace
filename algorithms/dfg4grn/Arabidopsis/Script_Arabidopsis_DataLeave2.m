% This script produces the following data:
% <yTrain>, <yTest>, <knownTrain>, <knownTest>, <deltaTtrain>, <deltaTtest>
% for leave-out-last analysis

% Subdivide the variables into training and testing
n_seq = length(KNO3);
for k = 1:n_seq
  yTrain{k} = KNO3{k}(:, 1:(end-2));
  yTest{k} = KNO3{k}(:, (end-2):end);
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Time steps
for k = 1:n_seq
  deltaTtrain{k} = deltaT(1:(end-2));
  deltaTtest{k} = deltaT((end-1):end);
end

% This script produces the following data:
% <yTrain>, <yTest>, <knownTrain>, <knownTest>, <deltaTtrain>, <deltaTtest>
% for leave-none-last analysis

% Subdivide the variables into training and testing
n_seq = length(KNO3);
for k = 1:n_seq
  yTrain{k} = KNO3{k};
  yTest{k} = KNO3{k};
  knownTrain{k} = ones(size(yTrain{k}));
  knownTest{k} = ones(size(yTest{k}));
  knownTest{k}(:, end) = 0;
end

% Time steps
for k = 1:n_seq
  deltaTtrain{k} = deltaT;
  deltaTtest{k} = deltaT;
end

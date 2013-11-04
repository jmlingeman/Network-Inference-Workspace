% Example script for performing sparse principal component analysis on the
% diabetes data set.

clear;
close all;
clc;
addpath('lib');

load ../data/diabetes
X = diabetes.x2;
X = normalize(X);
[n p] = size(X);

K = 6;
lambda = 1;
stop = 3;
maxiter = 250;
trace = 1;

[sl sv pcal pcav plots] = spca(X, [], K, lambda, stop, maxiter, trace);

for k = 1:K
  figure
  plot(plots(k).data');
end

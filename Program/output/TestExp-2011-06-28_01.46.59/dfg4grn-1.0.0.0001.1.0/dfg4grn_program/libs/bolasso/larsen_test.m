% Example script for performing elastic net regression on the diabetes data
% set.

clear; close all; clc;

addpath('lib');
load ../data/diabetes

X = diabetes.x2;
X = normalize(X);
y = diabetes.y;
y = center(y);
[n p] = size(X);

b1 = larsen(X, y, 0.1, 0, 1);
t1 = sum(abs(b1),2)/max(sum(abs(b1), 2));

figure;
plot(t1, b1, '-');
title('Elastic net (LARS-EN)');

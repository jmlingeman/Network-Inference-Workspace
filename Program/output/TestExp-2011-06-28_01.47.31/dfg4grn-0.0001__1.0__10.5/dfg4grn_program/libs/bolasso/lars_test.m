% Example script for performing LASSO regression on the diabetes data
% set.

clear; close all; clc;


load diabetes

X = diabetes.x;
X = normalize(X);
y = diabetes.y;
y = center(y);
[n p] = size(X);

b1 = lars(X, y, 'lasso', 0, 0, [], 1);
s1 = sum(abs(b1),2)/sum(abs(b1(end,:)));
[s_opt, b_opt, res_mean, res_std] = crossvalidateBL(@lars, 10, 1000, X, y, 'lasso', 0, 0, [], 0);
cvplot(s_opt, res_mean, res_std);

figure;
hold on;
plot(s1, b1, '.-');
axis tight;
ax = axis;
line([s_opt s_opt], [ax(3) ax(4)], 'Color', 'r', 'LineStyle', '-.');
legend
title('Least Angle Regression (LASSO)');

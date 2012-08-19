function [min_diff, max_diff] = DFG_Delta_QuantifyDiff(y, deltaT, tau)

if iscell(y)
  min_diff = inf;
  max_diff = -inf;
  for k = 1:length(y)
    [m0, m1] = DFG_Delta_QuantifyDiff(y{k}, deltaT, tau);
    min_diff = min(min_diff, m0);
    max_diff = max(max_diff, m1);
  end
else
  dim_y = size(y, 1);
  % a = y(:, 1:(end-1)) .* (1 - repmat(deltaT, dim_y, 1) ./ tau);
  % g = (y(:, 2:end) - a) .* tau ./ repmat(deltaT, dim_y, 1);
  a = diff(y, 1, 2) ./ repmat(deltaT, dim_y, 1);
  g = tau * a + y(:, 1:(end-1));
  min_diff = min(g(:));
  max_diff = max(g(:));
end

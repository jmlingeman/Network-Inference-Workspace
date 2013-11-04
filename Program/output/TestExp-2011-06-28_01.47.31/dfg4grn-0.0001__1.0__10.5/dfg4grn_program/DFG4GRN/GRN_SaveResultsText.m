% GRN_SaveResultsText  Save results into a text file
%
% Syntax:
%   GRN_SaveResultsText(params, filename, pNet)
% Inputs:
%   params:   parameters struct
%   filename: filename of text file where results will be stored
%   pNet:     matrix of size <N> x <M> of P-values

% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with this program; if not, write to the Free Software
% Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
%
% Version 1.0, New York, 29 March 2010
% (c) 2010, Piotr Mirowski,
%     Ph.D. candidate at the Courant Institute of Mathematical Sciences
%     Computer Science Department
%     New York University
%     719 Broadway, 12th Floor, New York, NY 10003, USA.
%     email: mirowski [AT] cs [DOT] nyu [DOT] edu

% Revision 1: replace z_sparsity by w_sparsity
%             add top 10 regulators and regulated genes
% Revision 2: monitor E-steps during learning (for choice of eta_z)
%             as well as M-steps and eta_w_optimal (eta_coeff)

function GRN_SaveResultsText(params, filename, pNet)

% Create a file <filename>.txt
if isequal(lower(filename((end-3):end)), '.mat')
  filename = filename(1:(end-4));
end
if ~isequal(lower(filename((end-3):end)), '.txt')
  filename = [filename '.txt'];
end
fid = fopen(filename, 'w');

% Print the parameters
PrintParam(fid, params, 'dynamic_algorithm', 's');
PrintParam(fid, params, 'dynamic_model', 's');
PrintParam(fid, params, 'dynamic_ar_order', 'd');
PrintParam(fid, params, 'pde_type', 's');
PrintParam(fid, params, 'dynamic_transfer', 's');
PrintParam(fid, params, 'tau', 'f');
PrintParam(fid, params, 'coeff_delta', 'f');
PrintParam(fid, params, 'observation_model', 's');
PrintParam(fid, params, 'eta_w', 'f');
PrintParam(fid, params, 'lambda_w', 'f');
PrintParam(fid, params, 'eta_w_conv', 'f');
PrintParam(fid, params, 'n_max_m_steps', 'd');
PrintParam(fid, params, 'use_conj_grad', 'd');
PrintParam(fid, params, 'eta_z', 'f');
PrintParam(fid, params, 'lambda_z', 'f');
PrintParam(fid, params, 'eta_z_conv', 'f');
PrintParam(fid, params, 'n_max_e_steps', 'd');
PrintParam(fid, params, 'gamma', 'f');

global METER_LEARN
global METER_INFER_TRAIN
global METER_INFER_TEST
n_models = length(METER_LEARN);

% Print the results of the meters for each trained model
for model_num = 1:(n_models+2)
  if (model_num <= n_models)
    METER_INFER_TRAIN{model_num}.meter_name = ...
      sprintf('train%d', model_num);
    METER_INFER_TEST{model_num}.meter_name = sprintf('test%d', model_num);
  elseif (model_num == (n_models+1))
    METER_INFER_TRAIN{model_num}.meter_name = ...
      sprintf('trainAv%d', model_num);
    METER_INFER_TEST{model_num}.meter_name = ...
      sprintf('testAv%d', model_num);
  else
    METER_INFER_TRAIN{model_num}.meter_name = ...
      sprintf('trainAvStat%d', model_num);
    METER_INFER_TEST{model_num}.meter_name = ...
      sprintf('testAvStat%d', model_num);
  end
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'target_auroc');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'target_aupr');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'target_Jaccard');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'target_F1');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'snr_observation');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'snr_dynamic');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'w_sparsity');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'error_trend_sign');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'error_trend_r2');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'error_trend_sign_all');
  PrintLast(fid, METER_INFER_TRAIN, model_num, 'error_trend_r2_all');
  PrintLast(fid, METER_INFER_TEST, model_num, 'target_auroc');
  PrintLast(fid, METER_INFER_TEST, model_num, 'target_aupr');
  PrintLast(fid, METER_INFER_TEST, model_num, 'target_Jaccard');
  PrintLast(fid, METER_INFER_TEST, model_num, 'target_F1');
  PrintLast(fid, METER_INFER_TEST, model_num, 'snr_observation');
  PrintLast(fid, METER_INFER_TEST, model_num, 'snr_dynamic');
  PrintLast(fid, METER_INFER_TEST, model_num, 'error_trend_sign');
  PrintLast(fid, METER_INFER_TEST, model_num, 'error_trend_r2');
  PrintLast(fid, METER_INFER_TEST, model_num, 'error_trend_sign_all');
  PrintLast(fid, METER_INFER_TEST, model_num, 'error_trend_r2_all');

  if (model_num <= n_models)
    if (params.gamma > 0)
      % Monitor number of E-steps
      n_e_steps = METER_LEARN{model_num}.n_e_steps;
      fprintf(fid, 'learn%d,min_num_e,%d\n', model_num, min(n_e_steps));
      fprintf(fid, 'learn%d,mean_num_e,%g\n', model_num, mean(n_e_steps));
      fprintf(fid, 'learn%d,max_num_e,%d\n', model_num, max(n_e_steps));
    end
    if isequal(params.dynamic_algorithm, 'gradient')
      % Monitor learning rate
      eta_coeff = METER_LEARN{model_num}.eta_coeff;
      eta_coeff = eta_coeff(~isnan(eta_coeff));
      fprintf(fid, 'learn%d,mean_opt_eta_w,%g\n', ...
        model_num, mean(eta_coeff));
      % Monitor number of M-steps
      n_m_steps = METER_LEARN{model_num}.n_m_steps;
      fprintf(fid, 'learn%d,min_num_m,%d\n', model_num, min(n_m_steps));
      fprintf(fid, 'learn%d,mean_num_m,%g\n', model_num, mean(n_m_steps));
      fprintf(fid, 'learn%d,max_num_m,%d\n', model_num, max(n_m_steps));
    end
  end

  % Top regulated/regulator genes
  [nIn, nOut, topIn, topOut, strIn, strOut] = GRN_TopGenes(pNet, params);
  fprintf(fid, 'model%d,topRegulated,%s\n', model_num, strIn);
  fprintf(fid, 'model%d,topRegulator,%s\n', model_num, strOut);
end

% Close the file
fclose(fid);


% -------------------------------------------------------------------------
function PrintParam(fid, params, name, type)

try
  val = eval(['params.' name ';']);
  switch type
    case 'd'
      fprintf(fid, 'params,%s,%d\n', name, val);
    case 'f'
      fprintf(fid, 'params,%s,%f\n', name, val);
    otherwise
      fprintf(fid, 'params,%s,%s\n', name, val);
  end
catch
end


% -------------------------------------------------------------------------
function PrintLast(fid, meter, model_num, name)

try
  model_name = meter{model_num}.meter_name;
  val = eval(['meter{model_num}.last_' name ';']);
  fprintf(fid, '%s,%s,%g\n', model_name, name, val);
catch
end

n_models=$1
output_dir=$2

#echo "addpath(genpath('./'));GRN_Batch_MultiModel_AR1({$n_models, 'dfg4grn_launch_script', '$output_dir','dfg4grn_output.mat', 'eta_z', $eta_z, 'tau', $tau, 'lambda', $lambda_w)"

matlab -nodesktop -nodisplay -nosplash -r "addpath(genpath('./'));try;GRN_Batch_MultiModel_AR1($n_models,'dfg4grn_launch_script','$output_dir','dfg4grn_output.mat'), catch, exit(1) ,end, exit(0)"

output_dir=$1
prediction_file=$2
new_weights_file=$3

matlab -nodesktop -nodisplay -nosplash -r "addpath(genpath('./'));GRN_EvaluatePrediction('','dfg4grn_launch_script','$output_dir','dfg4grn_prediction.mat', '$prediction_file', '$new_weights_file');"

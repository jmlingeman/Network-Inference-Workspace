clc;
clear all;

c = 0;

for k = [1 3 4 5 7 8 9 10 11 12]
    c = c + 1;
    
    clear A_init X U S N P A A_sparse conv_rate nl;
    
    file_name = strcat('GERSGORIN_GARDNER_40NOISE_',num2str(k));
    load(file_name);
    
    file_name = strcat('GERSGORIN_GARDNER_40NOISE_',num2str(c));
    save(file_name,'A_init','X','U','S','N','P','A','A_sparse','conv_rate','nl');
    
end
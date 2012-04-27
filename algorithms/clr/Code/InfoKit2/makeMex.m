function makeMex
%mi mex function
mex mi.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-fPIC -finline-functions -funroll-loops -ffast-math'

%kld mex function
mex kld.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-fPIC -finline-functions -funroll-loops -ffast-math'

%smooth histogram or set of histograms for a genes by experiments matrix
mex splineHistogram.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-finline-functions -funroll-loops -ffast-math -fPIC'

%gz importer
%THIS FAILS ON WINDOWS, AND I COULD NOT GET MATLAB TO LINK TO ZLIB!!!
%(NEED TO CHECK AGAINST THE LATEST MATLAB, THIS WAS TRUE FOR 7.1)
mex importGzFiles.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-fPIC -finline-functions -funroll-loops -ffast-math' -lz

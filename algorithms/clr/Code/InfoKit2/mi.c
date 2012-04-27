#include "mex.h"
#include "InfoKit2.h"

/* mex mi.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-finline-functions -funroll-loops -ffast-math' */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs,
const mxArray *prhs[])
{
  int numVars, numSamples, curVar, curSample;
  double *data;
  double *cData;
  float *fData;
  int isSingle = 0;
  int numBins = 10;
  int splineOrder = 3;
  
  /*
  if (nrhs != 1) {
	  mexPrintf("Need exactly one arguments\n");
	  return;
  } 
   */
  if (nrhs < 1) {
	  mexPrintf("mi(data, numBins[=10], splineOrder[=3]\n");
	  return;
  } 
  if (mxIsSingle(prhs[0])) {
	  isSingle = 1;
	  fData = (float*) mxGetData(prhs[0]);
  } else {
	  data = mxGetPr(prhs[0]);
  }
  if (nrhs >= 2) {
	  numBins = (int) mxGetScalar(prhs[1]);
  }
  if (nrhs >= 3) {
	  splineOrder = (int) mxGetScalar(prhs[2]);
  }
  numVars = mxGetM(prhs[0]);
  numSamples = mxGetN(prhs[0]);
  
  mexPrintf("Input data: %d variables and %d samples; number of bins: %d, spline order: %d\n", numVars, numSamples, numBins, splineOrder);
  
  cData = (double*) calloc(numVars * numSamples, sizeof(double));
  /* copy data & do a transpose */
  for (curVar = 0; curVar < numVars; curVar++) {
	  for (curSample = 0; curSample < numSamples; curSample++) {
		  if (isSingle) {
			  cData[curVar * numSamples + curSample] = (double) fData[curSample * numVars + curVar];
		  } else {
			  cData[curVar * numSamples + curSample] = (double) data[curSample * numVars + curVar];
		  }
	  }
  }

  plhs[0] = mxCreateNumericMatrix(numVars, numVars, mxSINGLE_CLASS, mxREAL);
  miSubMatrix(cData, (float*)mxGetPr(plhs[0]), numBins, numVars, numSamples, splineOrder, 0, numVars - 1);
  free(cData);
}



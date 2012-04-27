#include "mex.h"
#include "InfoKit2.h"

/* mex smoothKLD.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-fPIC -finline-functions -funroll-loops -ffast-math' */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs,
const mxArray *prhs[])
{
  int numVars, numSamples, curVar, curSampleP, curSampleQ;
  double *data1, *data2;
  double *pertIdx;
  double *dataP, *dataQ;
  int numBins = 10;
  int splineOrder = 3;
  int numPert;
  int i, found;
  
  /*
  if (nrhs != 1) {
	  mexPrintf("Need exactly one arguments\n");
	  return;
  } 
   */
  if (nrhs < 1) {
	  mexPrintf("KLD = kld(data, pertIdx, numBins[=10], splineOrder[=3])\n");
	  mexPrintf("\tor\n");
	  mexPrintf("KLD = kld(data1, data2, numBins[=10], splineOrder[=3])\n");
	  mexPrintf("\twhere data1 and data2 must be of the same size\n");
	  return;
  } 
  if (nrhs >= 3) {
	  numBins = (int) mxGetScalar(prhs[2]);
  }
  if (nrhs >= 4) {
	  splineOrder = (int) mxGetScalar(prhs[3]);
  }
  data1 = mxGetPr(prhs[0]);
  if (mxGetM(prhs[0]) == mxGetM(prhs[1]) && mxGetN(prhs[0]) == mxGetN(prhs[1])) {
	  pertIdx = (double*) NULL;
	  numPert = 0;
	  data2 = mxGetPr(prhs[1]);
  } else {
	  pertIdx = mxGetPr(prhs[1]);
	  numPert = mxGetN(prhs[1]);
	  data2 = (double*) NULL;
  }
  numVars = mxGetM(prhs[0]);
  numSamples = mxGetN(prhs[0]);
  mexPrintf("numPert: %d\n", numPert);
  mexPrintf("Input data: %d variables and %d samples; number of bins: %d, spline order: %d\n", numVars, numSamples, numBins, splineOrder);
  dataP = (double*) calloc(numVars * numSamples, sizeof(double));
  dataQ = (double*) calloc(numVars * (numSamples - numPert), sizeof(double));
  
  /* copy data & do a transpose */
  for (curVar = 0; curVar < numVars; curVar++) {
	  curSampleQ = 0;
	  for (curSampleP = 0; curSampleP < numSamples; curSampleP++) {
		  dataP[curVar * numSamples + curSampleP] = (double) data1[curSampleP * numVars + curVar];
		  if (numPert == 0) { /* A case of two-matrix input */
			  dataQ[curVar * numSamples + curSampleP] = (double) data2[curSampleP * numVars + curVar];
		  } else {
			  found = 0;
			  for (i = 0; i < numPert; i++)
				  if (pertIdx[i] == curSampleP)
					  found = 1;
			  
			  if (found == 0) {
				  dataQ[curVar * (numSamples - numPert)+ curSampleQ] = (double) data1[curSampleP * numVars + curVar];
				  curSampleQ += 1;
			  }
		  }
	  }
  }
  plhs[0] = mxCreateNumericMatrix(numVars, numVars, mxSINGLE_CLASS, mxREAL);
  kldSubMatrix(dataP, dataQ, (float*) mxGetPr(plhs[0]), numBins, numVars, numSamples, numSamples - numPert, splineOrder, 0, numVars - 1);

  free(dataP);
  free(dataQ);
}


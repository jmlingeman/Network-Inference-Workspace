#include "mex.h"
#include "InfoKit2.h"

/* mex smoothHist.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-finline-functions -funroll-loops -ffast-math -fPIC' */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs,
const mxArray *prhs[])
{
  int numVars, numSamples, curVar, curSample;
  double *data;
  double *cData;
  int *numBins;
  int splineOrder = 3;
  double *weights;
  double *knots;
  double *hist;
  float *fData;
  int fixedBinCount = 0;
  int isSingle = 0;
  mxArray *curHist;

  if (nrhs < 1) {
	  mexPrintf("splineHistogram(dataMatrix(Genes x Experiments format)[, binCount == -1, [splineOrder == 3]]\n");
	  mexPrintf("binCount == -1 means autodetect; spline order defaults to 3 but may be specified\n");
	  return;
  } else if (nrhs == 1) {
    fixedBinCount = 0;
  } else if (nrhs >= 2) {
    fixedBinCount = mxGetScalar(prhs[1]);
	if (fixedBinCount == -1)
		fixedBinCount = 0;
	
    if (nrhs >= 3) {
      splineOrder = mxGetScalar(prhs[2]);
    }
  }    
  numVars = mxGetM(prhs[0]);
  numSamples = mxGetN(prhs[0]);  

  if (mxIsSingle(prhs[0])) {
	  isSingle = 1;
	  fData = (float*) mxGetData(prhs[0]);
  } else {
	  data = mxGetPr(prhs[0]);
  }

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
  numBins = calcNumBins(cData, numVars, numSamples, 1);

  if (fixedBinCount) {
    for (curVar = 0; curVar < numVars; curVar++) {
      numBins[curVar] = fixedBinCount;
    }
  } else {
	  for (curVar = 0; curVar < numVars; curVar++) {
		  if (numBins[curVar] <= 1) /* No fewer than 2 bins allowed */
			  numBins[curVar] = 2;
	  }
  }
  plhs[0] = mxCreateCellMatrix(numVars, 1);

  for (curVar = 0; curVar < numVars; curVar++) {
    /* printf("Row %d: %d bins\n", curVar, numBins[curVar]); */
/*	  if (curVar == 0) {
		  mexPrintf("1: %lf, 2: %lf\n", cData[curVar * numSamples], cData[curVar * numSamples + 1]);
		  mexPrintf("numBins: %d\n", numBins[curVar]);
	  }
 */
    curHist = mxCreateDoubleMatrix(1, numBins[curVar], mxREAL);
    hist = mxGetPr(curHist);
    knots = (double*) calloc(numBins[curVar] + splineOrder, sizeof(double));
    knotVector(knots, numBins[curVar], splineOrder);
    weights = (double*) calloc(numSamples * numBins[curVar], sizeof(double));
    findWeights(cData + curVar*numSamples, knots, weights, numSamples, splineOrder, numBins[curVar], -1, -1);    
    hist1d(cData + curVar * numSamples, knots, hist, weights, numSamples, splineOrder, numBins[curVar]);
    free(knots);
    free(weights);
    mxSetCell(plhs[0], curVar, curHist);
  }
  free(cData);
}



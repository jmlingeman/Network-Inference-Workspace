#include "mex.h"
#include "InfoKit2.h"

/* mex jointDensity.c InfoKit2.c COPTIMFLAGS='-O3' CFLAGS='-finline-functions -funroll-loops -ffast-math -fPIC' */
void mexFunction(int nlhs, mxArray *plhs[], int nrhs,
const mxArray *prhs[])
{
	int numSamples, numBins, splineOrder;
	double *x, *y, *bounds;
	mxArray *histogram;
	double *knots, *weightsX, *weightsY;
	
	if (nrhs < 1) {
		mexPrintf("jointDensity(x, y, bounds, binCount, splineOrder)\n");
		mexPrintf("x, y are 1 by G samples of equal lengths, bounds is a bounds array of the form [minX, maxX, minY, maxY]\n");
		return;
	} else if (nrhs != 5) {
		mexPrintf("Must have exactly 5 arguments\n");
		mexPrintf("jointDensity(x, y, bounds, binCount, splineOrder)\n");
		mexPrintf("x, y are 1 by G samples of equal lengths, bounds is a bounds array of the form [minX, maxX, minY, maxY]\n");
		return;
	}
	numSamples = mxGetN(prhs[0]);
	x = mxGetPr(prhs[0]);
	y = mxGetPr(prhs[1]);

	bounds = mxGetPr(prhs[2]);
	numBins = (int)(*mxGetPr(prhs[3]));
	splineOrder = (int)(*mxGetPr(prhs[4]));
	histogram = mxCreateDoubleMatrix(numBins, numBins, mxREAL);
	
	knots = (double*) calloc(numBins + splineOrder, sizeof(double));
	knotVector(knots, numBins, splineOrder);
	weightsX = (double*) calloc(numSamples * numBins, sizeof(double));
	weightsY = (double*) calloc(numSamples * numBins, sizeof(double));

	if (mxGetN(prhs[2]) == 4) {
		findWeights(x, knots, weightsX, numSamples, splineOrder, numBins, bounds[0], bounds[1]);
		findWeights(y, knots, weightsY, numSamples, splineOrder, numBins, bounds[2], bounds[3]);
	
		hist2d(x, y, knots, weightsX, weightsY, mxGetPr(histogram), numSamples, splineOrder, numBins);		
	} else {
		findWeights(x, knots, weightsX, numSamples, splineOrder, numBins, -1, -1);
		findWeights(y, knots, weightsY, numSamples, splineOrder, numBins, -1, -1);
	
		hist2d(x, y, knots, weightsX, weightsY, mxGetPr(histogram), numSamples, splineOrder, numBins);
	}
	
	free(weightsX);
	free(weightsY);
	free(knots);
	plhs[0] = histogram;
}



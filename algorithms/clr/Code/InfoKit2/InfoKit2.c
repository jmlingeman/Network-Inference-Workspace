#include "InfoKit2.h"

/* On some machines, log2 system macro appears to be broken - write our own! */

float simpson(float a, float b, int m, float (*f) (float, float*), float *params) {
	float *evalResult, point, h, s0, s1, s2, J;
	int i;
	
	h = (b - a)/(2*m);

	evalResult = (float*) calloc((2*m + 1), sizeof(float));
	for (i = 0; i <= 2*m; i++) {
		point = a + i*h;
		evalResult[i] = f(point, params);
	}
	s0 = evalResult[0] + evalResult[2*m];
	s1 = 0;
	for (i = 1; i < 2*m; i += 2) {
		s1 += evalResult[i];
	}
	s2 = 0;
	for (i = 2; i < 2*m; i += 2) {
		s2 += evalResult[i];
	}

	free(evalResult);
	J = h/3*(s0 + 4*s1 + 2*s2);
	return J;
}

float fdr(const float *p, int numElem, float alpha, int dependence) {
  float *tmp = (float*)calloc(numElem, sizeof(float));
  float val;
  int i, rightmost;
  float Cm;
  
  for (i = 0; i < numElem; i++) {
    tmp[i] = p[i];
  }
  
  qsort(tmp, numElem, sizeof(float), (void*) compare_floats);
  
  if (dependence == INDEPENDENT || dependence == POSITIVE) {
    Cm = 1;
  } else { /* NEGATIVE */
    Cm = log(numElem) + EULER_MASCHERONI;
  }
  rightmost = -1;
  for (i = 0; i < numElem; i++) {
    if (tmp[i] < i/(numElem * Cm)*alpha)
      rightmost = i;
  }
  
  if (rightmost < 0) {
	  val = rightmost;
  } else {
	  val = tmp[rightmost];
  }
  free(tmp);
  return(val);
}

float normPdf (float x, float *params) {
  float mu = params[0];
  float sigma = params[1];
  float ePower = -(x - mu)*(x - mu)/(2*sigma*sigma);
  float pdf = 1/(sigma*sqrt(2*PI))*exp(ePower);
  
  return pdf;
}

float normCdf(float val, float mu, float sigma) {
	float result;
	float params[2];
	params[0] = mu;
	params[1] = sigma;
	result = simpson(-20, val, (int)(NUM_INTEGRAL_PTS/2), &normPdf, params);
	return result;
}


float zToP(float z, int tail) {
  float p;
  
  if (tail == POSITIVE) { /* positive z is significant */
    p = 1 - normCdf(z, 0, 1);
  } else if (tail == NEGATIVE) { /* negative z is significant */
    p = normCdf(z, 0, 1);
  } else { /* absolute value is significant */
    p = normCdf(z, 0, 1);
    if (p > 0.5)
      p = 2*(1-p);
    else
      p = 2*p;
  }
  return p;
}

float log2f(float x) {
  return log(x)/log(2);
}

double log2d(double x) {
  return log(x)/log(2);
}

int compare_integers (const int *a, const int *b) {
  if (*a < *b)
    return -1;
  else if (*a > *b)
    return 1;
	
  return 0;
}

int compare_floats (const float *a, const float *b) {
  if (*a < *b)
    return -1;
  else if (*a > *b)
    return 1;
	
  return 0;
}

int compare_doubles (const double *a, const double *b) {
  if (*a < *b)
    return -1;
  else if (*a > *b)
    return 1;
	
  return 0;
}

double mean(double *data, int numSamples) {
  int curSample;
  double mean = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    mean += data[curSample];
  }
  return mean / (double) numSamples;
}

double std(double *data, int numSamples) {
  int curSample;
  double m = mean(data, numSamples);
  double std = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    std += (data[curSample] - m)*(data[curSample] - m);
  }
  return sqrt(1/(double)(numSamples - 1) * std);
}

float meanf(float *data, int numSamples) {
  int curSample;
  float mean = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    mean += data[curSample];
  }
  return mean / (float) numSamples;
}

float stdf(float *data, int numSamples) {
  int curSample;
  float m = meanf(data, numSamples);
  float std = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    std += (data[curSample] - m)*(data[curSample] - m);
  }
  return sqrt(1/(float)(numSamples - 1) * std);
}

double meani(int *data, int numSamples) {
  int curSample;
  double mean = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    mean += (double)data[curSample];
  }
  return mean / (double) numSamples;
}

double mediani(int *data, int numElem) {
  int *dataCopy = (int*) malloc(numElem * sizeof(int));
  int half = floor(numElem/2);
  int i;
  double median;

  for (i = 0; i < numElem; i++) {
    dataCopy[i] = data[i];
  }
  /* sort */
  qsort(dataCopy, numElem, sizeof(int), (void*) compare_integers);
  
  median = ((double)dataCopy[half] + (double)dataCopy[half + 1])/2.0;
  free(dataCopy);
  return median;
}

double stdi(int *data, int numSamples) {
  int curSample;
  double m = meani(data, numSamples);
  double std = 0;
	
  for (curSample = 0; curSample < numSamples; curSample++) {
    std += (double) (data[curSample] - m)*(data[curSample] - m);
  }
  return sqrt(1/(double)(numSamples - 1) * std);
}

/* Accepts sorted input */
double iqr(double *data, int numSamples) {
  double q1, q3;
  int idx;
  numSamples = numSamples - 1; /* convert into indices */
	
  if ((numSamples + 1) % 2 == 0) {
    if ((numSamples + 1) % 4 == 0) {
      q1 = (data[numSamples/4] + data[numSamples/4 + 1])/2;
      q3 = (data[numSamples*3/4] + data[numSamples*3/4 + 1])/2;
    } else {
      q1 = data[(int)ceil(numSamples/4)];
      q3 = data[(int)ceil(numSamples*3/4)];
    }
  } else {
    idx = (int)ceil(numSamples/4);
    q1 = (data[idx] + data[idx + 1])/2;
    idx = (int)ceil(numSamples*3/4);
    q3 = (data[idx] + data[idx + 1])/2;
  }

  return q3 - q1;
}

void clrUnweightedStouffer(float *miMatrix, float *clrMatrix, int numVars) {
  float *m = (float*) calloc(numVars, sizeof(float));
  float *s = (float*) calloc(numVars, sizeof(float));
  float *curMI = miMatrix;
  int i, j, k, diag = 0;
  float a, b;
	
  for (i = 0; i < numVars; i++) {
    if (miMatrix[i + i*numVars] != 0) {
      diag = 1;
      /* copy matrix to one w/o diag and break */
      curMI = (float*) calloc(numVars * numVars, sizeof(float));
      for (j = 0; j < numVars; j++) {
	for (k = 0; k < numVars; k++) {
	  if (j == k)
	    continue;
		
	  curMI[j + k * numVars] = miMatrix[j + k * numVars];
	}
      }
      break;
    }
  }
  for (i = 0; i < numVars; i++) {
    m[i] = meanf(curMI + numVars * i, numVars);
    s[i] = stdf(curMI + numVars * i, numVars);
  }
  for (i = 0; i < numVars - 1; i++) {
    for (j = i + 1; j < numVars; j++) {
      a = (curMI[i + numVars * j] - m[i])/s[i];
      b = (curMI[i + numVars * j] - m[j])/s[j];
      /* printf("a; %f, b: %f\n", a, b); */
      clrMatrix[i + numVars * j] = (a + b)/sqrt(2);
      clrMatrix[j + numVars * i] = clrMatrix[i + numVars * j];
    }
  }
  free(m);
  free(s);
  if (diag) {
    free(curMI);
  }
}

void clrGauss(float *miMatrix, float *clrMatrix, int numVars) {
  float *m = (float*) calloc(numVars, sizeof(float));
  float *s = (float*) calloc(numVars, sizeof(float));
  float *curMI = miMatrix;
  int i, j, k, diag = 0;
  float a, b;
	
  for (i = 0; i < numVars; i++) {
    if (miMatrix[i + i*numVars] != 0) {
      diag = 1;
      /* copy matrix to one w/o diag and break */
      curMI = (float*) calloc(numVars * numVars, sizeof(float));
      for (j = 0; j < numVars; j++) {
	for (k = 0; k < numVars; k++) {
	  if (j == k)
	    continue;
		
	  curMI[j + k * numVars] = miMatrix[j + k * numVars];
	}
      }
      break;
    }
  }
  for (i = 0; i < numVars; i++) {
    m[i] = meanf(curMI + numVars * i, numVars);
    s[i] = stdf(curMI + numVars * i, numVars);
  }
  for (i = 0; i < numVars - 1; i++) {
    for (j = i + 1; j < numVars; j++) {
      a = (curMI[i + numVars * j] - m[i])/s[i];
      b = (curMI[i + numVars * j] - m[j])/s[j];
      /* printf("a; %f, b: %f\n", a, b); */
      if (a < 0)
	a = 0;
      if (b < 0)
	b = 0;
      clrMatrix[i + numVars * j] = sqrt(a*a + b*b);
      clrMatrix[j + numVars * i] = clrMatrix[i + numVars * j];
    }
  }
  free(m);
  free(s);
  if (diag) {
    free(curMI);
  }
}

/* zero-stage rule, see e.g. Wand M.P., Data-Based Choice of Histogram Bin Width */
double binWidth(double *data, int numSamples) {
  double s = std(data, numSamples);
  double i = iqr(data, numSamples) / 1.349;
  double shat = s < i ? s : i;
  return 3.49*shat*(double)pow((double)numSamples, (double)-1/3);
}

/* Operates on a matrix of data */
int *calcNumBins(double *data, int numVars, int numSamples, double binMultiplier) {
  int *binCount = calloc(numVars, sizeof(int));
  int curVar, curSample;
  double *sData = calloc(numVars * numSamples, sizeof(double)); /* for sorted data */
	
  for (curVar = 0; curVar < numVars; curVar++)
    for (curSample = 0; curSample < numSamples; curSample++)
      sData[curVar * numSamples + curSample] = data[curVar * numSamples + curSample];
	
	
  for (curVar = 0; curVar < numVars; curVar++) {
    qsort(sData + curVar * numSamples, numSamples, sizeof(double), (void*) compare_doubles);
    binCount[curVar] = (int) ceil((sData[curVar * numSamples + numSamples - 1] - sData[curVar * numSamples])/binWidth(sData + curVar * numSamples, numSamples)*binMultiplier);
  }
  free(sData);
  return binCount;
}

/* Follows Daub et al, which contains mistakes; corrections based on spline descriptions on MathWorld pages */
double basisFunction(int i, int p, double t, const double *kVector, int numBins) {
  double d1, n1, d2, n2, e1, e2;
  if (p == 1) {
    if ((t >= kVector[i] && t < kVector[i+1] && 
	 kVector[i] < kVector[i+1]) ||
	(fabs(t - kVector[i+1]) < 1e-10 && (i+1 == numBins))) {
      return(1);
    }
    return(0);
  }
    
  d1 = kVector[i+p-1] - kVector[i];
  n1 = t - kVector[i];
  d2 = kVector[i+p] - kVector[i+1];
  n2 = kVector[i+p] - t;

  if (d1 < 1e-10 && d2 < 1e-10) {
    return(0);
  } else if (d1 < 1e-10) {
    e1 = 0;
    e2 = n2/d2*basisFunction(i+1, p-1, t, kVector, numBins);
  } else if (d2 < 1e-10) {
    e2 = 0;
    e1 = n1/d1*basisFunction(i, p-1, t, kVector, numBins);
  } else {
    e1 = n1/d1*basisFunction(i, p-1, t, kVector, numBins);
    e2 = n2/d2*basisFunction(i+1, p-1, t, kVector, numBins);
  }    
  
  /* sometimes, this value is < 0 (only just; rounding error); truncate */
  if (e1 + e2 < 0) {
    return(0);
  }
  return(e1 + e2);

}

void knotVector(double *v, int numBins, int splineOrder) {
  int nInternalPoints = numBins - splineOrder;
  int i;

  for (i = 0; i < splineOrder; ++i) {
    v[i] = 0;
  }
  for (i = splineOrder; i < splineOrder + nInternalPoints; ++i) {
    v[i] = (double)(i - splineOrder + 1)/(nInternalPoints + 1);
  }
  for (i = splineOrder + nInternalPoints; i < 2*splineOrder + nInternalPoints; ++i) {
    v[i] = 1;
  }
}

double max(const double *data, int numSamples) {
  int curSample;
  double curMax = data[0];
	
  for (curSample = 1; curSample < numSamples; curSample++) {
    if (data[curSample] > curMax) {
      curMax = data[curSample];
    }
  }
  return curMax;
}

double min(const double *data, int numSamples) {
  int curSample;
  double curMin = data[0];
	
  for (curSample = 1; curSample < numSamples; curSample++) {
    if (data[curSample] < curMin) {
      curMin = data[curSample];
    }
  }
  return curMin;
}

int maxi(const int *data, int numSamples) {
  int curSample;
  int curMax = data[0];
	
  for (curSample = 1; curSample < numSamples; curSample++) {
    if (data[curSample] > curMax) {
      curMax = data[curSample];
    }
  }
  return curMax;
}

int mini(const int *data, int numSamples) {
  int curSample;
  int curMin = data[0];
	
  for (curSample = 1; curSample < numSamples; curSample++) {
    if (data[curSample] < curMin) {
      curMin = data[curSample];
    }
  }
  return curMin;
}

void xToZ(const double *fromData, double *toData, int numSamples, int splineOrder, int numBins, double xMin, double xMax) {
  int curSample;
  
  if (xMin == -1 && xMax == -1) { /*then compute on the fly */
	  xMin = min(fromData, numSamples);
	  xMax = max(fromData, numSamples);
  } /*else use provided values */
  
  for (curSample = 0; curSample < numSamples; curSample++) {
    /* toData[curSample] = (fromData[curSample] - xMin) * (numBins - splineOrder + 1) / (double) (xMax - xMin); */
    /* normalize to [0, 1] */
    toData[curSample] = (fromData[curSample] - xMin) / (double) (xMax - xMin);
  }
}

void findWeights(const double *x, const double *knots, double *weights, int numSamples, int splineOrder, int numBins, double rangeLeft, double rangeRight) {
  int curSample;
  int curBin;
  double *z = (double*) calloc(numSamples, sizeof(double));

  xToZ(x, z, numSamples, splineOrder, numBins, rangeLeft, rangeRight);

  for (curSample = 0; curSample < numSamples; curSample++) {
    for (curBin = 0; curBin < numBins; curBin++) {
      weights[curBin * numSamples + curSample] = basisFunction(curBin, splineOrder, z[curSample], knots, numBins);
      /* mexPrintf("%d|%f(%f)\t", curBin, weights[curBin * numSamples + curSample],z[curSample]); */
    }
  }
  free(z);
}

void hist1d(const double *x, const double *knots, double *hist, double *w, int numSamples, int splineOrder, int numBins) {
  int curSample;
  int curBin;
	
  for (curBin = 0; curBin < numBins; curBin++) {
    for (curSample = 0; curSample < numSamples; curSample++) {
      hist[curBin] += w[curBin * numSamples + curSample]/(double)numSamples;
    }
  }
}

void hist2d(const double *x, const double *y, const double *knots, const double *wx, const double *wy, double *hist, int numSamples, int splineOrder, int numBins) {
  int curSample;
  int curBinX, curBinY;
  double sum = 0;
	
  for (curBinX = 0; curBinX < numBins; curBinX++) {
    for (curBinY = 0; curBinY < numBins; curBinY++) {
      for (curSample = 0; curSample < numSamples; curSample++) {
	hist[curBinX * numBins + curBinY] += wx[curBinX * numSamples + curSample] * wy[curBinY * numSamples + curSample]/numSamples;
      }
    }
  }
	
  /*
    for (curBinX = 0; curBinX < numBinsX; curBinX++) {
    for (curBinY = 0; curBinY < numBinsY; curBinY++) {
    mexPrintf("%f\t", hist[curBinX * numBinsY + curBinY]);
    }
    mexPrintf("\n");
    }
  */
}

double entropy1d(const double *x, const double *knots, double *weights, int numSamples, int splineOrder, int numBins) {
  int curBin;
  double *hist = (double*) calloc(numBins, sizeof(double));
  double H = 0;
	
  hist1d(x, knots, hist, weights, numSamples, splineOrder, numBins);
  for (curBin = 0; curBin < numBins; curBin++) {
    if (hist[curBin] > 0) {
      H -= hist[curBin] * log2d(hist[curBin]);
    }
  }
  free(hist);
  return H;
}

double entropy2d(const double *x, const double *y, const double *knots, const double *wx, const double *wy, int numSamples, int splineOrder, int numBins) {
  int curBinX, curBinY;
  double *hist = (double*) calloc(numBins * numBins, sizeof(double));
  double H = 0;
  double incr;

  hist2d(x, y, knots, wx, wy, hist, numSamples, splineOrder, numBins);
  for (curBinX = 0; curBinX < numBins; curBinX++) {
    for (curBinY = 0; curBinY < numBins; curBinY++) {
      incr = hist[curBinX * numBins + curBinY];
      if (incr > 0) {
	H -= incr * log2d(incr);
      }
    }
  }
  free(hist);
  return H;
}

double kld2d(const double *xP, const double *yP, const double *xQ, \
	     const double *yQ, const double *knots, const double *wxP, const double *wyP,\
	     const double *wxQ, const double *wyQ, int numSamplesP, int numSamplesQ,\
	     int splineOrder, int numBins) {
  int curBinX, curBinY;
  double *histP = (double*) calloc(numBins * numBins, sizeof(double));
  double *histQ = (double*) calloc(numBins * numBins, sizeof(double));
  double KLD = 0;
	
  hist2d(xP, yP, knots, wxP, wyP, histP, numSamplesP, splineOrder, numBins);
  hist2d(xQ, yQ, knots, wxQ, wyQ, histQ, numSamplesQ, splineOrder, numBins);
  for (curBinX = 0; curBinX < numBins; curBinX++) {
    for (curBinY = 0; curBinY < numBins; curBinY++) {
      if(histP[curBinX * numBins + curBinY] > 0
	 && histQ[curBinX * numBins + curBinY] > 0) {
	KLD += histP[curBinX * numBins + curBinY] * log2d(histP[curBinX * numBins + curBinY]/histQ[curBinX * numBins + curBinY]);
      }
    }
  }
  free(histP);
  free(histQ);
  return KLD;
}

/* Compute mutual information for lower triangular matrix from col fromCol to col toCol */
void miSubMatrix(const double *data, float *miMatrix, int numBins, int numVars, int numSamples, int splineOrder, int fromCol, int toCol) {
  int col, row, i;
	
  double *knots = (double*) calloc(numBins + splineOrder, sizeof(double));
  double *entropies = calloc(numVars - fromCol, sizeof(double));
  double *weights = (double*)calloc((numVars - fromCol)*numSamples*numBins, sizeof(double));
  double *hist2 = (double*) calloc(numBins * numBins, sizeof(double));
  double *hist1 = (double*) calloc(numBins, sizeof(double));

  knotVector(knots, numBins, splineOrder);

  for (i = 0; i < numVars - fromCol; i++) {
    /* allocate room for marginal weights */
    findWeights(data + (i + fromCol)*numSamples, knots, weights + i*numSamples*numBins, numSamples, splineOrder, numBins, -1, -1);
    entropies[i] = entropy1d(data + (i + fromCol)*numSamples, knots, weights + i*numSamples*numBins, numSamples, splineOrder, numBins);
  }

  for (col = fromCol; col <= toCol; col++) {
    for (row = col; row < numVars; row++) {
      miMatrix[(col - fromCol) * numVars + row] = (float) entropies[col - fromCol] + (float) entropies[row - fromCol] - \
	(float) entropy2d(data + col*numSamples, data + row*numSamples, knots, weights + (col - fromCol)*numSamples*numBins, weights + (row - fromCol)*numSamples*numBins, numSamples, splineOrder, numBins);
      /* Make matrix symmetric around diagonal if not in cluster mode */
      if (toCol - fromCol + 1 == numVars)
	miMatrix[(row - fromCol) * numVars + col] = miMatrix[(col - fromCol) * numVars + row];
    }
  }

  free(entropies);
  free(knots);
  free(weights);
  free(hist1);
  free(hist2);
}



/* Compute Kullback-Leibler Divergence for lower triangular matrix from col fromCol to col toCol */
/* It is the expectation here that indices to be withheld are specified, and used to compute
 * KLD(all samples | all - withheld samples) */
void kldSubMatrix(const double *dataP, const double *dataQ, float *kldMatrix, int numBins, int numVars, int numSamplesP, int numSamplesQ, int splineOrder, int fromCol, int toCol) {
  int col, row, i;
	
  double *knots = (double*) calloc(numBins + splineOrder, sizeof(double));
  double *weightsP = (double*)calloc((numVars - fromCol)*numSamplesP*numBins, sizeof(double));
  double *weightsQ = (double*)calloc((numVars - fromCol)*numSamplesQ*numBins, sizeof(double));
  double *histP = (double*) calloc(numBins * numBins, sizeof(double));
  double *histQ = (double*) calloc(numBins * numBins, sizeof(double));
  double leftP, leftQ, rightP, rightQ, left, right;
  
  knotVector(knots, numBins, splineOrder);
  
  for (i = 0; i < numVars - fromCol; i++) {
	  leftP = min(dataP + (i + fromCol) * numSamplesP, numSamplesP);
	  leftQ = min(dataQ + (i + fromCol) * numSamplesQ, numSamplesQ);	  
	  rightP = max(dataP + (i + fromCol) * numSamplesP, numSamplesP);
	  rightQ = max(dataQ + (i + fromCol) * numSamplesQ, numSamplesQ);	  
	  left = (leftP < leftQ) ? leftP : leftQ;
	  right = (rightP > rightQ) ? rightP : rightQ;
	  
	  findWeights(dataP + (i + fromCol)*numSamplesP, knots, weightsP + i*numSamplesP*numBins, numSamplesP, splineOrder, numBins, left, right);
	  findWeights(dataQ + (i + fromCol)*numSamplesQ, knots, weightsQ + i*numSamplesQ*numBins, numSamplesQ, splineOrder, numBins, left, right);
  }

  /* The loop below assumes that the calling process is NOT multitreading and that indexing begins with 0
     in the calling process REGARDLESS OF WHICH COLUMNS ARE BEING WORKED ON!!!! */
  for (col = fromCol; col <= toCol; col++) {
    for (row = col; row < numVars; row++) {
      kldMatrix[(col - fromCol) * numVars + row] = (float)
	kld2d(dataP + col*numSamplesP, dataP + row*numSamplesP,
	      dataQ + col*numSamplesQ, dataQ + row*numSamplesQ, knots, 
	      weightsP + (col - fromCol)*numSamplesP*numBins, 
	      weightsP + (row - fromCol)*numSamplesP*numBins, 
	      weightsQ + (col - fromCol)*numSamplesQ*numBins, 
	      weightsQ + (row - fromCol)*numSamplesQ*numBins, 
	      numSamplesP, numSamplesQ, splineOrder, numBins);
      /* Make matrix symmetric around diagonal if not in cluster mode */
      if (toCol - fromCol + 1 == numVars)
	kldMatrix[(row - fromCol) * numVars + col] = kldMatrix[(col - fromCol) * numVars + row];
    }
  }

  free(knots);
  free(weightsP);
  free(weightsQ);
  free(histP);
  free(histQ);
}

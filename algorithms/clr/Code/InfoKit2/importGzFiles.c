#include "InfoKit2.h"
#include <string.h>
#include "zlib.h"
#include "mex.h"
#include "matrix.h"

/* Build using
   mex importGzFiles.c OPTIMFLAGS='-O3' CFLAGS='-fPIC -finline-functions -ffast-math -funroll-loops -lz'
*/

void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[]) {
  int numVars, curFileNum, i, j, numBytes, numFiles, colStart, colEnd;
  float *BUF, *BUFtmp;
  gzFile *in;
  mxArray *curFname;
  char *cFname;
  char *cFnameCopy;
  char *tok;

  if (nrhs < 1) {
    mexPrintf("Need to supply the input filename!\n");
    return;
  }
  numVars = mxGetScalar(prhs[0]);
  BUFtmp = (float*) calloc(numVars * numVars, sizeof(float));
  plhs[0] = mxCreateNumericMatrix(numVars, numVars, mxSINGLE_CLASS, mxREAL);
  BUF = (float*) mxGetPr(plhs[0]);

  /* mxArray *mxGetCell(const mxArray *array_ptr, int index); */
  /* int mxGetString(const mxArray *array_ptr, char *buf, int buflen); */
  /* mxArray *mxGetField(const mxArray *array_ptr, int index, 
     const char *field_name); */

  numFiles = mxGetM(prhs[1]);
  for (curFileNum = 0; curFileNum < numFiles; curFileNum++) {
    curFname = mxGetField(prhs[1], curFileNum, "name");
    numBytes = mxGetN(curFname) + 1;
    cFname = (char*) calloc(numBytes, sizeof(char));
    cFnameCopy = (char*) calloc(numBytes, sizeof(char));
    mxGetString(curFname, cFname, numBytes);
    mxGetString(curFname, cFnameCopy, numBytes);
    mexPrintf("reading %s...\t", cFname);
    
    strtok(cFnameCopy, "_"); /* skip prefix */
    strtok(NULL, "_"); /* skip numGenes */
    tok = strtok(NULL, "_"); /* this is colStart */
    colStart = atoi(tok);
    tok = strtok(NULL, "_"); /* this is colEnd */
    colEnd = atoi(tok);

    in = gzopen(cFname, "rb");
    if (in == NULL) {
      printf("could not open %s\n", cFname);
      return;
    } else if (in == 0) {
      printf("EOF\n");
      return;
    }

    numBytes = gzread(in, BUFtmp, numVars * (colEnd - colStart + 1) * sizeof(float));
    printf("%d bytes\n", numBytes);
    for (i = colStart; i <= colEnd; i++) {
      for (j = 0; j < numVars; j++) {
	BUF[i * numVars + j] += BUFtmp[(i - colStart) * numVars + j];
	BUF[j * numVars + i] = BUF[i * numVars + j];
      }
    }
    gzclose(in);
    free(cFname);
    free(cFnameCopy);
  }

  free(BUFtmp);
}

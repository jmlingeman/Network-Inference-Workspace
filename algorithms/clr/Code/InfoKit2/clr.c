#include "InfoKit2.h"
#include "FileUtil.h"
#include <getopt.h>

typedef struct {
  char *dataFile;
  char *tfIdxFile;
  char *mapFile;
  int numBins;
  int splineDegree;
} ARGUMENTS;

ARGUMENTS parseOptions (int argc, char *argv[]){
  int c;
  ARGUMENTS args;
	
  /* set defaults */
  args.dataFile = (char*) NULL;
  args.tfIdxFile = (char*) NULL;
  args.mapFile = (char*) malloc((strlen("out.csv") + 1)*sizeof(char));
  sprintf(args.mapFile, "out.csv");
  args.numBins = -1;
  args.splineDegree = 3;

  while (1) {
    static struct option long_options[] =
      {
	/* These options don't set a flag.
	   We distinguish them by their indices. */
	{"data",      required_argument, 0, 'd'},
	{"tfidx",     required_argument, 0, 't'},
	{"map",       required_argument, 0, 'm'},
	{"bins",      required_argument, 0, 'b'},
	{"spline",    required_argument, 0, 's'}
      };
    /* getopt_long stores the option index here. */
    int option_index = 0;
		
    c = getopt_long_only (argc, argv, "d:t:m:b:s:",
			  long_options, &option_index);

    /* Detect the end of the options. */
    if (c == -1)
      break;

    switch (c)
      {
      case 'd':
	/* printf ("option -d with value `%s'\n", optarg); */
	args.dataFile = (char*) malloc((strlen(optarg) + 1)*sizeof(char));
	strcpy(args.dataFile, optarg);
	break;
      case 't':
	/* printf ("option -t with value `%s'\n", optarg); */
	args.tfIdxFile = (char*) malloc((strlen(optarg) + 1)*sizeof(char));
	strcpy(args.tfIdxFile, optarg);
	break;
      case 'm':
	/* printf ("option -m with value `%s'\n", optarg); */
	args.mapFile = (char*) malloc((strlen(optarg) + 1)*sizeof(char));
	strcpy(args.mapFile, optarg);
	break;
      case 'b':
	args.numBins = atoi(optarg);
	break;
      case 's':
	/* printf ("option -s with value `%s'\n", optarg); */
	args.splineDegree = atoi(optarg);
	break;
      case '?':
	/* getopt_long already printed an error message. */
	printf("Usage: clr --data input_file_name [--tfidx tfidx_file_name --map output_file_name --bins num_bins --spline spline_degree]\n");
	exit(0);
	break;
				
      default:
	printf("aborting!\n");
	exit(0);
      }
  }
  /* Print any remaining command line arguments (not options). */
  if (optind < argc) {
    printf ("Unrecognized arguments: \n");
    while (optind < argc)
      printf ("%s ", argv[optind++]);
    putchar ('\n');
  }
  
  if (args.dataFile == (char*) NULL) {
    printf("Usage: clr --data input_file_name [--tfidx tfidx_file_name --map output_file_name --bins num_bins --spline spline_degree]\n");
    exit(0);
  }
  return args;
}

int main(int argc, char *argv[]) {
  int i, j, numVars, numSamples, numBins, splineOrder, *tfIdx, numTfs;
  double *X, *tmpTFIdx;
  float *MI, *C;
  int *binCount;
  FILE *out;
  char type;
  double stdBinCount;
  int c;
  ARGUMENTS args = parseOptions(argc, argv);
  
  ReadFile(args.dataFile, &X, &numVars, &numSamples);
  if (args.tfIdxFile != (char*) NULL)
    ReadFile(args.tfIdxFile, &tmpTFIdx, &numTfs, &j);
  else { /* default mode: every gene is a TF */
    numTfs = numVars;
    tmpTFIdx = (double*) calloc(numTfs, sizeof(double));
    for (i = 0; i < numTfs; i++) {
      tmpTFIdx[i] = i + 1;
    }
  }
  /* Assume TF list only has one column or row (it had better!) */
  if (numTfs == 1)
    numTfs = j;
  tfIdx = calloc(numTfs, sizeof(int));
  for (i = 0; i < numTfs; i++) {
    tfIdx[i] = (int) tmpTFIdx[i] - 1; /* Convert into C notation: 0 to n-1 */
  }

  printf("Detected %d genes and %d datapoints in the input file\n", numVars, numSamples);

  binCount = calcNumBins(X, numVars, numSamples, 1);
  if (args.numBins == -1) {
    args.numBins = (int) floor(mediani(binCount, numVars));
    stdBinCount = stdi(binCount, numVars);
    printf("Bin count not supplied.  Autodetected that dataset warrants %d bins (median); stddev == %f\n", args.numBins, stdBinCount);
    if (args.numBins > 15)
      printf("Warning: this automatic bin count (%d) may be a bit slow on large datasets, and may warrant a spline degree above 3\n", args.numBins);
    if (args.numBins < 2) {
      printf("Too few bins (%d)!\n", args.numBins);
      exit(0);
    }
      
  }
  MI = (float*) calloc(numVars*numVars, sizeof(float));

  miSubMatrix(X, MI, args.numBins, numVars, numSamples, args.splineDegree, 0, numVars - 1);

  C = (float*) calloc(numVars*numVars, sizeof(float));
  /* clrGauss(MI, C, numVars); */
  clrUnweightedStouffer(MI, C, numVars);
  free(MI);

  /* 
     int fwrite( const void *buffer, size_t size, size_t count, FILE *stream );
     int gzwrite (gzFile file, const voidp buf, unsigned len);
  */

  out = fopen(args.mapFile, "w");

  for (i = 0; i < numVars; i++) {
    for (j = 0; j < numTfs; j++) { 
      fprintf(out, "%f", C[tfIdx[j] * numVars + i]);
      if (j < numTfs - 1) {
	fprintf(out, ",");
      }
    }
    fprintf(out, "\n");
  }
  fclose(out);

  free(tmpTFIdx);
  free(tfIdx);
  free(binCount);
  free(X);
  free(C);
  free(args.dataFile);
  if (args.tfIdxFile != (char*) NULL)
    free(args.tfIdxFile);
  if (args.mapFile != (char*) NULL)
    free(args.mapFile);
}





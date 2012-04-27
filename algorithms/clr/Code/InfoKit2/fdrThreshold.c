#include "InfoKit2.h"
#include "FileUtil.h"
#include <getopt.h>

typedef struct {
  char *dataFile;
  char *outFile;
  float fdr;
  int corrType;
} ARGUMENTS;

ARGUMENTS parseOptions (int argc, char *argv[]){
  int c;
  ARGUMENTS args;
	
  /* set defaults */
  args.corrType = NEGATIVE;
  args.fdr = 0.05;
  args.dataFile = (char*) NULL;
  args.outFile = (char*) NULL;

  while (1) {
    static struct option long_options[] =
      {
	/* These options don't set a flag.
	   We distinguish them by their indices. */
	{"data",  required_argument, 0, 'd'},
	{"fdr", required_argument, 0, 'f'},
	{"output_file", required_argument, 0, 'o'},
	{"corr_type", required_argument, 0, 'c'},
      };
    /* getopt_long stores the option index here. */
    int option_index = 0;
    
    c = getopt_long_only (argc, argv, "d:f:o:c:",
			  long_options, &option_index);

    /* Detect the end of the options. */
    if (c == -1)
      break;

    switch (c)
      {
      case 'd':
	args.dataFile = (char*) malloc((strlen(optarg) + 1)*sizeof(char));
	strcpy(args.dataFile, optarg);
	break;
      case 'f':
	args.fdr = (float)atof(optarg);
	break;
      case 'o':	
	args.outFile = (char*) malloc((strlen(optarg) + 1)*sizeof(char));
	strcpy(args.outFile, optarg);
	break;
      case 'c':
	args.corrType = (int)atoi(optarg);
	switch (args.corrType) {
	case INDEPENDENT:
	case NEGATIVE:
	case POSITIVE:
	  break;
	default:
	  args.corrType = INDEPENDENT;
	  printf("Warning: invalid correlation type; defaulting to 0 (independent)\n");
	  break;
	}
	break;
      case '?':
	/* getopt_long already printed an error message. */
	printf("Usage: fdrThreshold --data input_file_name [--output_file output_file_name --fdr {0..1} --corr_type {1, 0, -1}]\n\tDefaults: fdr == 0.05, corr_type == 0\n");
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
  /* run some checks */
  if (args.dataFile == (char*) NULL) {
    printf("Usage: fdrThreshold --data input_file_name [--output_file output_file_name --fdr {0..1} --corr_type {1, 0, -1}]\n\tDefaults: fdr == 0.05, corr_type == 0\n");
    exit(0);
  }  
  
  return args;
}

int main(int argc, char *argv[]) {
  int i, numVars, numSamples, tidx, sigCount;
  double *Z;
  float *P;
  float pt;
  int *binCount;
  FILE *out;
  char type;
  double meanBinCount, stdBinCount;
  int c;
  ARGUMENTS args = parseOptions(argc, argv);
  FILE *fid;

  ReadFile(args.dataFile, &Z, &numVars, &numSamples);

  /* make a float-sized copy */
  P = (float*) calloc(numVars*numSamples, sizeof(float));
  for (i = 0; i < numVars*numSamples; i++) {
    P[i] = (float) zToP(Z[i], POSITIVE);
  }
  
  pt = fdr(P, numVars*numSamples, args.fdr, args.corrType);
  if (pt == -1) {
    printf("Cannot find a threshold for FDR of %f\n", args.fdr);
  } else {
    /* Do the slow search for this probability value to find index; this should be fast enough! */
    tidx = -1;
    sigCount = 0;
    for (i = 0; i < numVars * numSamples; i++) {
      if (P[i] == pt) {
	tidx = i;
	sigCount++;
      } else if (P[i] < pt) {
	sigCount++;
      }
    }
    printf("FDR level %f corresponds to the threshold value of %f; %d values are significant (out of %d)\n", args.fdr, Z[tidx], sigCount, numVars*numSamples);
  }
  if (args.outFile != (char*) NULL) {
    fid = fopen(args.outFile, "w");
    fprintf(fid, "FDR\t%f\n", args.fdr);
    fprintf(fid, "z_threshold\t%f\n", Z[tidx]);
    fprintf(fid, "num_sig_entries\t%d\n", sigCount);
    fprintf(fid, "num_entries\t%d\n", numVars*numSamples);
    fclose(fid);
    free(args.outFile);
  }
  free(P);
  free(Z);  
  free(args.dataFile);
}





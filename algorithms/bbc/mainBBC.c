/* This file contains various functions for Bayesian BiClustering.          
                                                                            
 * The Bayesian BiClustering (BBC) program was written by Jiajun Gu         
 * at the School of Engineering and Applied Sciences at Harvard University.
 *                                                                          
 * Permission to use, copy, modify, and distribute this software and its    
 * documentation with or without modifications and for any purpose and      
 * without fee is hereby granted, provided that any copyright notices       
 * appear in all copies and that both those copyright notices and this      
 * permission notice appear in supporting documentation, and that the       
 * names of the contributors or copyright holders not be used in            
 * advertising or publicity pertaining to distribution of the software      
 * without specific prior permission.                                       
 *                                                                          
 * THE CONTRIBUTORS AND COPYRIGHT HOLDERS OF THIS SOFTWARE DISCLAIM ALL     
 * WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED           
 * WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL THE         
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT    
 * OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS   
 * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE    
 * OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE   
 * OR PERFORMANCE OF THIS SOFTWARE.                                         
 *                                                                          
 */




#include "hBBC.h"
   int lt=0;


int main (int argc, char **argv ){
  char **genes,**samples, fname[400], fout1[400];
  float **tau_a, **tau_b,*tau_e;
  int ***delta, ***kapa;
  int **sdelta,**skapa;
  char **ym;
  float **ek, **bk, *qq, *smu,**salpha,**sbeta, *taua_a, *taua_b, *taub_a, *taub_b, *ek_a, *ek_b, *bk_a, *bk_b, taue_a, taue_b ;
  float **y, normrange=-1;
  int i,j,nk,ik,nn,pp,normchoice=-1;
  float bic[2];
  if (argc>=9&&argc%2==1){
    for (i=1;i<argc;i=i+2){
      if (argv[i][0]=='-'){
	switch (argv[i][1]){
	case 'i':
	  for(j=0;j<strlen(argv[i+1]);j++){
	    fname[j]=argv[i+1][j];
	  }
	  fname[j]='\0';

	  break;
	case 'k':
	  j=sscanf(argv[i+1],"%d",&nk);
	  break;
	case 'o':
	  for(j=0;j<strlen(argv[i+1]);j++){
	    fout1[j]=argv[i+1][j];
	  }
	  fout1[j]='\0';
	break;
	case 'n': //normalization choice
	  normchoice=-1;
	  switch (argv[i+1][0]){
	  case 'n':
	  case'N':
	    if ((argv[i+1][1]=='o'||argv[i+1][1]=='O')
		&&(argv[i+1][2]=='n' ||argv[i+1][2]=='N')
		&&(argv[i+1][3]=='e'||argv[i+1][3]=='E'))
	      normchoice=0;
	    else{ 
	      printf("*** Invalid normalization method ***\n\n");
	      exit(EXIT_FAILURE);
	    }
	    break;
	  case 'r':
	  case 'R':
	    if ((argv[i+1][1]=='s'||argv[i+1][1]=='S')
		&&(argv[i+1][2]=='n'||argv[i+1][2]=='N'))
	      normchoice=1;
	    else{ 
	      printf("*** Invalid normalization method ***\n\n");
	      exit(EXIT_FAILURE);
	    }
	    break;
	  case 'c':
	  case 'C':
	    if ((argv[i+1][1]=='s'||argv[i+1][1]=='S')
		&&(argv[i+1][2]=='n'||argv[i+1][2]=='N'))
	      normchoice=2;
	    else{ 
	      printf("*** Invalid normalization method ***\n\n");
	      exit(EXIT_FAILURE);
	    }
	    break;
	  case 'i':
	  case'I':
	    if ((argv[i+1][1]=='q'||argv[i+1][1]=='Q')
                &&(argv[i+1][2]=='r' ||argv[i+1][2]=='R')
                &&(argv[i+1][3]=='n'||argv[i+1][3]=='N'))
	      normchoice=3;
	    else{ 
	      printf("*** Invalid normalization method ***\n\n");
	      exit(EXIT_FAILURE);
	    }
	    break;
	  case 's':
	  case 'S':
	    if ((argv[i+1][1]=='q'||argv[i+1][1]=='Q')
                &&(argv[i+1][2]=='r' ||argv[i+1][2]=='R')
                &&(argv[i+1][3]=='n'||argv[i+1][3]=='N'))
	      normchoice=4;
	    else{ 
	      printf("*** Invalid normalization method ***\n\n");
	      exit(EXIT_FAILURE);
	    }
	    break;
	  default:
	    printf("*** Invalid normlization method ***\n\n");
	    exit(EXIT_FAILURE);
	  }
	  break;
	case 'r': // normalization range
	  j=sscanf(argv[i+1],"%f",&normrange);
	  if (normrange>100 || normrange<=5){
	    printf("*** Invalid parameter value for normalization.\nThe alpha value for normalization should be between 5 and 100!  ***\n\n");
	    exit(EXIT_FAILURE);
	  }
	  normrange=normrange/100;
	  break;
	default:
	  printf("*** Invalid parameters! ***\n\n");
	  exit(EXIT_FAILURE);
	}
      }
      else{
	printf(" *** Invalid command format! \n\n");
	exit(EXIT_FAILURE);
      }
    }
    //printf("%d\t%f\n",normchoice, normrange);
    if (normchoice>2 &&normrange<0){
      printf("*** Invalid command format! The alpha value for normalization is missing. ***\n");
      exit(EXIT_FAILURE);
    }
  }
  else if(argc==1){
    printf("Bayesian BiClustering Usage:\n");
    printf("./BBC -i InputFileName  -k NumberofClusters -o OutputFileName -n NormalizationMethod -r NormalizationAlphaValue(Optional)\n\n");
    exit(EXIT_FAILURE);
  }
  else{
    printf("*** Invalid Command Format ***\n\n");
    exit(EXIT_FAILURE);
  }
  printf("*** Bayesian BiClustering Starts! ***\n");
  // allocate Nss memory  
  i=getsize(fname,&nn,&pp);
  if (i<0){
    printf("*** Cannot find the input file! ***\n\n");
    exit(EXIT_FAILURE);
  }
  else{
    printf("%s%d%s%d%s\n",
	   "*** Input file is opened successfully. It contains "
	   ,nn," genes and ",pp," conditions. ***");
  }

  tau_a=malloc(Nss*sizeof(float*));
  tau_b=malloc(Nss*sizeof(float*));
  bk=malloc(Nss*sizeof(float*));
  ek=malloc(Nss*sizeof(float*));
  tau_e=malloc(Nss*sizeof(float));
  
  qq=malloc(nk*sizeof(float));
  delta=malloc(Nss*sizeof(int**));
  kapa=malloc(Nss*sizeof(int**));
  sdelta=malloc(nk*sizeof(int*));
  skapa=malloc(nk*sizeof(int*));
  smu=malloc(nk*sizeof(float));
  salpha=malloc(nk*sizeof(float));
  sbeta=malloc(nk*sizeof(float));
  taua_a=malloc(nk*sizeof(float));
  taua_b=malloc(nk*sizeof(float));
  taub_a=malloc(nk*sizeof(float));
  taub_b=malloc(nk*sizeof(float));
  bk_a=malloc(nk*sizeof(float));
  bk_b=malloc(nk*sizeof(float));
  ek_a=malloc(nk*sizeof(float));
  ek_b=malloc(nk*sizeof(float));
  
  for (i=0;i<nk;i++){
    salpha[i]=malloc(nn*sizeof(float));
    sbeta[i]=malloc(pp*sizeof(float));
    sdelta[i]=malloc(nn*sizeof(int));
    skapa[i]=malloc(pp*sizeof(int));
    taua_a[i]=10;
    taua_b[i]=1;
    taub_a[i]=10;
    taub_b[i]=1;
    bk_a[i]=10;
    bk_b[i]=1;
    ek_a[i]=10;
    ek_b[i]=1;
  }
  taue_a=10;
  taue_b=1;
  for(i=0;i<Nss;i++){
    bk[i]=malloc(nk*sizeof(float));
    ek[i]=malloc(nk*sizeof(float));
    delta[i]=malloc(nk*sizeof(int*));
    kapa[i]=malloc(nk*sizeof(int*));
    tau_a[i]=malloc(nk*sizeof(float));
    tau_b[i]=malloc(nk*sizeof(float));
    for(ik=0;ik<nk;ik++){
      delta[i][ik]=malloc(nn*sizeof(int));
      kapa[i][ik]=malloc(pp*sizeof(int));
    }
  }
  genes=malloc(nn*sizeof(char*));
  samples=malloc(pp*sizeof(char*));
  y=malloc(nn*sizeof(float*));
  ym=malloc(nn*sizeof(char*));
  
  for (i=0;i<nn;i++){
    genes[i]=malloc(600*sizeof(char));
    y[i]=malloc(pp*sizeof(float));
    ym[i]=malloc(pp*sizeof(char));
  }
  for(i=0;i<pp;i++){
    samples[i]=malloc(300*sizeof(char));
  }
  for (i=0;i<nk;i++){
    qq[i]=0.1;
  }
  
  
  //read in datafile
  
  dataY_yeast(nn,pp,y,ym,genes,samples,fname,lt);
  switch (normchoice){
  case 1:
    rowstd(nn,pp,y,ym);//row normalization
    printf("*** Normalization finished ***\n");
    break;
  case 2:
    colstd(nn,pp,y,ym);//column normalization
    printf("*** Normalization finished ***\n");    
    break;
  case 3:
    norm_iqr(nn,pp,y,ym,&normrange);//iqr
    printf("*** Normalization finished ***\n");    
    break;
  case 4:
    norm_sqr(nn,pp,y,ym,&normrange);//sqrn
    printf("*** Normalization finished ***\n");
    break;
  default:
    break;
  }
  
  gibbssample(fout1,nk,nn, pp,y,ym,bk,tau_a,tau_b, tau_e,ek,
	      bk_a, bk_b, taua_a,taua_b,taub_a,taub_b, ek_a, ek_b, taue_a, taue_b,
	      qq,delta,kapa,sdelta,skapa,smu,salpha,sbeta,bic);
  output(nn, pp, fout1,nk,sdelta, skapa,smu, salpha, sbeta,bic,genes,samples);


  printf("*** Bayesian BiClustering finished! ***\n\n");
  return(0);
}

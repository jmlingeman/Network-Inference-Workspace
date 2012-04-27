/* This file contains various functions for Bayesian BiClustering.
 
 * The Bayesian BiClustering (BBC) program was written by Jiajun Gu 
 * at the School of Engineering and Applied Sciences at Harvard University
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
float a=0.0,b=1.0,a_e=10,b_e=10;

float tau_c=1.0;
gsl_rng *r;


void colstd(int nn, int pp, float **y, char **ym){

  int i,j,n1;
  double m,s;
  for (j=0;j<pp;j++){
    m=0;
    n1=0;
    for (i=0;i<nn;i++){
      if (ym[i][j]<1){
	m+=y[i][j];
	n1++;
      }
    }
    m/=n1;
    s=0;
    for(i=0;i<nn;i++){
      if (ym[i][j]<1){
	s+=(y[i][j]-m)*(y[i][j]-m);
      }
    }
    s=sqrt(s/(n1-1));
    for (i=0;i<nn;i++){
      if (ym[i][j]<1)
	y[i][j]=(y[i][j]-m)/s;
      else y[i][j]=0;
    }
  }
}
void rowstd(int nn, int pp, float **y, char **ym){

  int i,j,n1;
  double m,s;
  for (i=0;i<nn;i++){
    m=0;
    n1=0;
    for (j=0;j<pp;j++){
      if (ym[i][j]<1){
        m+=y[i][j];
        n1++;
      }
    }
    m/=n1;
    s=0;
    for(j=0;j<pp;j++){
      if (ym[i][j]<1){
        s+=(y[i][j]-m)*(y[i][j]-m);
      }
    }
    s=sqrt(s/(n1-1));
    for (j=0;j<pp;j++){
      if (ym[i][j]<1)
        y[i][j]=(y[i][j]-m)/s;
      else y[i][j]=0;
    }
  }
}

void dataY(int nn, int pp, float **y,char** genes, char **samples, char fname[]){
  FILE *fid;
  double z;
  int i,j,ti,k;
  char t[300]="0",t1;
  fid=fopen(fname,"r");
  t1='0';
  while (t1!='\t'){
    fscanf(fid,"%c",&t1);
  }
  t1='0';
  while (t1!='\t'){
    fscanf(fid,"%c",&t1);
  }
  // fscanf(fid,"%s\t%s\t",t,t1);
  for (j=0;j<pp;j++){
    fscanf(fid,"%s",samples[j]);
    // samples[j]=t;
  }
  for (i=0;i<nn;i++){
    t1='0';
    k=0;
    while (t1!='\n'){
      fscanf(fid,"%c",&t1);
    }

    while (t1!='\t'){
      fscanf(fid,"%c",&t1);
      genes[i][k]=t1;
      k++;
    }
    genes[i][k]=t1;
    k++;
    t1='0';
    while (t1!='\t'){
      fscanf(fid,"%c",&t1);
      genes[i][k]=t1;
      k++;
    }
    genes[i][k]='\0';
    for (j=0;j<pp;j++){
      fscanf(fid,"%s",t);
      ti=sscanf(t,"%lf",&z);
      if (z>1e-10) y[i][j]=log(z);
      else y[i][j]=-10;
    }
    fscanf(fid, "%c", &t1);
  }
  //exit(11);
  fclose(fid);
}
int getsize(char *fname,int *nn,int *pp){
  FILE *fid;
  char *tmp,*t1;
  int i,j,status=0;
  tmp=malloc(1e6*sizeof(char));
  t1=malloc(200*sizeof(char));
  fid=fopen(fname,"r");
  if (fid==NULL){
    status=-1;
  }
  else{
    fgets(tmp,1e6,fid);
    //get col number
    i=0;j=0;
    while (tmp[i]!='\n'&&i<1e6){
      if (tmp[i]=='\t'&&tmp[i+1]!='\n') j++;
      i++;
    }
    *pp=j;
    i=0;
    while (fgets(tmp,1e6,fid)!=NULL){
      i++;
    }
    *nn=i;
    status=1;
  }
  free (tmp);
  free (t1);
  return(status);
}

void dataY_yeast(int nn, int pp, float **y,char **ym, char** genes, char **samples, char fname[], int lt){
  FILE *fid;
  double z;
  int i,j,ti,k;
 char t[300]="0",t1;
  fid=fopen(fname,"r");
  t1='0';
  while (t1!='\t'){
    fscanf(fid,"%c",&t1);
  }
  for (j=0;j<pp;j++){
    t1='0';
    k=0;
    fscanf(fid,"%c",&t1);
    while (t1!='\t'&&t1!='\n'){
      samples[j][k]=t1;
      k++;
      fscanf(fid,"%c",&t1);
    }
    samples[j][k]='\0';
  }
  for (i=0;i<nn;i++){
    fscanf(fid,"%c",&t1);
    k=0;
    while (t1!='\t'){
      genes[i][k]=t1;
     
      fscanf(fid,"%c",&t1);
      k++;
    }
    k++;
    genes[i][k]='\0';
    for (j=0;j<pp;j++){
      fscanf(fid,"%s",t);
      if (t[0]=='N'){
	ym[i][j]=1;
	y[i][j]=0;
      }
      else{
	ti=sscanf(t,"%lf",&z);
	ym[i][j]=0;
	y[i][j]=z;
      }
    }
    fscanf(fid,"%c",&t1); // READ /N
    k=0;
    while (t1!='\n'&&!feof(fid)){
      fscanf(fid,"%c",&t1);
    }
  }
  fclose(fid);
}




void dataY_noheader(int nn, int pp, float **y,char **ym, char** genes, char **samples, char fname[],int lt){
  FILE *fid;
  double z;
  int i,j,ti,k;
  char t[300]="0",t1[10];
  fid=fopen(fname,"r");
  for (j=0;j<pp;j++){
    samples[j][0]='s';
    //samples[j][6]='1';                                                                                                            
    ti=sprintf(t1,"%d",j+1);
    for(k=1;k<1+ti;k++){
      samples[j][k]=t1[k-1];
    }
    samples[j][k]='\0';
  }
  for (i=0;i<nn;i++){
    genes[i][0]='g';
    ti=sprintf(t1,"%d",i+1);
    for(k=1;k<1+ti;k++){
      genes[i][k]=t1[k-1];
    }
    genes[i][k]='\0';

    for (j=0;j<pp;j++){
      fscanf(fid,"%s",t);
      ti=sscanf(t,"%lf",&z);
      if (lt>0){
        if (z>1e-10) y[i][j]=log(z);
        else y[i][j]=-10;
      }
      else y[i][j]=z;
      ym[i][j]=0;
    }
 }
  fclose(fid);
}

void norm_iqr(int nn, int pp, float **y, char **ym, float *normrange){
  int i,j;
  size_t ni,n1,n2, n;
  double *yc,m,s;
  n=nn;
  yc=malloc(n*sizeof(double));
  for (j=0;j<pp;j++){
    ni=0;
    for (i=0;i<nn;i++){
      if (ym[i][j]<1) {
	yc[ni]=y[i][j];
	ni++;
      }
    }
    gsl_sort(yc,1,ni);
    n1=ni*(0.5-*normrange*0.5);
    n2=ni*(0.5+*normrange*0.5);
    //printf("%f\t%d\t%d\n",*normrange, n1, n2);
    m=0.0;
    for (ni=n1;ni<n2+1;ni++){
	m+=yc[ni];
    }
    m=m/(n2-n1+1);
    s=0.0;
    for(ni=n1;ni<n2+1;ni++){
      s+=(yc[ni]-m)*(yc[ni]-m);
    }
    s/=(n2-n1);
    s=sqrt(s);
    for (i=0;i<nn;i++){
      if (ym[i][j]<1){
	y[i][j]=(y[i][j]-m)/s;
      }
    }
  }
  free (yc);
}

void norm_sqr(int nn, int pp, float **y, char **ym, float *normrange){
  int i,j, range,idmindiff,*pm;
  size_t ni;
  double *yc,m,s,mindiff;
  yc=malloc(nn*sizeof(double));
  pm=malloc(nn*sizeof(double));
  for (j=0;j<pp;j++){
    ni=0;
    for (i=0;i<nn;i++){
      if (ym[i][j]<1) {
        yc[ni]=y[i][j];
        ni++;
      }
    }
    range=(int) ni*(*normrange);
    gsl_sort_index(pm,yc,1,ni);
    mindiff=1e10;
    idmindiff=0;
    for(i=0;i<range;i++){
      if ((yc[pm[i+range]]-yc[pm[i]])<mindiff){
        mindiff=yc[pm[i+range]]-yc[pm[i]];
	idmindiff=i;
      }
    }
    m=0.0;
    for (ni=idmindiff;ni<idmindiff+range;ni++){
      m+=yc[pm[ni]];
    }
    m/=range;
    s=0.0;
    for(ni=idmindiff;ni<idmindiff+range;ni++){
      s+=(yc[pm[ni]]-m)*(yc[pm[ni]]-m);
    }
    s/=(range-1);
    s=sqrt(s);

    for (i=0;i<nn;i++){
      if (ym[i][j]<1){
        y[i][j]=(y[i][j]-m)/s;
      }
    }
  }
  free(pm);
  free(yc);
}


double pcorr(float *d1,double *d2,int n){
  double result=0, de1=0, de2=0, sum1=0, sum2=0;
  int i;
  for (i=0;i<n;i++){
    sum1+=d1[i];
    sum2+=d2[i];
    result+= d1[i]*d2[i];
    de1+=d1[i]*d1[i];
    de2+=d2[i]*d2[i];
  }
  result-=sum1*sum2/n;
  de1-=sum1*sum1/n;
  de2-=sum2*sum2/n;
  result=result/sqrt(de1*de2);
  if (sum1==0||sum2==0) result=0;
  if (de1==0||de2==0) result=0;
  return result;
}

void initial3(int nn, int pp, float **y, int nk, float **ek, float**bk, float* tau_e, float **tau_a, float **tau_b, int ***delta,int*** kapa, int **sdelta, int **skapa, float *taua_a, float *taua_b, float* taub_a, float* taub_b, float* ek_a, float* ek_b, float* bk_a, float* bk_b, float taue_a, float taue_b ){
  int *ifound, npass, *clusterid;
  int i,i1,j1,ik,keep;
  double *v, tmp;
  float *yc;
  yc=malloc(nn*sizeof(float));
  gsl_vector *s = gsl_vector_alloc(pp);
  gsl_permutation * p= gsl_permutation_alloc(pp);
  npass=2;
  v=malloc(sizeof(double)*nn);
  ifound=malloc(sizeof(int)); // how many times a cluster has been found;
  clusterid=malloc(nn*sizeof(int)); // clusterid of each gene;
  kcluster(nk, nn, pp, y, clusterid, ifound, npass);
  
  for (i1=0;i1<nn;i1++){
    delta[0][clusterid[i1]][i1]=1;
  }
    if (nk==1){
    for (i1=0;i1<nn;i1++){
      tmp=0;
      for (j1=0;j1<pp;j1++)	tmp+=y[i1][j1];
      gsl_vector_set(s,i1,tmp);
    }
  }
  
  for(ik=0;ik<nk;ik++){
    for (i1=0;i1<nn;i1++){
      if (delta[0][ik][i1]){
	v[i1]=1;
      }
      else v[i1]=0;
      sdelta[ik][i1]=0;
    }
      
    for (j1=0;j1<pp;j1++){
      skapa[ik][j1]=0;
      kapa[0][ik][i1]=0;
      for (i1=0;i1<nn;i1++){
	yc[i1]=y[i1][j1];
      }
      tmp=pcorr(yc,v,nn);
      //tmp=abs(tmp);
      gsl_vector_set(s, j1, tmp);
      //   printf("%f\n",tmp);
    }
  
    gsl_sort_vector_index(p,s);
    keep=2;
    if (0.2*pp>keep) keep=0.2*pp;
    for (j1=0;j1<keep;j1++){
      i=gsl_permutation_get(p,pp-1-j1);
      kapa[0][ik][i]=1;
    }
    
    ek[0][ik]=gsl_ran_gamma(r,ek_a[ik], ek_b[ik]);
    //printf("%d\t%f\n",ik,ek[0][ik]);
    tau_a[0][ik]=gsl_ran_gamma(r,taua_a[ik], taua_b[ik]);
    //printf("%d\t%f\n",ik,ek[0][ik]);
    tau_b[0][ik]=gsl_ran_gamma(r,taub_a[ik], taub_b[ik]);
    //printf("%d\t%f\n",ik,ek[0][ik]);
    bk[0][ik]=gsl_ran_gamma(r,bk_a[ik],bk_b[ik]);
    //printf("%s\t%d\n","var",ik);
  }
  //printf("%s\n","before tau_e");
  tau_e[0]=gsl_ran_gamma(r, taue_a,taue_b);
  free(v);
  free(yc);
}


void output(int nn, int pp, char fout[],int nk,int **sdelta, int **skapa, float *smu, float**salpha, float**sbeta,float* bic, char **genes,char** samples){
  FILE *fid;
  int i,ik,j, n1,n2,j1;
  int *isempty;
  isempty=malloc(nk*sizeof(int));
  j1=0;
  for (ik=0;ik<nk;ik++){
    n1=0;n2=0;isempty[ik]=1;
    for (i=0;i<nn;i++){
      if (sdelta[ik][i]>0) n1++;
    }
    for (j=0;j<pp;j++){
      if(skapa[ik][j]>0) n2++;
    }
    if (n1>1&&n2>1){
      j1++;
      isempty[ik]=0;
    }
    
  }
   
  fid=fopen(fout,"w");
  fprintf(fid,"%s%d\t%s%d\t%s%f\t%s%f\t%s%f\n","K=",nk,"Number of stable clusters: ", j1,  "likelihood: ",bic[1],"Number of parameters: ",bic[0],"BIC: ",-2*bic[1]+bic[0]*log(nn*pp));
  
  n1=0;
  for (ik=0;ik<nk;ik++){
    if (isempty[ik]==0){
      fprintf(fid,"%s%d\n", "bicluster",n1+1);
      n1++;
      fprintf(fid, "%s%f\n", "bicluster main effect: ", smu[ik]);
      fprintf(fid,"%s\t%s\t%s\n","row", "genes names", "gene effects");
      for(i=0;i<nn;i++){
	if (sdelta[ik][i]>0)      fprintf(fid,"%d\t%s\t%f\n",i+1,genes[i], salpha[ik][i]);
      }
      fprintf(fid,"%s\t%s\t%s\n","col", "condition names", "condition effects");
      for(j=0;j<pp;j++){
	if (skapa[ik][j]>0)fprintf(fid,"%d\t%s\t%f\n",j+1,samples[j], sbeta[ik][j]);
      }
    }
  }
  fclose(fid);
  free(isempty);
}

/*****************************************************************/
  
void sp_sym_mul(double A[],double B[], double C[],int mj){
  C[0]=A[0]*B[0]+(mj-1)*A[1]*B[1];
  C[1]=A[0]*B[1]+A[1]*B[0]+(mj-2)*A[1]*B[1];
}

void sp_sym_inv(double A[],double IA[],int mj){
  IA[0]=(A[0]+(mj-2)*A[1])/(A[0]*A[0]-(mj-1)*A[1]*A[1]+(mj-2)*A[0]*A[1]);
  IA[1]=-A[1]/(A[0]*A[0]-(mj-1)*A[1]*A[1]+(mj-2)*A[0]*A[1]);
}

void sp_sym_mul3(double A[],double B[],double C[],double D[],int mj){
  double TEMP[2];
  sp_sym_mul(A,B,TEMP,mj);
  sp_sym_mul(TEMP,C,D,mj);
}

void sp_BLsym_inv(double A[],double B[], double C[],double D[],int mi,int mj){ //solve C1,D1
  double IA[2],t1[2],t2[2];
  sp_sym_inv(A,IA,mj);
  sp_sym_mul3(B,IA,B,t1,mj);
  t2[0]=A[0]-(mi-1)*t1[0]+(mi-2)*B[0];
  t2[1]=A[1]-(mi-1)*t1[1]+(mi-2)*B[1];
  sp_sym_inv(t2,t1,mj);
  sp_sym_mul3(t1,B,IA,t2,mj);
  D[0]=-t2[0];
  D[1]=-t2[1];
  t1[0]=A[0]-B[0];
  t1[1]=A[1]-B[1];
  sp_sym_inv(t1,t2,mj);
  C[0]=t2[0]+D[0];
  C[1]=t2[1]+D[1];
}
void sp_sym4_inv4(double A[],double IA[],int mj){
  IA[0]=-((mj-2)*A[3]+A[1])/(A[2]*A[2]*(mj-1)-A[0]*A[3]*(mj-2)-A[0]*A[1]);
  IA[2]=A[2]/(A[2]*A[2]*(mj-1)-A[0]*A[3]*(mj-2)-A[0]*A[1]);
  IA[1]=((mj-2)*A[2]*A[2]-(mj-3)*A[0]*A[3]-A[0]*A[1])/(A[2]*A[2]*(mj-1)-A[0]*A[3]*(mj-2)-A[0]*A[1])/(A[1]-A[3]);
  IA[3]=(A[0]*A[3]-A[2]*A[2])/(A[2]*A[2]*(mj-1)-A[0]*A[3]*(mj-2)-A[0]*A[1])/(A[1]-A[3]);
}



void sp_sym4_mul(double A[],double B[],double C[],int mj){
  C[0]=A[0]*B[0]+(mj-1)*A[2]*B[2];
  C[1]=A[2]*B[2]+A[1]*B[1]+(mj-2)*A[3]*B[3];
  C[2]=A[0]*B[2]+A[2]*B[1]+(mj-2)*A[2]*B[3];
  C[3]=A[2]*B[0]+A[2]*B[2]+(mj-2)*A[3]*B[2];
  C[4]=A[2]*B[2]+A[3]*B[1]+A[1]*B[3]+(mj-3)*A[3]*B[3];
}
  
void sp_sym4_5_mul(double A[],double B[],double C[],int mj){
  C[0]=A[0]*B[0]+(mj-1)*A[2]*B[3];
  C[1]=A[3]*B[2]+A[1]*B[1]+(mj-2)*A[4]*B[4];
  C[2]=A[0]*B[2]+A[2]*B[1]+(mj-2)*A[2]*B[4];
  C[3]=A[3]*B[0]+A[1]*B[3]+(mj-2)*A[4]*B[3];
  C[4]=A[3]*B[2]+A[1]*B[4]+A[4]*B[1]+(mj-3)*A[4]*B[4];
}
  

void sp_BLsym4_inv(double A[],double B[], double C[], double D[],int mi,int mj){
  double IA[4],t1[4],t2[4],t3[5],t4[5],t5[5];
  int i;
  sp_sym4_inv4(A,IA,mj);
  t1[0]=0;
  t1[1]=B[1]*B[1]*IA[1];
  t1[2]=0;
  t1[3]=B[1]*B[1]*IA[3];

  for (i=0;i<4;i++){
    t2[i]=A[i]-(mi-1)*t1[i]+(mi-2)*B[i];
  }
  sp_sym4_inv4(t2,t1,mj);
  sp_sym4_mul(t1,B,t3,mj);
  t5[0]=IA[0];
  t5[1]=IA[1];
  t5[2]=IA[2];
  t5[3]=IA[2];
  t5[4]=IA[3];
  sp_sym4_5_mul(t3,t5,t4,mj);
  for (i=0;i<5;i++){
    D[i]=-t4[i];
  }
  for (i=0;i<4;i++){
    t1[i]=A[i]-B[i];
  }
  sp_sym4_inv4(t1,t2,mj);
  C[0]=t2[0]+D[0];
  C[1]=t2[1]+D[1];
  C[2]=t2[2]+D[2];
  C[3]=t2[2]+D[3];
  C[4]=t2[3]+D[4];
}
                                                                         
void sp_BLsym_inv_diff(double A[],double B[],double D[],double CMd[],int mi,int mj){ //solve C1-C0, that is C_delta
  double IA[2],t1[2],t2[2];
  sp_sym_inv(A,IA,mj);
  sp_sym_mul3(B,IA,B,t1,mj);
  t2[0]=A[0]-(mi-1)*t1[0]+(mi-2)*B[0];
  t2[1]=A[1]-(mi-1)*t1[1]+(mi-2)*B[1];
  sp_sym_inv(t2,t1,mj);
  sp_sym_mul3(t1,B,IA,t2,mj);
  t1[0]=-t2[0];
  t1[1]=-t2[1];
  CMd[0]=D[0]-t1[0];
  CMd[1]=D[1]-t1[1];
}

  
double  sp_sym_logdet(double A[],int mj){
  double det;
  if (A[0]+(mj-1)*A[1]>0)
    det=(mj-1)*log(A[0]-A[1])+log(A[0]+(mj-1)*A[1]);
  else
    det=(mj-1)*log(A[0]-A[1])+log(-(A[0]+(mj-1)*A[1]));
  return det;
}
	
double sp_sym_vmv_mul(double x[],double A[],double y[],int mj){
  int i;
  double xs=0,ys=0,t1=0,r;
  for (i=0;i<mj;i++){
    t1+=x[i]*y[i];
    xs+=x[i];
    ys+=y[i];
  }
  r=(A[0]-A[1])*t1+A[1]*xs*ys;
  return r;
}

/***************************************************/
int mysamplek(int nk, double ps[]){
  double s, rd, *eps;
  double x0=0.0, max,t;
  int i, ti,flag=1;
  eps=malloc(nk*sizeof(double));
  for (i=0;i<nk;i++){
    if (ps[i]<=10) eps[i]=exp(ps[i]); 
    else eps[i]=exp(10.0);
    x0+=eps[i];
  }
  x0=1/(1+x0);
  t=1/(1+exp(10.0));
  if (x0<t){   // highly unlikely to be in no cluster;
    max=ps[0];
    for(i=1;i<nk;i++){
      if (max<ps[i]) max=ps[i];
    }
    s=0;
    for(i=0;i<nk;i++){
      eps[i]=ps[i]-max;
      if (eps[i]<-10) eps[i]=0;
      else eps[i]=exp(eps[i]);
      s+=eps[i];
    }
  }
  else{
    s=x0;
    for(i=0;i<nk;i++){
      s+=eps[i];
    }
  }
  for (i=0;i<nk;i++){
    eps[i]/=s;
  }
  
  rd=gsl_ran_flat(r,0, 1);
  //rd=rd/temp;
  ti=-1; //default;
  s=0;
  for (i=0;i<nk&&flag>0;i++){
    s+=eps[i];
    if (rd<=s){
      flag=0;
      ti=i;
    }
  }
  free(eps);
  return ti;
}


/******************************************************************/
void gibbssample(char fout[],int nk,int nn, int pp, float **y,char **ym,float 
		 **bk,float **tau_a,float**tau_b,float *tau_e, float **ek, 
		 float *bk_a, float *bk_b, float *taua_a,float *taua_b,float* 
		 taub_a,float *taub_b, float *ek_a, float *ek_b, float taue_a
		 , float taue_b,float *qq,int ***delta, int ***kapa, int **sdelta, int **skapa, float *smu, float** salpha, float** sbeta
		 , float*bic){
  
  const gsl_rng_type *T;
  int seed;
  int i,j,ik,i1,j1,io,mi,mj,cn,ci,ti, n1, cn2;
  float  temp;
  double *ps;
    double CM[2];
  double DM[2];
  double EM4[5];
  double CMd[2],RM3[2],RM1[2],RM2[2],AM4[4],BM4[4],CM4[5],DM4[5],CM0[2],DM0[2];
  int *mjk, *mik, *candk;
  double **AM, **BM, *mr, *vr, *vd, **vdk;
  float e,u,u1, s, m, mden,m1,m2;
  double r0, r1, r2, r3,ratio,det,te;

  int tempcount, mflag=0;
  int flag=1;
  n1=(nn>pp)?nn:pp;
  mjk=malloc(nk*sizeof(int));
  mik=malloc(nk*sizeof(int));
  candk=malloc(n1*sizeof(int));
  AM=malloc(nk*sizeof(double*));
  BM=malloc(nk*sizeof(double*));
  vdk=malloc(nk*sizeof(double*));
  for (ik=0;ik<nk;ik++){
    AM[ik]=malloc(2*sizeof(double));
    BM[ik]=malloc(2*sizeof(double));
    vdk[ik]=malloc(n1*sizeof(double));
  }
  mr=malloc(n1*sizeof(double));
  vr=malloc(n1*sizeof(double));
  vd=malloc(n1*sizeof(double));

  ps=malloc(nk*sizeof(double));

  seed=(int) time (NULL);
  gsl_rng_env_setup();
  T=gsl_rng_default;
  r=gsl_rng_alloc(T);
  gsl_rng_set(r,seed); 

  //printf("%d\n",seed); 
  for (io=1;io<Ns;io++){
    if (io<Nss) i=io;
    else{
      for (j=0;j<Nss-1;j++){
	delta[j]=delta[j+1];
	kapa[j]=kapa[j+1];
      }
      i=Nss-1;
    }
    //printf("%d\t%d\n",io,i);  
    // initialize
    if (io==1){
      initial3(nn, pp, y, nk,ek, bk,tau_e, tau_a, tau_b, delta, kapa,sdelta, skapa, taua_a, taua_b, 
      	 taub_a, taub_b, ek_a, ek_b, bk_a,  bk_b,  taue_a, taue_b);
      printf("%s\n","*** Initialization finished. ***");
      printf("%s\n","*** Start Sampling. This step may take time. ***");
    }
    

    for(ik=0;ik<nk;ik++){
      //kapa                                                                         
      mj=0;
      mi=0;
      //first get A B                                                                
      for (j1=0;j1<pp;j1++){
        if (kapa[i-1][ik][j1]>0){
          mj++;
        }
      }
      for (i1=0;i1<nn;i1++){
        if (delta[i-1][ik][i1]>0){
          vd[mi]=0.0;
          for (j1=0;j1<pp;j1++){
            if(kapa[i-1][ik][j1]>0){
              vd[mi]+=y[i1][j1];
            }
          }
          mi++;
        }
      }
      AM[0][0]=1/ek[i-1][ik]+1/tau_a[i-1][ik]+1/tau_b[i-1][ik]+1/bk[i-1][ik];
      AM[0][1]=1/bk[i-1][ik]+1/tau_b[i-1][ik];
      BM[0][0]=1/bk[i-1][ik]+1/tau_a[i-1][ik];
      BM[0][1]=1/bk[i-1][ik];
      tempcount=0;
      //printf("hello\n");
      for (j1=0;j1<pp;j1++){
	cn=0;
	r3=0;
	mflag=0;
	//printf("%d\n",io);
	//printf("%d\t%d\t%d\n",i,ik,j1);
	//printf("%d\n",kapa[i][ik][j1]);

	  if (kapa[i-1][ik][j1]>0) {
	    mj--;mflag=1;
	    for (i1=0;i1<nn;i1++){
	      if (delta[i-1][ik][i1]>0){
		vd[cn]=vd[cn]-y[i1][j1];
		vr[cn]=y[i1][j1];
		vd[cn]/=mj;
		r3+=vr[cn]*vr[cn]*tau_e[i-1];
	      cn++;
	      
	      }
	    }
	  }
	
	else{
	  for (i1=0;i1<nn;i1++){
	    if (delta[i-1][ik][i1]>0){
	      vd[cn]/=mj;
	      vr[cn]=y[i1][j1];
	      r3+=vr[cn]*vr[cn]*tau_e[i-1];
	      cn++;
	    }
	  }
	}
	
	sp_BLsym_inv(AM[0],BM[0], CM,DM,mj+1,mi); //solve C1,D1
	sp_BLsym_inv(AM[0],BM[0],CM0,DM0,mj,mi);
	sp_BLsym_inv_diff(AM[0],BM[0],DM,CMd,mj,mi); //solve C1-C0, that is C_delta
	sp_sym_mul3(BM[0],CM0,BM[0],RM1,mi);
	sp_sym_mul3(BM[0],DM0,BM[0],RM2,mi);
	RM3[0]=AM[0][0]-RM1[0]*mj-RM2[0]*mj*(mj-1);
	RM3[1]=AM[0][1]-RM1[1]*mj-RM2[1]*mj*(mj-1);
	det=sp_sym_logdet(RM3,mi);
	r1=sp_sym_vmv_mul(vr,DM,vd,mi);
	r2=sp_sym_vmv_mul(vr, CM, vr,mi);
	r0=sp_sym_vmv_mul(vd,CMd,vd,mi);
	ratio=log(qq[ik]/(1-qq[ik]))-0.5*det+mi*0.5*log(1/tau_e[i-1])-0.5*(2*mj*r1+r2+mj*mj*r0-r3);
	//printf("%s\t%d\t%s\t%f\n","j1",j1,"ratio",ratio );
	  if (ratio>20) u1=1;
	  else u1=exp(ratio)/(1+exp(ratio));
	  u=gsl_ran_bernoulli(r,u1);
	  if (u>0){
	    kapa[i][ik][j1]=1;
	    cn=0;
	    tempcount++;
	    for (i1=0;i1<nn;i1++){
	      if(delta[i-1][ik][i1]>0){
		vd[cn]=vd[cn]*mj+y[i1][j1];
		cn++;
	      }
	    }
	    mj++;
	  }
	  else{
	    kapa[i][ik][j1]=0;
	    for (i1=0;i1<mi;i1++){
	      vd[i1]=vd[i1]*mj;
	    }
	  }
	  //  if (mflag>0) mi++;
      } //for j1
     
      //test if this cluster has already been found
      
   
      // if lost a cluster, randomly generate one
      while (tempcount<2) {
	//printf("%s","oops!");
	tempcount=0;
	for (j1=0;j1<pp;j1++){
	  kapa[i][ik][j1]=gsl_ran_bernoulli(r,qq[ik]);
	  if (kapa[i][ik][j1]) tempcount++;
	}
      }
    }
 

    //kapa  
    // define matrices
    for (ik=0;ik<nk;ik++){
      mik[ik]=0;
      AM[ik][0]=1/ek[i-1][ik]+1/tau_a[i-1][ik]+1/tau_b[i-1][ik]+1/bk[i-1][ik];
      AM[ik][1]=1/bk[i-1][ik]+1/tau_a[i-1][ik];
      BM[ik][0]=1/bk[i-1][ik]+1/tau_b[i-1][ik];
      BM[ik][1]=1/bk[i-1][ik];
      for (j1=0;j1<pp;j1++){
	vdk[ik][j1]=0;
      }
      for (i1=0;i1<nn;i1++){
	if (delta[i-1][ik][i1]){
	  mik[ik]++; // count;
	  cn=0;
	  for (j1=0;j1<pp;j1++){
	    if (kapa[i][ik][j1]){
	      vdk[ik][cn]+=y[i1][j1];
	      cn++;
	    }
	  }
	  mjk[ik]=cn;
	}
      }
      for (i1=0;i1<cn;i1++){
	vdk[ik][i1]/=mik[ik];
      }
    }
    
    ci=0;
    for (i1=0;i1<nn;i1++){
      temp=0;
      for (ik=0;ik<nk;ik++){
	r3=0;
	if (delta[i-1][ik][i1]){
	  cn=0;
	  for (j1=0;j1<pp;j1++){
	    if (kapa[i][ik][j1]){
	      vdk[ik][cn]=(vdk[ik][cn]*mik[ik]-y[i1][j1])/(mik[ik]-1);
	      cn++;
	    }
	  }
	  mik[ik]--;

	}
	cn=0;
	for(j1=0;j1<pp;j1++){
	  if (kapa[i][ik][j1]){
	    vr[cn]=y[i1][j1];
	    r3+=vr[cn]*vr[cn]*tau_e[i-1];
	    cn++;
	  }
	}

	// check till this line
	sp_BLsym_inv(AM[ik],BM[ik], CM,DM,mik[ik]+1,mjk[ik]);
	sp_BLsym_inv(AM[ik],BM[ik],CM0,DM0,mik[ik],mjk[ik]);
	sp_BLsym_inv_diff(AM[ik],BM[ik],DM,CMd,mik[ik],mjk[ik]);
	sp_sym_mul3(BM[ik],CM0,BM[ik],RM1,mjk[ik]);
	sp_sym_mul3(BM[ik],DM0,BM[ik],RM2,mjk[ik]);
	RM3[0]=AM[ik][0]-RM1[0]*mik[ik]-RM2[0]*mik[ik]*(mik[ik]-1);
	RM3[1]=AM[ik][1]-RM1[1]*mik[ik]-RM2[1]*mik[ik]*(mik[ik]-1);
	det=sp_sym_logdet(RM3,mjk[ik]);
	//      if (det<0) det=-det;                       
	r1=sp_sym_vmv_mul(vr,DM,vdk[ik],mjk[ik]);
	r2=sp_sym_vmv_mul(vr, CM, vr,mjk[ik]);
	r0=sp_sym_vmv_mul(vdk[ik],CMd,vdk[ik],mjk[ik]);
	ps[ik]=-0.5*det+mjk[ik]*0.5*log(1/tau_e[i-1])-0.5*(2*mik[ik]*r1+r2+mik[ik]*mik[ik]*r0-r3);
	if (mik[ik]==0) ps[ik]=-1e20;
	
      }
      
      //temp=1/(1+temp); // prob of not in any cluster;
      ti=mysamplek(nk, ps); // mysample returns the k;
      if (ti>=0){
	delta[i][ti][i1]=1;
	for (ik=0;ik<ti;ik++){
	  delta[i][ik][i1]=0;
	}
	for (ik=ti+1;ik<nk;ik++){
	  delta[i][ik][i1]=0;
	}
	cn=0;
	for (j1=0;j1<pp;j1++){
	  if (kapa[i][ti][j1])  {
	    vdk[ti][cn]=(vdk[ti][cn]*mik[ti]+y[i1][j1])/(mik[ti]+1);
	    cn++;
	  }
	}
	mik[ti]++;
      }
      else  {
	for (ik=0;ik<nk;ik++){
	  delta[i][ik][i1]=0;
	}
	candk[ci]=i1;
	ci++;
      }
      
    }// i1
    //printf("delta 1\n");
    
    for (ik=0;ik<nk;ik++){
      if (mik[ik]==0){
	for(j1=0;j1<2;j1++){
	  n1=(int) gsl_ran_flat(r,0,nn);
	  if (n1==nn) n1--;
	  delta[i][ik][n1]=1;
	  for (i1=0;i1<ik;i1++){
	    if (delta[i][i1][n1]==1) delta[i][i1][n1]=0;
	  }
	  for (i1=ik+1;i1<nk;i1++){
	    if (delta[i][i1][n1]==1) delta[i][i1][n1]=0;
	  }
	}
      }
    }
 
    //printf("delta done!\n");
    
    if (io%S==0){
      for (ik=0;ik<nk;ik++){
	m=0.0;s=0.0; mden=0.0;
	mi=0;mj=0;
	AM[0][0]=1/ek[i-1][ik]+1/tau_a[i-1][ik]+1/tau_b[i-1][ik];
	AM[0][1]=1/tau_a[i-1][ik];
	BM[0][0]=1/tau_b[i-1][ik];
	BM[0][1]=0;
      
	for (j1=0;j1<pp;j1++){
	  if (kapa[i][ik][j1]>0){
	    mr[j1]=0;
	    mj++;
	    for(i1=0;i1<nn;i1++){
	      if (delta[i][ik][i1]>0){
		mr[j1]+=y[i1][j1];
	      }
	    }
	    mden+=mr[j1];
	  }
	}
	for(i1=0;i1<nn;i1++){
	  if (delta[i][ik][i1]>0) mi++;
	}
	mden=mden/(mi*mj);
	sp_BLsym_inv(AM[0],BM[0],CM,DM,mi,mj);
	s=mi*mj*(CM[0]+(mj-1)*CM[1]+(mi-1)*DM[0]+(mi-1)*(mj-1)*DM[1]);
	m=mden*s/(s+bk[i-1][ik]);
	s=s+bk[i-1][ik];
	if (s>1e-12) s=1/sqrt(s);
	else s=1e6;
	smu[ik]=gsl_ran_gaussian(r, s)+m;
	bk[i][ik]=gsl_ran_gamma(r,bk_a[ik]+0.5, 1/(1/bk_b[ik]+0.5*smu[ik]*smu[ik]));
        	//beta
	AM4[0]=1/tau_a[i-1][ik]+1/ek[i-1][ik];
	AM4[1]=AM4[0]+1/tau_b[i-1][ik];
	AM4[2]=1/tau_a[i-1][ik];
	AM4[3]=AM4[2];
	BM4[0]=0;
	BM4[1]=1/tau_b[i-1][ik];
	BM4[2]=0;
	BM4[3]=0;
	mden-=smu[ik];
	sp_BLsym4_inv(AM4,BM4, CM4,DM4,mi,mj);
	for (i1=0;i1<5;i1++){
	  EM4[i1]=mi*CM4[i1]+mi*(mi-1)*DM4[i1];
	}
	te=(EM4[2]+EM4[3])/2;
	m1=0;cn=0;
	for (j1=0;j1<pp;j1++){
	  if (kapa[i][ik][j1]>0){
	    s=EM4[0];
	    mr[j1]=mr[j1]/mi-smu[ik];
	    m=((EM4[0]-te)*mr[j1]+te*mj*mden); // this si mean * s
	    //m=m/s;
	    s=s+tau_b[i-1][ik];
	    m=m/s;
	    if (s>1e-12) s=1/sqrt(s);
	    else s=1e6;
	    sbeta[ik][j1]=gsl_ran_gaussian(r, s)+m;
	    cn++;
	    m1=sbeta[ik][j1]*sbeta[ik][j1];
	    //beta[i][ik][j1]=m;
	  }
	}
	tau_b[i][ik]=gsl_ran_gamma(r, cn*0.5+taub_a[ik], 1/(1/taub_b[ik]+0.5*m1));
	
	//alpha
	m1=0;cn=0;cn2=0;m2=0;
	for (i1=0;i1<nn;i1++){
	  if (delta[i][ik][i1]>0){
	    s=mj*ek[i-1][ik]+tau_a[i-1][ik];
	    m=0;
	    for (j1=0;j1<pp;j1++){
	      if (kapa[i][ik][j1]>0) m+=y[i1][j1]-sbeta[ik][j1]-smu[ik];
	    }
	    m/=mj; //y1.
	    m=m*mj*ek[i-1][ik];
	    m/=s;
	    if (s>1e-12) s=1/sqrt(s);
	    else s=1e6;
	    salpha[ik][i1]=gsl_ran_gaussian(r,s)+m;
	    cn++;
	    m1+=salpha[ik][i1]*salpha[ik][i1];
	    //m=m/mj;
	    //  alpha[i][ik][i1]=m;
	    
	    for (j1=0;j1<pp;j1++){
	      if (kapa[i][ik][j1]){
		mden=smu[ik]+salpha[ik][i1]+sbeta[ik][j1];
		m2+=(y[i1][j1]-mden)*(y[i1][j1]-mden);
		cn2++;
	      }
	    }
	  }
	}
	tau_a[i][ik]=gsl_ran_gamma(r, cn*0.5+taua_a[ik], 1/(1/taua_b[ik]+0.5*m1 ));
	ek[i][ik]=gsl_ran_gamma(r, cn2*0.5+ek_a[ik], 1/(1/ek_b[ik]+0.5*m2));
	
      }//for ik
      
      // sample tau_e
      cn=0;m1=0;
      for(i1=0;i1<nn;i1++){
	for (j1=0;j1<pp;j1++){
	  flag=1;
	  for (ik=0;ik<nk;ik++){
	    if (delta[i][ik][i1]*kapa[i][ik][j1]){
	      flag=0;
	    }
	  }
	  if (flag){
	    cn++;
	    m1+=y[i1][j1]*y[i1][j1];
	  }
	}
      }
      tau_e[i]=gsl_ran_gamma(r, cn*0.5+taue_a,1/(1/taue_b+0.5*m1 ));
      //printf("tau_e done\n");
      m=0;
      for(i1=0;i1<nn;i1++){
        for (j1=0;j1<pp;j1++){
	  flag=1;
	  for (ik=0;ik<nk;ik++){
	    if (delta[i][ik][i1]*kapa[i][ik][j1]){
	      flag=0;
	      if (ym[i1][j1]){
		y[i1][j1]=(smu[ik]+salpha[ik][i1]+sbeta[ik][j1])+gsl_ran_gaussian(r,1/sqrt(ek[i][ik]));
	      }
	      else{
		mden=y[i1][j1]-(smu[ik]+salpha[ik][i1]+sbeta[ik][j1]);
		m+=-mden*mden*ek[i][ik]+log(ek[i][ik]);
	      }
	    }
	  }
	  if (flag){
	    if (ym[i1][j1]){
	      y[i1][j1]=gsl_ran_gaussian(r,1/sqrt(tau_e[i]));
	    }
	    else m+=-y[i1][j1]*y[i1][j1]*tau_e[i]+log(tau_e[i]);
	  }
	}//i1
      }//j1
      m=m*0.5;
      tau_e[i-1]=tau_e[i];
      ek[i-1]=ek[i];
      bk[i-1]=bk[i];
      tau_a[i-1]=tau_a[i];
      tau_b[i-1]=tau_b[i];
      
          
    }// if io%S
    flag=0;
    if (io>=Ns-NFS){
      for(ik=0;ik<nk;ik++){
	for (i1=0;i1<nn;i1++){
	  sdelta[ik][i1]+=delta[Nss-1][ik][i1];
	  if (delta[i][ik][i1]!=delta[i-1][ik][i1]) flag=1;
	}
	for (j1=0;j1<pp;j1++){
	  skapa[ik][j1]+=kapa[Nss-1][ik][j1];
	  if (kapa[i][ik][j1]!=kapa[i-1][ik][j1]) flag=1;
	}
      }
    }
  }//for io
  n1=0;
  for(ik=0;ik<nk;ik++){

    for (i1=0;i1<nn;i1++){
      if (sdelta[ik][i1]>0.5*NFS) {
        sdelta[ik][i1]=1;
        n1++;
      }
      else sdelta[ik][i1]=0;
    }
    for (j1=0;j1<pp;j1++){
      if (skapa[ik][j1]>0.5*NFS) {
        n1++;
        skapa[ik][j1]=1;
      }
      else skapa[ik][j1]=0;
    }
  }
  bic[0]=n1;   
 
  for (ik=0;ik<nk;ik++){
  //next sample muk, alpha, beta
  m=0.0;s=0.0; mden=0.0;
  mi=0;mj=0;
  AM[0][0]=1/ek[1][ik]+1/tau_a[1][ik]+1/tau_b[1][ik];
  AM[0][1]=1/tau_a[1][ik];
  BM[0][0]=1/tau_b[1][ik];
  BM[0][1]=0;
      
  for (j1=0;j1<pp;j1++){
    if (skapa[ik][j1]>0){
      mr[j1]=0;
      mj++;
      for(i1=0;i1<nn;i1++){
	if (sdelta[ik][i1]>0){
	  mr[j1]+=y[i1][j1];
	}
      }
      mden+=mr[j1];
    }
  }
  for(i1=0;i1<nn;i1++){
    if (sdelta[ik][i1]>0) mi++;
  }
  mden=mden/(mi*mj);
  sp_BLsym_inv(AM[0],BM[0],CM,DM,mi,mj);
  s=mi*mj*(CM[0]+(mj-1)*CM[1]+(mi-1)*DM[0]+(mi-1)*(mj-1)*DM[1]);
  m=mden*s/(s+bk[i][ik]);
  s=s+bk[i][ik];
  if (s>0.00001) s=1/sqrt(s);
  else s=1e6;
  smu[ik]=m;
  //mu[i][ik]=mden;
  //beta
  AM4[0]=1/tau_a[1][ik]+1/ek[1][ik];
  AM4[1]=AM4[0]+1/tau_b[1][ik];
  AM4[2]=1/tau_a[1][ik];
  AM4[3]=AM4[2];
  BM4[0]=0;
  BM4[1]=1/tau_b[1][ik];
  BM4[2]=0;
  BM4[3]=0;
      
  mden-=smu[ik];
  sp_BLsym4_inv(AM4,BM4, CM4,DM4,mi,mj);
  for (i1=0;i1<5;i1++){
    EM4[i1]=mi*CM4[i1]+mi*(mi-1)*DM4[i1];
  }
      
  te=(EM4[2]+EM4[3])/2;

  for (j1=0;j1<pp;j1++){
    if (skapa[ik][j1]>0){
      s=EM4[0];
      mr[j1]=mr[j1]/mi-smu[ik];
       
      m=((EM4[0]-te)*mr[j1]+te*mj*mden); // this si mean * s
      //m=m/s;
      s=s+tau_b[i][ik];
      m=m/s;
      if (s>0.000001) s=1/sqrt(s);
      else s=1e6;
      sbeta[ik][j1]=m;
      //beta[i][ik][j1]=m;
    }
  }
  //alpha
      
  for (i1=0;i1<nn;i1++){
    if (sdelta[ik][i1]>0){
      s=mj*ek[i][ik]+tau_a[i][ik];
      m=0;
      for (j1=0;j1<pp;j1++){
	if (skapa[ik][j1]>0) m+=y[i1][j1]-sbeta[ik][j1]-smu[ik];
      }
      m/=mj; //y1.
      m=m*mj*ek[i][ik];
      m/=s;
      if (s>0.000001) s=1/sqrt(s);
      else s=1e6;
      salpha[ik][i1]=m;
      //m=m/mj;
      //  alpha[i][ik][i1]=m;
    }
  } 
  }
  n1=0;
  bic[1]=0;
  for (i1=0;i1<nn;i1++){
    for (j1=0;j1<pp;j1++){
      if (ym[i1][j1]<1){
	n1++;
	flag=1;
	for (ik=0;ik<nk;ik++){
	  if (sdelta[ik][i1]*skapa[ik][j1]>0){
	    e=y[i1][j1]-smu[ik]-sbeta[ik][j1]-salpha[ik][i1];
	    e=e*e;
	    bic[1]+=0.5*log(ek[1][ik])-0.5*e*ek[1][ik];
	    flag=0;
	  }
	}
	if (flag){ //not in any cluster
	  bic[1]+=0.5*log(tau_e[1])-0.5*y[i1][j1]*y[i1][j1]*tau_e[1];
	}    
      }
    }
  }
  bic[1]+=-0.5*log(2*pi)*n1;
  free(mik);
  free(mjk);
  free(candk);
  free(AM);
  free(BM);
  free(mr);
  free(vr);
  free(vd);
  free(vdk);
  free(ps);
  //  printf("%f\n",bic[1]);
  printf("*** Sampling finished! ***\n");
}

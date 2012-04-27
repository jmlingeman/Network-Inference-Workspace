
/* This k-means clustering code was modified from Cluster 3.0 by
 * Michiel Jan Laurens de Hoon from Human Genome Center, Institute 
 * of Medical Science, University of Tokyo,
 
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
 */



#include "hBBC.h"


static int equal_cluster(int n, int clusterids1[], int clusterids2[]){
  int i;
  for (i=0;i<n;i++) 
    if (clusterids1[i]!=clusterids2[i]) return 0;
  return 1;
}



void getclustermean(int nk,int nn, int pp, float** data, int clusterid[], float** cdata){
  int ik, i,j;
  int *cn;
  cn=malloc(nk*sizeof(int));
  for (ik = 0; ik < nk; ik++){
    for (j = 0; j < pp; j++){
       cdata[ik][j] =0.0;
       cn[ik]=0;
    }
  }
  for (i=0;i<nn;i++){
    ik=clusterid[i];
    for(j=0;j<pp;j++){
      cdata[ik][j]+=data[i][j];
    }
    cn[ik]++;
  }
  for(ik=0;ik<nk;ik++){
    for(j=0;j<pp;j++){
      cdata[ik][j]=cdata[ik][j]/cn[ik];
    }
  }
  free(cn);
  return;
}

void randomassign (int nk, int n, int clusterid[]){
  int i;
  long* map=malloc(n*sizeof(long));
  for (i=0;i<n;i++)    map[i]=i;
  gsl_ran_shuffle(r,map,n,sizeof(int));
  //genprm(map,n);
  for (i=0;i<nk;i++) clusterid[map[i]]=i;
  for(i=nk;i<n;i++) {
    clusterid[map[i]]=(int) gsl_ran_flat(r,0,nk);
    while (clusterid[map[i]]==nk){
      clusterid[map[i]]=(int) gsl_ran_flat(r,0,nk);
    }
  }
  free(map);
  return;
}

float metric (int n, float ** data1, float **data2, int i1, int i2 ){
  
  // correlation
  float result=0, denom1=0, denom2=0, sum1=0, sum2=0;
  int i;

  for (i=0;i<n;i++){
    sum1+=data1[i1][i];
    sum2+=data2[i2][i];
    result+= data1[i1][i]*data2[i2][i];
    denom1+=data1[i1][i]*data1[i1][i];
    denom2+=data2[i2][i]*data2[i2][i];
  }
  result-=sum1*sum2/n;
  denom1-=sum1*sum1/n;
  denom2-=sum2*sum2/n;
  result=result/sqrt(denom1*denom2);
  result=1.0-result;
  return result;
}


void emalg(int nk, int nn, int pp, float **y, int clusterid[], float** cdata){

  int *cn,*savedids ;
  int changed, iteration=0,period=10, i, j,jnow;
  float distance, tdistance;
  cn=malloc(nk*sizeof(int));
  savedids=malloc(nn*sizeof(int));
  for(i=0;i<nk;i++){
    cn[i]=0;
  }
  for(i=0;i<nn;i++){
    cn[clusterid[i]]++;
  }
  do{
    if (iteration%period==0){
      for(i=0;i<nn;i++){
	savedids[i]=clusterid[i];
      }
      period*=2;
    }
    iteration++;
    getclustermean(nk,nn,pp, y, clusterid, cdata);
    changed=0;
    for(i=0;i<nn;i++){
      jnow=clusterid[i];
      if(cn[jnow]==1) continue;
      distance=metric(pp, y, cdata,i,jnow);
      for(j=0;j<nk;j++){
	if (j==jnow) continue;
	tdistance=metric(pp,y,cdata,i,j);
	if (tdistance<distance){
	  distance=tdistance;
	  cn[clusterid[i]]--;
	    clusterid[i]=j;
	    cn[j]++;
	    changed=1;
	}
      }
    }
  } while (changed&&!equal_cluster(nn,savedids,clusterid)&&iteration<1000);
  free(savedids);
  free(cn);
}


void kcluster( int nk,int nn, int pp, float **y,  int clusterid[],  int *ifound, int npass){

  float error;
  float **cdata;
  int *mapping;
  int *tclusterid;
  int i,i1,ipass;  
  double tssin=0.0;
  int same=1;
   
  tclusterid=malloc(nn*sizeof(int));
  mapping=malloc(nk*sizeof(int));
  cdata=malloc(nk*sizeof(float*));
  for (i=0;i<nk;i++){
    cdata[i]=malloc(nn*sizeof(float));
  }
  // set the result of the first pas as the initial best clustering solution.
  
  randomassign(nk,nn, clusterid);
  //malloc cdata
  emalg(nk,nn,pp, y, clusterid, cdata);
  *ifound=1;
  error=0.0;
  for (i1=0;i1<nn;i1++){
    int j=clusterid[i1];
    error+=metric(pp, y, cdata, i1, j); // why??????
  }
  if (npass==0){
    for (i=0;i<nk;i++){
      //	free(cdata[i]);
    }
    //free(cdata);
    return;
  }
  for (ipass=1;ipass<npass;ipass++){
    randomassign(nk,nn, tclusterid);
    emalg(nk,nn,pp, y, tclusterid, cdata);
    for(i=0;i<nk;i++) mapping[i]=-1;
    for(i=0;i<nn;i++){
      int j=tclusterid[i];
      if(mapping[j]==-1) mapping[j]=clusterid[i];
      else if (mapping[j]!=clusterid[i]) same=0;
      tssin+=metric(pp, y, cdata, i,j);
    }
    if(same) (*ifound)++;
    else if (tssin<error){
      *ifound=1;
      error=tssin;
      for (i=0;i<nn;i++) clusterid[i]=tclusterid[i];
    }
  }
  //deallocate
 
  //free(mapping);
  //free (tclusterid);

  for (i=0;i<nk;i++){
    //free(cdata[i]);
   }
  return;
}

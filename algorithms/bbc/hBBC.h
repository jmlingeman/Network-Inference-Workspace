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


#include <malloc.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include <gsl/gsl_vector.h>
#include <gsl/gsl_permutation.h>
#include <gsl/gsl_sort_vector.h>
#include <gsl/gsl_sort_double.h>
#include <math.h>
#include <string.h>
#define Ns 6000
#define Nss 2
#define S 10
#define pi 3.1415926
#define NFS 2000


//float mynorm(float a, float b);
//float mygamma (float a, float b);
//int mybernoulli (float p);
void rowstd(int nn, int pp, float **y, char **ym);
void ref_initial(int nk, int ***delta,int ***kapa);
void dataY_noheader(int nn, int pp, float **y,char **ym,char** genes, char **samples, char fname[],int lt);
void dataY(int nn, int pp, float **Y, char **genes, char **samples,char fname[]);
void dataY_yeast(int nn, int pp, float **Y, char **ym, char **genes, char **samples,char fname[], int lt);
//void dataY1(float **Y, char **genes, char **samples,char fname[]);
int getsize(char *fname,int *nn,int *pp);

void gibbssample(char fout[],int nk,int nn, int pp, float **y,char **ym,float **bk,float **tau_a,float**tau_b,float *tau_e, float **ek, float *bk_a, float *bk_b, float *taua_a,float *taua_b,float* taub_a,float *taub_b, float *ek_a, float *ek_b, float taue_a, float taue_b,float *qq,int ***delta, int ***kapa, int **sdelta, int **skapa, float *smu, float** salpha, float** sbeta, float *bic);

void output(int nn, int pp, char fout[],int nk,int **sdelta, int **skapa, float *smu, float **salpha, float **sbeta, float* bic, char **genes,char** samples);


void initial(int nk,float **y,float *bk, float **tau_a, float **tau_b, float*tau_e, float *a_a, float *b_a,float *a_b,float* b_b,
	     float *pp,float*qq, float *mu0, float **mu, float ***alpha,float ***beta,int ***delta, int ***kapa, 
	     float*smu0, float *smu, int **sdelta,int **skapa, float **salpha, float **sbeta);

//int pisa(int nk,float **y,int n,int p,float **mu,int ***delta,int ***kapa,float ***alpha,float ***beta,double tau_c);
int isa(int sd,int nk,float **y,int n,int p,int ***delta,int ***kapa);

void kcluster(int nk, int nn, int pp, float **y,  int clusterid[],  int *ifound, int npass);

void colstd(int nn, int pp, float **y, char **ym);
void norm_iqr(int nn, int pp, float **y, char **ym, float *normrange);
void norm_sqr(int nn, int pp, float **y, char **ym, float *normrange);


extern gsl_rng *r; 
extern gsl_rng_type* T;

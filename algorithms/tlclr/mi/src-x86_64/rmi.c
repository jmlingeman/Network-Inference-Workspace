#include <stdio.h>
#include <stdlib.h>
#include <R.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>

#include "matrix.h"
#include "mi.h"

int debug = 0;

/*
 Rmi.C
 --------------------------------------------------------------------------------------------------
 
 This file contains all the functions necessary for interfacing the C functions with R. For more 
 information regarding this topic as well as information on creating R packages, please see the README
 file included  in this package as well as the CRAN manual on this subject.
 
 The R_init_rclr() method is automatically called when the clr library is loaded. It registers the 
 C methods that are to be callable from the R console or an R script.
 
 The Rmi() and Rz() methods are ones that are called directly from R and send data to the appropriate 
 C methods for computation. They are discussed in further detail in the README file.
 
 For debugging purposes, the global variable debug has been included. Since this file should never be 
 used when running C code from the command line, all print methods are Rprintf()
 
 -------------------------------------------------------------------------------------------------
 
 */

/*-----------Rmi()----------------

Input: float *m
float *z
int *numVar
int *numSamp
int *n
int *k
int *g

Output: void

Notes: Fucntion for interfacing mutual information
calculation with R. See details above.

-----------------------------------*/

void Rmi(float *m, float *z, int *numVar, int *numSamp, int *n, int *k, int *g) {
	
	matrix *d;
	d = new_matrix(*numVar, *numSamp);
	
	int i,j;
	
	//Transfer data from m to a matrix
	if (debug)
		Rprintf("Moving data into a matrix...\n");
	for (i = 0; i < d->rows; ++i) {
		for (j = 0; j < d->cols; ++j) {
			d->m[i][j] = m[i * d->cols + j];
		}
	}
	
	//Calculate mutual information
	if (debug)
		Rprintf("Beginning mutual information calculation...\n");
	matrix *w;
	w = mi(d, *n, *k, *g);
	if (debug)
		Rprintf("Done!\n");
	

	//Move data into z for use in R
	if (debug)
		Rprintf("Moving data back..\n");
	for (i = 0; i < w->rows; ++i) {
		for (j = 0; j < w->cols; ++j) {
			z[i * w->cols + j] = w->m[i][j];
		}
	}
	
	free_matrix(d);
	free_matrix(w);
	
}


//This array is for registering functions for use in R
//The type of array specifies that these functions are called using the R command .C()
//Information regarding .C() can be found on the web and in the R help pages

static const  R_CMethodDef CMethods[] = {
	{"Rmi", (DL_FUNC) &Rmi, 7},
	{NULL, NULL, 0}
};


/*-----------R_init_clr()----------------

Input: DllInfo *info

Output: void

Notes: Called by the R function .First.lib()
Used for setting up the library when library() 
is called.

-----------------------------------*/

void R_init_clr(DllInfo *info) {
	// params:  -info: where are stores info about rutines
	//			`.C'            `R_CMethodDef'
	//			`.Call'         `R_CallMethodDef'
	//			`.Fortran'      `R_FortranMethodDef'
	//			`.External'     `R_ExternalMethodDef'
	
	R_registerRoutines(info, CMethods, NULL, NULL, NULL);
	
}




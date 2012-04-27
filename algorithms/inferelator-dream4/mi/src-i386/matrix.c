#include <stdio.h>
#include <stdlib.h>

#include "matrix.h"


/*
MATRIX.C
-------------------------------------------------------------------------------------------------------

This file contains the methods for use and manipulation of the matrix structure (defined in matrix.h). 
The matrix structure's primary purpose is to make the manipulation of 2-dimensional data cleaner rather than
having to use messy indexes into an array as is done in the original Matlab implementation of clr (from the 
plos paper). Most things can be done with the matrices using the '->' syntax to access and change the
internal variables. However, a few functions are provided in this file to help with the data structure.

The new_matrix() and free_matrix() functions are the most useful and should be used often. The new_matrix()
function will allocate the appropriate memory space for a matrix of m x n dimensions and return a pointer to
that memory space. The free_matrix() function will clean-up any matrices that are no longer needed by freeing
all the memory space used by them.

The get_matrix() and print_matrix() functions are meant for debugging of the C code when not using an R 
interface. The get_matrix() function allows the user to construct and fill a matrix from the command line
while the program is running. The print_matrix() function will print out all the elements of the matrix one
at a time.

The copy_matrix() function is used during other parts of the clr algorithm to allow ease of copying data from
one matrix to another. The function will also ensure that all elements of a matrix have a real value contained 
in them. However, the function will not work if the dimensions of the matrix to be copied are bigger than the 
dimensions of the target matrix.

-------------------------------------------------------------------------------------------------------- 

*/

/*-----------new_matrix()----------------

Input: int numrows
       int numcols

Output: matrix *

Notes: Returns pointer to a new matrix of 
       dimensions numrows by numcols.

  -----------------------------------*/

matrix *new_matrix(int numrows, int numcols) {

  double **tmp;
  tmp = (double **)malloc(numrows * sizeof(double *));

  int i;
  for (i = 0; i < numrows; ++i) {
    tmp[i] = (double *)malloc(numcols * sizeof(double));
  }

  matrix *m;
  m = (matrix *)malloc(sizeof(matrix));
  m->m = tmp;
  m->rows = numrows;
  m->cols = numcols;

  return m;

}


/*-----------free_matrix()----------------

Input: matrix *m

Output: void

Notes: Frees memory used in storage of matrix m.

  -----------------------------------*/

void free_matrix(matrix *m) {

  int i;

  for (i = 0; i < m->rows; ++i) {
    free(m->m[i]);
  }

  free(m->m);
  free(m);

  return;

}


/*-----------get_matrix()----------------

Input:

Output: matrix *

Notes: Assembles and returns pointer to
       new matrix. Uses user input to get 
       dimensions and elements of array.
       Only useful when testing algorithm 
       outside of Matlab and R on very small
       matrices. Can be used for debugging purposses
       when running C code from the command line.

  -----------------------------------*/

matrix *get_matrix(){

  int row, col;
  printf("Please give dimensions of new array:\n");
  scanf("%d %d", &row, &col);

  matrix *m;
  m = new_matrix(row, col);

  int i,j;
  double k;
  printf("Please input values into matrix\n");
  for (i = 0; i < row; ++i) {
    for (j = 0; j <  col; ++j) {
      scanf("%lf", &k);
      m->m[i][j] = k;
    }
  }

  return m;

}


/*-----------print_matrix()----------------

Input: matrix *m

Output: void

Notes: Prints out elements of matrix m.
       Transverses across rows and prints
       one element per line. Used for C code
       debugging from the command line. For all 
       other debugging purposes, use Rprintf().

  -----------------------------------*/

void print_matrix(matrix *m){

  int i,j;
  for (i = 0; i < m->rows; ++i) {
    for (j = 0; j < m->cols; ++j) {
      printf("%lf\n", m->m[i][j]);
    }
  }

}


/*-----------copy_matrix()----------------

Input: matrix *a
       matrix *b

Output: void

Notes: Copies data in matrix a into matrix b.
       If dimensions of a are greater than 
       dimensions of b, does nothing.
       If dimenstions of a are less than
       dimensions of b, fills the remainder of
       b with 0's.

  -----------------------------------*/

void copy_matrix(matrix *a, matrix *b) {
  if (a->rows > b->rows || a->cols > b->cols)
    return;

  int i,j;
  for (i = 0; i < b->rows; ++i) {
    for (j = 0; j < b->cols; ++j) {
      if (i < a->rows && j < a->cols) 
	b->m[i][j] = a->m[i][j];
      else
	b->m[i][j] = 0;
    }
  }

  return;

}

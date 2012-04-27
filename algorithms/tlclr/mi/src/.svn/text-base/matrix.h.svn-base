#ifndef MATRIX_H
#define MATRIX_H

//This is the matrix structure
//The typedef declaration allows the structure to be called without the keyword 'struct'
typedef struct matrix {

  int rows; //number of rows in matrix
  int cols; //number of columns in matrix
  double **m; //actual data

} matrix;

matrix *new_matrix(int numrows, int numcols);

void free_matrix(matrix *m);

matrix *get_matrix();

void print_matrix(matrix *m);

void copy_matrix(matrix *a, matrix *b);

#endif

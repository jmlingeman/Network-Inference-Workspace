#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "matrix.h"
#include "mi.h"

/*------------- log2() ---------------

Inputs: double x 

Output: double 

Notes: computes log base 2 of x

-------------------------------------*/

double log2(double x) {
	return log(x) / log(2);
}


/*------------- max() ---------------

Inputs: matrix *m
int i

Output: double

Notes: finds largest value contained in row i of matrix m

-------------------------------------*/


double max(matrix *m, int i) {
	
	double t = m->m[i][0];
	int j;
	
	//for (i = 0; i < m->rows; ++i) {
	for (j  = 0; j < m->cols; ++j) {
      if (m->m[i][j] > t)
			t = m->m[i][j];
	}
	
	//}
	return t;

}


/*------------- min() ---------------

Inputs: matrix *m
int i

Output: double

Notes: finds smallest value contained in row i of matrix m

-------------------------------------*/

double min(matrix *m, int i) {
	
	double t = m->m[i][0];
	int j;
	
	//for (i = 0; i < m->rows; ++i) {
	for (j  = 0; j < m->cols; ++j) {
      if (m->m[i][j] < t)
			t = m->m[i][j];
	}
	//}
	
return t;

}


/*------------- calcKnot() ---------------

Inputs: int *knots [empty knot vector]
int n [number of bins]
int k [spline order]

Output: void

Notes: calculates knot vector for given bin number and spline order
places results into knots

-------------------------------------*/

void calcKnot(int *knots, int n, int k) {
	
	int i;
	
	for (i = 0; i < n + k + 1; ++i) {
		
		if (i < k)
			knots[i] = 0;
		
		else if (i <= n - 1)
			knots[i] = i - k + 1;
      //knots[i] = knots[i-1] + 1;
		
		else
			knots[i] = n - 1 - k + 2;
      //knots[i] = knots[n - 1] + 1;
	}
	
}


/*------------- convData() ---------------

Inputs: matrix *m [data matrix]
int n [number of bins]
int k [spline order]

Output: matrix *

Notes: converts data matrix so all values lie
in range of BSpline functions
returns matrix of converted data

-------------------------------------*/

matrix *convData(matrix *m, int n, int k) {
	
	matrix *z;
	z = new_matrix(m->rows, m->cols);
	int i,j;
	double xmin, xmax;
	
	for (i = 0; i < m->rows; ++i) {
		xmax = max(m, i);
		xmin = min(m, i);
		for (j = 0; j < m->cols; ++j) {
			z->m[i][j] = (m->m[i][j] - xmin) * (n - k + 1) / (xmax - xmin);
			if (z->m[i][j] < 0 || z->m[i][j] > n - k + 1)
				printf("z out of range\n");
		}
	}
	
	return z;
	
}


/*------------- BSpline() ---------------

Inputs: int t [current bin]
int k [spline order]
double z [data value]
int *knots [knot vector]

Output: double

Notes: calculates value of BSpline function value
for given value z

-------------------------------------*/

double BSpline(int t, int k, double z, int *knots, int n) {
	
	if (k == 1) {
		if ((z >= knots[t] && z < knots[t + 1]) || (fabs(z - knots[t + 1]) < 1e-10 && t + 1 == n))
			return 1;
		else
			return 0;
	}
	
	double d1, d2;
	double value;
	d1 = knots[t + k -1] - knots[t];
	d2 = knots[t + k] - knots[t + 1];
	
	if (d1 == 0 && d2 == 0)
		return 0;
	
	if (d1 == 0)
		value = ((knots[t + k] - z) / d2) * BSpline(t+1, k-1, z, knots, n);
	
	else if (d2 == 0)
		value =  ((z - knots[t]) / d1) * BSpline(t, k-1, z, knots, n);
	
	else
		value =  ((z - knots[t]) / d1) * BSpline(t, k-1, z, knots, n) + ((knots[t + k] - z) / d2) * BSpline(t+1, k-1, z, knots, n);
	
	if (value < 0)
		return 0;
	
	return value;
	
}


/*------------- calcWeights() ---------------

Inputs: matrix *m [data matrix]
int n [number of bins]
int k [spline order]
int *knots [knot vector]

Output: matrix *

Notes: Calculates bin probabilities for each variable
in data matrix
retuns matrix of dimensions numVars x numBins

-------------------------------------*/

matrix *calcWeights(matrix *m, int n, int k, int *knots) {
	
	//double w[m->rows][m->cols][n];
	matrix *w;
	matrix *z;
	
	w = new_matrix(m->rows, n * m->cols);
	z =convData(m, n, k);
	
	int i, j, t;
	
	for (i = 0; i < m->rows; ++i) {
		for (t = 0; t < n; ++t) {
			for (j = 0; j < m->cols; ++j) {
				w->m[i][t * m->cols + j] = BSpline(t, k, z->m[i][j], knots, n);
				//if (w->m[i][t*n + j] > 1)
				//printf("bad weight %lf\n", w->m[i][t*n +j]); 
			}
		}
	} 
	
	/*matrix *p;
	p = new_matrix(m->rows, n);
	//printf("p: %d, %d\n", p->rows, p->cols);
	
	double sum;
	
	for (i = 0; i < m->rows; ++i) {
		for (t = 0; t < n; ++t) {
			sum = 0;
			for (j = 0; j < m->cols; ++j) {
				sum += w[i][j][t];
			}
			sum /= n;
			p->m[i][t] = sum;
		}
	}
	*/
	//printf("p2: %d, %d\n", p->rows, p->cols);
	
	free_matrix(z);
	
	return w;
	
}


/*------------- combP() ---------------

Inputs: matrix *p [probability matrix]
int x [row 1]
int y [row 2]

Output: matrix *

Notes: computes combined probabilities of all cases
between rows x and y of the probability matrix

-------------------------------------*/

matrix * combP(matrix *p, int x, int y) {
	
	matrix *p2;
	p2 = new_matrix(p->cols, p->cols);
	
	int i, j;
	
	for (i = 0; i < p->cols; ++i) {
		for (j = 0; j < p->cols; ++j) {
			p2->m[i][j] = p->m[x][i] + p->m[y][j] /*- p->m[x][i] * p->m[y][j]*/;
		}
	}
	
	return p2;
	
}

matrix *calcP1(matrix *w, int numSams, int n) {
	
	matrix *p;
	p = new_matrix(w->rows, n);
	
	int i,j,t;
	double sum;
	
	for (i = 0; i < w->rows; ++i) {
		for (t = 0; t < n; ++t) {
			sum = 0;
			for (j = 0; j < numSams; ++j) {
				sum += w->m[i][t * numSams + j]/numSams;
			}
			//printf("%lf\n", sum);
			p->m[i][t] = sum;
			if (p->m[i][t] < 0 || p->m[i][t] > 1)
				printf("bad probability %lf\n", p->m[i][t]);
		}
	}
	
	return p;
}

matrix *calcP2(matrix *w, int numSams, int n, int x, int y) {
	
	matrix *p;
	p = new_matrix(n, n);
	
	
	int i,j,t;
	double sum;
	
	for (i = 0; i < n; ++i) {
		for (j = 0; j < n; ++j) {
			sum = 0;
			for (t = 0; t < numSams; ++t) {
				sum += w->m[x][i * numSams + t] * w->m[y][j * numSams + t]/numSams;
			}
			p->m[i][j] = sum;
			if (p->m[i][j] < 0 || p->m[i][j] > 1)
				printf("bad probability p2 %lf\n", p->m[i][j]);
		}
	}
	
	return p;
	
}


/*------------- entropy() ---------------

Inputs:  matrix *p [probability matrix]

Output: matrix *

Notes: computes entropy for each row in matrix p

-------------------------------------*/

matrix *entropy(matrix *w, int numSams, int n) {
	
	//printf("p2: %d, %d\n", p->rows, p->cols);
	matrix *e;
	e = new_matrix(w->rows, 1);
	
	matrix *p;
	p = calcP1(w, numSams, n);
	
	double sum;
	int i,j;
	
	for (i = 0; i < p->rows; ++i) {
		sum = 0;
		for (j = 0; j < p->cols; ++j) {
			if (p->m[i][j] > 0.0000001)
				sum += p->m[i][j] * log2(p->m[i][j]);
		}
		e->m[i][0] = -sum;
		//printf("%lf\n", e->m[i][0]);
	}
	
	free_matrix(p);
	
	return e;
	
}


/*------------- entropy2() ---------------

Inputs: matrix *p

Output: matrix *

Notes: Computes entropy between every combination of rows
in matrix p

-------------------------------------*/

matrix *entropy2(matrix *w, int numSams, int n, int g) {
	
	matrix *e;
	e = new_matrix(w->rows, w->rows);
	
	matrix *p;
	
	int i, j, t, s;
	double sum;
	
//	for (i = 0; i < w->rows; ++i) {
//		for (j = 0; j < w->rows; ++j) {
//			p = calcP2(w, numSams, n, i, j);
//			sum = 0;
//			for (t = 0; t < p->rows; ++t) {
//				for (s = 0; s < p->cols; ++s) {
//					if (p->m[t][s] > 0.0000001)
//						sum += p->m[t][s] * log2(p->m[t][s]);
//				}
//			}
//			e->m[i][j] = -sum;
//			//printf("%lf\n", e->m[i][j]);
//			free_matrix(p);
//		}
//	}
	
	for (i = 0; i < g; ++i) {
		for (j = g; j < w->rows; ++j) {
			p = calcP2(w, numSams, n, i, j);
			sum = 0;
			for (t = 0; t < p->rows; ++t) {
				for (s = 0; s < p->cols; ++s) {
					if (p->m[t][s] > 0.0000001)
						sum += p->m[t][s] * log2(p->m[t][s]);
				}
			}
			e->m[i][j] = -sum;
			//printf("%lf\n", e->m[i][j]);
			free_matrix(p);
		}
	}
	
	return e;
	
}

double minE(matrix *e) {
	double t = e->m[0][0];
	int i,j;
	
	for (i = 0; i < e->rows; ++i) {
		for (j = 0; j < e->cols; ++j) {
			if (e->m[i][j] < t)
				t = e->m[i][j];
		}
	}
	
	return t;
}

matrix *minEnorm(matrix *mi, matrix *e) {
	
	double t = minE(e);
	
	matrix *z;
	z = new_matrix(mi->rows, mi->cols);
	
	int i,j;
	
	for (i = 0; i < mi->rows; ++i) {
		for (j = 0; j < mi->cols; ++j) {
			z->m[i][j] = mi->m[i][j] / t;
		}
	}
	
	return z;
}


/*------------- mi() ---------------

Inputs: matrix *m [data matrix]
int n [number of bins]
int k [spline order]
int g [number of non-predictors]

Output: matrix *

Notes: computes mutual information between all non-predictors (rows 0,...,g-1) 
and predictors (rows g,...,m->rows)
contained in data matrix

-------------------------------------*/

matrix *mi(matrix *m, int n, int k, int g) {
	
	//printf("before mi\n");
	matrix *mi;
	double r[g*((m->rows)-g)];
	
//AM:
	//	mi = new_matrix(m->rows, m->rows);
	mi = new_matrix(g, ((m->rows)-g));
//AM: this is the returned matrix mi: 
	//mi[i][j] is the mi between response vec m[i][] i=0,...,g-1 
	//and predictor vec m[j][] where j=g,...,m->rows 
	
	int *knots;
	knots = (int *)malloc((n + k + 1) * sizeof(int));
	calcKnot(knots, n, k);
	/*int l;
	for (l = 0; l < n + k + 1; ++l)
		printf("%d\n", knots[l]);
	*/
	//printf("before p\n");
	matrix *w;
	w = calcWeights(m, n, k, knots);
//	print_matrix(w);
//	printf("w1: %d, %d\n", w->rows, w->cols);
	//printf("p1: %d, %d\n", p->rows, p->cols);
	//printf("before e\n"); //here
	matrix *e;
	e = entropy(w, m->cols, n);
//	print_matrix(e);
	
	//printf("before e2\n");
	matrix *e2;
	e2 = entropy2(w, m->cols, n, g);
//	print_matrix(e2);
	
	int i,j;

//AM:
	//for (i = 0; i < mi->rows; ++i) {
	for (i = 0; i < g; ++i) {
		for (j = g; j < (m->rows) ; ++j) {
			mi->m[i][j-g] = e->m[i][0] + e->m[j][0] - e2->m[i][j];
//			Rprintf("[i,j,mi] = %d %d %lf\n", i, j, mi->m[i][j]);
		}
	}
	//}
//	print_matrix(mi);
	free(knots);
	free_matrix(w);
	free_matrix(e);
	free_matrix(e2);


	return mi;
	
}

//int main() {
//	
//	matrix *m;
//	m = get_matrix();
//	
//	int n,k;
//	printf("Please give number of bins and spline order\n");
//	scanf("%d %d", &n, &k);
//	
//	//printf("n: %d, k: %d\n", n, k);
//	//print_matrix(m);
//	
//	matrix *i;
//	i = mi(m, n, k);
//	
//	print_matrix(i);
//	
//	free_matrix(m);
//	free_matrix(i);
//	
//	return 1;
//	
//}

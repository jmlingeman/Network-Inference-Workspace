#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "zlib.h"

#define SEPARATOR ","
#define NEW_LINE "\n"
#define BLOCK_SIZE 1024*8

long sizeArray(char*, int*, int*);
void ReadFile(char*, double**, int*, int*);

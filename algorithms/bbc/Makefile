CFLAGS= -lgsl -lgslcblas -lm
CPPFLAGS= -Wall -g 
CC=gcc
OBJS=	mainBBC.o \
	func_BBC.o\
	kmeans.o\



EXEC=BBC

$(EXEC): 	$(OBJS)
	$(CC) $(CPPFLAGS) $(OBJS) $(CFLAGS) -o $(EXEC)
	@rm -rf *.o

mainBBC.o:	mainBBC.c 
	$(CC) -c $(CPPFLAGS) $< hBBC.h

func_BBC.o:	func_BBC.c 
	$(CC) -c $(CPPFLAGS) $< hBBC.h  
kmeans.o: kmeans.c 
	$(CC) -c $(CPPFLAGS) $< hBBC.h  
 



clean:	
	@rm -rf *.o $(EXEC)

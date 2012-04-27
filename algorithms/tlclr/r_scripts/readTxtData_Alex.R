##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '
## May 2010 Dream3/4 pipeline (MCZ,tlCLR,Inferelator)
## Bonneau lab - "Aviv Madar" <am2654@nyu.edu>,
##  		     "Alex Greenfield" <ag1868@nyu.edu>
## NYU - Center for Genomics and Systems Biology
##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

# following is an example of how to create the data structures necessary for
# running TL-CLR and Inferelator. Note that the functions called below
# will work only for data formatted as the data for the DREAM challenges is formatted
# All of the data is located in the exampleRawData directory

rm(list=ls())
source("r_scripts/readTxtDataUtil.R")
#we first create the data for the DREAM4 network of 10 genes

#we create ratios, the table of all data

dirPath <- "exampleRawData/DREAM4_in-silico_challenge/Size_10/insilico_size10_1/"
chalName <- "DREAM4"
ratios <- makeRatios(chalName,dirPath)

#we now create colMap
numEq_exp <- 21 #the number of non-time-series experiments
del.t <- 50 #the time interval between time-series expirements
colMap <- makeColMap(ratios, numEq_exp, del.t, chalName)

#we now make clusterSatck
clusterStack <- makeClusterStack(ratios)

#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
#creating these data structures for the remaining datasets is very similar
#now we show this process for the DREAM4 network of 100 genes
#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
dirPath <- "exampleRawData/DREAM4_in-silico_challenge/Size_100/insilico_size100_1/"
chalName <- "DREAM4"
ratios <- makeRatios(chalName,dirPath)

#we now create colMap
numEq_exp <- 201 #the number of non-time-series experiments
del.t <- 50 #the time interval between time-series expirements
colMap <- makeColMap(ratios, numEq_exp, del.t, chalName)

#we now make clusterSatck
clusterStack <- makeClusterStack(ratios)

#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
#and now for the DREAM3 network
#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
dirPath <- dirPath <- "exampleRawData/DREAM3_in-silico_challenge/Size100/network1/"
chalName <- "DREAM3"
ratios <- makeRatios(chalName,dirPath)

#we now create colMap
numEq_exp <- 201 #the number of non-time-series experiments
del.t <- 10 #the time interval between time-series expirements
colMap <- makeColMap(ratios, numEq_exp, del.t, chalName)

#we now make clusterSatck
clusterStack <- makeClusterStack(ratios)

#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
#and finally for the DREAM2 network 1
#-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
dirPath <- "exampleRawData/DREAM2_in-silico_challenge/network_1/"
chalName <- "DREAM2"
ratios <- makeRatios(chalName,dirPath)

#we now create colMap
numEq_exp <- 101 #the number of non-time-series experiments
del.t <- 20 #the time interval between time-series expirements
colMap <- makeColMap(ratios, numEq_exp, del.t, chalName)

#we now make clusterSatck
clusterStack <- makeClusterStack(ratios)


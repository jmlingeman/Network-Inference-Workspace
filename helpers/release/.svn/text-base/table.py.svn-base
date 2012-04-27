################################################################################
# Filename: table.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Implements the functionality for reading a file into memory.
################################################################################

import copy

################################################################################
# Class: table_data
# Description: Implements the functionality for reading a file into memory
################################################################################
# Constructor inputs:
#         filename (path of the file to be read) required
#         delimiter (string seperating the values in the file) default = ","
#         header (boolean indicating presence of header) default = True
################################################################################

class table_data(object):
    def __init__(self, filename, delimiter = ",", header = False):
        
        # Names of the columns
        self.columns = [] 

        # Temporary storage, rowwise
        rowwise_data = []

        # Read the file in rowwise_data. Open given file, read it line by line, 
        # split  each line at delimiter to  get tokens, strip space and  newline
        # from each token,  append resulting tokens in  a list, append the list 
        # in  rowwise_data  and finally, close the file.
        curr_file = open(filename, 'r')
        for line in curr_file:
            curr_row = []
            words = line.split(delimiter)
            for word in words:
                curr_row.append(word.strip(" \n"))                
            rowwise_data.append(curr_row)
        curr_file.close()

        # Get the number of rows and columns
        rows = len(rowwise_data)
        columns = len(rowwise_data[0])

        # Temporary storage, columnwise
        columnwise_data = []

        # Convert data from rows to columns 
        for column in xrange(0, columns):
            curr_column = []
            for row in xrange(0, rows):
                try:
                    curr_column.append(float(rowwise_data[row][column]))
                except:
                    if row == 0 and header:
                        curr_column.append(rowwise_data[row][column])

            columnwise_data.append(curr_column)

        column_names = []

        if header:
            for column in xrange(0, columns):
                column_name = columnwise_data[column][0]
                columnwise_data[column] = columnwise_data[column][1:]
                column_names.append(column_name)
        else:
            for column in xrange(0, columns):
                column_name = "C" + str(column)
                column_names.append(column_name)

        for column in xrange(0, columns):
            curr_column_name = column_names[column]
            curr_column_value = columnwise_data[column]
            self.__dict__[curr_column_name] = copy.copy(curr_column_value)
            self.columns = copy.copy(column_names)

################################################################################
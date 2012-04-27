################################################################################
# Filename: table.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Implements the functionality for dumping measures into a file
################################################################################


################################################################################
# Function: table_dump
# Description: Writes computed measures to a file
# Inputs: 
#             filename (required)
#             measure1 measure2 ... to be written
# Outputs:
#             None
################################################################################
def table_dump(output_filename, *args):
    output_file = open(output_filename, 'w')
    
    for position in xrange(0, len(args[0])):
        output_string = ""
        for list in args:            
            output_string += str(list[position])
            output_string += ","
        output_string = output_string[:len(output_string) - 1] + "\n"
        output_file.write(output_string)
    
    output_file.close()
    
################################################################################
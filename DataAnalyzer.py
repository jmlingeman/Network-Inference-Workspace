,!/bin/python

# This class takes a microarray class as an argument and will analyze qualities of it
# TODO I want to do stuff here
def DataAnalyzer(microarray):
   experiments = microarray.experiments
   # First test to see how many genes vs experiments we have
   def test_size(experiments):
     size_genes = len(experiments[0])
     size_experiments = len(experiments)

     if size_genes == size_experiments:
        pass
     else if size_genes > size_experiments:
        pass
     else:
        pass

     # TODO: Create suggested workflow based on these rules?  Including parameter finding?

import sys
import os
os.environ["gene_path"] = "/Users/jesse/Workspace/School/MastersThesis/Program/"

def get_immediate_subdirectories(dir):
    return [dir + name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
sys.path.append(os.environ["gene_path"])
sys.path += get_immediate_subdirectories(os.environ["gene_path"])
print sys.path
# Algorithm we are wrapping in a VisTrail
from mcz import *

# Vistrails imports.  These will only be available when VisTrails is running
import core.modules.module_registry
from core.modules.vistrails_module import Module, ModuleError

version = "0.1"
name = "Median Corrected ZScores"
identifier = "edu.nyu.sci.gene.dfg4grn"

class MCZ_vt(Module):
    def compute(self):
        # Read everything in from the configuration file, set up a new DFG4GRN
        # instance.  This should probably be a config generated from a previous
        # run of DFG4GRN, since that will contain all of the defaults as well
        # as all of the modified parameters.

        knockout_file = self.getInputFromPort("knockout_file")
        wildtype_file = self.getInputFromPort("wildtype_file")

        mcz = MCZ()
        zscores = mcz.run(knockout_file, wildtype_file, "vistrails-run")

        self.setResult("zscores", zscores)




def initialize( *args, **keywords ):

    reg = core.modules.module_registry.registry
    reg.add_module(MCZ_vt)

    # Define the inputs.  In this case, just the config file that stores all of
    # our inputs
    reg.add_input_port(MCZ_vt, \
            "knockout_file", (core.modules.basic_modules.String, "Path to knockout_file"))
    reg.add_input_port(MCZ_vt, \
            "wildtype_file", (core.modules.basic_modules.String, "Path to wildtype file"))

    reg.add_output_port(MCZ_vt, \
            "zscores", (core.modules.basic_modules.List, "Output list of zscores"))


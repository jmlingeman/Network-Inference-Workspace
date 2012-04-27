import sys
import os

def get_immediate_subdirectories(dir):
    return [dir + name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
sys.path.append("/Users/Jesse/Workspace/School/MastersThesis/Program/")
sys.path += get_immediate_subdirectories("/Users/Jesse/Workspace/School/MastersThesis/Program/")
print sys.path
# Algorithm we are wrapping in a VisTrail
from dfg4grn import *

# Vistrails imports.  These will only be available when VisTrails is running
import core.modules.module_registry
from core.modules.vistrails_module import Module, ModuleError

version = "0.1"
name = "DFG4GRN"
identifier = "edu.nyu.sci.gene.dfg4grn"

class DFG4GRN_vt(Module):
    def compute(self):
        # Read everything in from the configuration file, set up a new DFG4GRN
        # instance.  This should probably be a config generated from a previous
        # run of DFG4GRN, since that will contain all of the defaults as well
        # as all of the modified parameters.

        config_file = self.getInputFromPort("config_file")

        tau = self.getInputFromPort("tau")
        lambda_w = self.getInputFromPort("lambda_w")
        eta_z = self.getInputFromPort("eta_z")

        dfg = DFG4GRN()
        dfg.run_vt(config_file, tau, lambda_w, eta_z)



def initialize( *args, **keywords ):

    reg = core.modules.module_registry.registry
    reg.add_module(DFG4GRN_vt)

    # Define the inputs.  In this case, just the config file that stores all of
    # our inputs
    reg.add_input_port(DFG4GRN_vt, \
            "config_file",(core.modules.basic_modules.String, "Configuration \
                file that contains all of the settings for this algorithm."))
    reg.add_input_port(DFG4GRN_vt, \
            "tau", (core.modules.basic_modules.Float, "Time weight parameter"))
    reg.add_input_port(DFG4GRN_vt, \
            "lambda_w", (core.modules.basic_modules.Float, ""))
    reg.add_input_port(DFG4GRN_vt, \
            "eta_z", (core.modules.basic_modules.Float, ""))



    # Define our outputs from this algorithm
    reg.add_output_port(DFG4GRN_vt, "percent_err", \
        (core.modules.basic_modules.Float, "Percent error on final point"))
    reg.add_output_port(DFG4GRN_vt, "network_file", \
        (core.modules.basic_modules.String, "The inferred network"))



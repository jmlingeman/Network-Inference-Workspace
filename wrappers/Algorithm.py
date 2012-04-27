#!/bin/python

import sys
import os
def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]
sys.path += get_immediate_subdirectories("../")
sys.path += "../"
import time
import subprocess
import shlex
from DataStore import *
from WriteConfig import *
from Network import *
from shutil import copytree
from datetime import datetime
from JobManager import *
import os
import gzip
import numpy
import scipy.io
import copy
from ReadConfig import *
from ReadData import *
from datetime import datetime
from shutil import copytree

global settings

class Algorithm:
    name = ""
    input_files = []
    output_files = []
    cmd_str = ""
    cwd = ""

    def run_cluster(self, pbs_server, job_script, settings):

        import pbs
        from threading import threa
        self.settings = copy.deepcopy(settings)
        # Launch script, wait for output to come back, return when it does

        # Create the job options struct
        attropl = pbs.new_attropl(4)

        # Set the name of the job
        #
        attropl[0].name  = pbs.ATTR_N
        attropl[0].value = "inferno_" + self.name

        # Job is Rerunable
        #
        attropl[1].name  = pbs.ATTR_r
        attropl[1].value = 'y'

        # Walltime
        #
        attropl[2].name  = pbs.ATTR_l
        attropl[2].resource = 'walltime'
        attropl[2].value = '400'

        # Nodes
        #
        attropl[3].name  = pbs.ATTR_l
        attropl[3].resource = 'nodes'
        attropl[3].value = '1:ppn=4'

        # Run the job
        if pbs_server == None:
            pbs_server = pbs.pbs_default()
        job_id = pbs.pbs_submit(pbs_server, attropl, job_script, 'NULL', 'NULL')

        e, e_txt = pbs.error()
        if e:
            print e,e_txt

        # Save the job ID for later so we can check on the status
        self.job_id = job_id

        # TODO: Change this
        # Now loop, checking every 5 seconds or so if the job is done by
        # polling the pbs_server about the jobid.
        running = True
        while running:
            job_info = pbs.pbs_statjob(pbs_server, self.job_id, 'NULL', 'NULL')
            print job_info
            time.sleep(5)



    def run_local(self, settings):
       #stdin = open(settings["global"]["log_dir"] + self.name + \
               #  "_stdin.log", 'w')
        stdout = open(self.log_dir + self.name + "_stdout.log", 'w')
        stderr = open(self.log_dir + self.name + "_stderr.log", 'w')

        # Back up settings for this run in case we need them later
        self.settings = copy.deepcopy(settings)

        print self.cmd, shlex.split(self.cmd), self.cwd
        self.save()
        try:
            retcode = subprocess.check_call(shlex.split(self.cmd), 0, None, None, stdout, \
              stderr, None, False, False, self.cwd) # Launch process
            self.read_output(self.settings)
            self.write_output(self.settings)
            self.save()
            return retcode
        except subprocess.CalledProcessError:
            return 1
        except:
            print "Unexpected error:", sys.exc_info()[0]
            return 1


    def create_directories(self, settings):
        # Get current timestamp for organizing output directory
        t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
        # Create the genie3 directory in the output folder
        self.output_dir = settings["global"]["working_dir"] + \
              settings["global"]["output_dir"] + "/" + self.name + "/"
        if not os.path.isdir(self.output_dir):
          os.mkdir(self.output_dir)
        if not os.path.isdir(self.output_dir + "/output/"):
          os.mkdir(self.output_dir + "/output/")

        # Create the genie3 data directory
        self.data_dir = self.output_dir + "/data/"
        if not os.path.isdir(self.data_dir):
          os.mkdir(self.data_dir)

        # Create the genie3 log directory
        self.log_dir = self.output_dir + "/log/"
        if not os.path.isdir(self.log_dir):
          os.mkdir(self.log_dir)

        # Copy the entire working directory into our new directory.
        # We want to keep track of everything that was run.
        self.working_dir = self.output_dir + "/" + self.alg_name + "_program/"
        #if not os.path.isdir(self.working_dir):
        #os.mkdir(self.working_dir)
        print self.name + " is copying files that it needs before it begins..."
        copytree(settings[self.alg_name]["path"], self.working_dir, True)
        settings[self.alg_name]["name"] = self.name

        settings[self.alg_name]["output_dir"] = self.output_dir
        settings[self.alg_name]["log_dir"] = self.log_dir
        settings[self.alg_name]["data_dir"] = self.data_dir
        settings[self.alg_name]["working_dir_config"] = self.output_dir + '/' + self.alg_name + "_program/" + settings[self.alg_name]["working_dir_config"]

    def write_config(self, settings):
        # Code to write the config file on the fly before calling genie3
        # This will allow us to use the stock implementation of genie3
        WriteConfig(self.alg_name, settings)

        # Now we have to write a symlink to this file into the genie3
        # directory.  This is because genie3 must be run from its directory
        # because it relies on relative paths.

        print "Writing configuration file to " + settings[self.alg_name]["working_dir_config"]
        if os.path.isfile(settings[self.alg_name]["working_dir_config"]) or \
              os.path.islink(settings[self.alg_name]["working_dir_config"]):
                  os.remove(settings[self.alg_name]["working_dir_config"])
        os.symlink( settings[self.alg_name]["settings_file"], \
              settings[self.alg_name]["working_dir_config"])

    def write_prior(self, settings, prior):
        settings[self.alg_name]["prior_file"] =  settings[self.alg_name]["data_dir"] + "/"\
               + settings[self.alg_name]["prior_file"]
        prior.write_network(settings[self.alg_name]["prior_file"])

    def write_output(self, settings):
        # Write out the ranked listed of edges and the network
        pass

    def save(self):
        import pickle
        pickle.dump(self, open(self.output_dir + "/" + self.name + ".pickle", 'w'))

   # def getVistrail(self):



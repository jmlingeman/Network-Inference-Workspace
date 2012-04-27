import datetime

class Workflow:
   record = []  # This is the structure that will keep track of what we've run thus far
   vistrail = None # The file pointer to the vistrail file that we will be keeping

   def __init__(self):


      # Create the directory to keep all of our files in
      time_string = datetime.datetime.now().strftime("_%Y-%m-%d_%H.%M")
      settings["global"]["output_dir"] = "./output/" + settings["global"]["name"] + time_string + "_output/"
      os.mkdir(settings["global"]["output_dir"])

      # Create our Vistrail XML file
      settings["global"]["vistrail"] = settings["global"]["output_dir"] + time_string + "_vistrail"

      # TODO: Initialize vistrail XML file here.
      vistrail = File.open()


   def probe_data(data):
      # Code to probe data to figure out what we should run according to some rules
      # Perhaps these rules should be read in from a file

   def add_to_flow(alg):
      # Add an algorithm to the workflow

   def write_flow(self):
      # Writes the current workflow recorded in record
      for rec in record:
         vistrail.write(rec.get_vistrail)


   def run_node():
      # Runs the current node in the workflow stack
      record[len(record)-1].run()

   def read_in_config(config):
      settings = ReadConfig(config)


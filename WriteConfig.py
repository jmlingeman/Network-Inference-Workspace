def WriteConfig(algorithm_name, settings ):
   # What we are going to try to do here is read in a template file
   # that has the arguments we're filling in the the format {{argname}}
   # and replace those with the value that we have stored in the settings
   # hash.

   template_dir = settings["global"]["template_dir"]
   template_file = open(template_dir + '/' + algorithm_name + '.' + \
           settings[algorithm_name]["file_ext"], 'r')

   # Read in the template

   template = template_file.readlines()

   for i in xrange(len(template)):
      line = template[i]
      if "{{" in line and "}}" in line:
         # Loop through the line and replace all of the params with the
         # params from the settings dict.
         if line.count("{{") == line.count("}}"):
            for p in xrange(line.count("{{")):
                line = template[i]
                param_str = line[line.index("{{"):line.index("}}")+2]
                template[i] = template[i].replace(param_str, \
                        str(settings[algorithm_name][param_str[2:len(param_str)-2]]))
         else:
            print "ERROR in template file.  Forgot {{ or }}?"

   settings[algorithm_name]["settings_file"] = \
        settings[algorithm_name]["output_dir"] + "/" + algorithm_name + \
        "_settings" + "." + settings[algorithm_name]["file_ext"]
   config_file = open(settings[algorithm_name]["settings_file"], 'w')
   for l in template:
       config_file.write(l)
   config_file.flush()
   config_file.close()

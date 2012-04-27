def ReadConfig(settings={},config_file=None):

   if config_file != None:
      file_loc = config_file
   else:
      file_loc = "./config.cfg"

   file = open(file_loc, 'r')
   for line in file:
     # Each line should have the format of algname.paramname = value
     if len(line) > 1 and line.strip()[0] != '#':

        algname = line.split(".")[0].lower()
        paramname = line[line.find('.')+1:line.find('=')].strip().lower()
        value = line.split('=')[1].strip()

        value = value.split(',')
        for i in xrange(len(value)):
           value[i] = value[i].strip()
           if value[i].lower() == "none":
               value[i] = None
           elif value[i].isdigit():
               value[i] = int(value[i])
           else:
               try:
                  value[i] = float(value[i])
               except:
                  pass

        if len(value) == 1:
           value = value[0]
        if algname not in settings.keys():
           settings[algname] = {}
        settings[algname][paramname] = value

   #print settings
   return settings

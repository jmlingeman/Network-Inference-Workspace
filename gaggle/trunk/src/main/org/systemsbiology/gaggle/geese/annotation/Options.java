package org.systemsbiology.gaggle.geese.annotation;


/**
 * parses command line options for AnnotationGoose
 */
public class Options {
  
  String species = "unknown";
  String dataUri;
  String gooseName = "Annotation";


  public Options() {}
  
  public Options(String[] args) {
    parseCommandLineArguments(args);
  }


  public static String usage() {
    String ln = System.getProperty("line.separator");
    return "command line parameters:" + ln +
           "  -h            help" + ln +
           "  -s [species]  set species" + ln +
           "  -d [dataUri]  location of a tab-delimited file with column headers in first row" +ln;
  }

  public boolean gotSpecies() {
    return species != null;
  }

  public boolean gotDataUri() {
    return dataUri != null;
  }

  public void parseCommandLineArguments (String [] args) 
  {
    int max = args.length;
  
    for (int i = 0; i < max; i++) {
      String arg = args[i].trim();
      System.out.println("parse, args " + i + ": " + arg);
      if (arg.equals("-s") && i+1 < max) {
        species = args[++i].trim();
        System.out.println ("found -s, assigning species: " + species);
      }
      else if (arg.equals("-d") && i+1 < max)
        dataUri = args[++i].trim();
      else if (arg.equals("-h")) {
        System.out.println(usage());
      }
      else if (arg.equals("--gooseName") && i+1 < max) {
        gooseName = args[++i];
      }
      else {
        System.out.println("Warning: unrecognized command line argument: \"" + arg + "\"");
      }
    }

    System.out.println ("species: " + species + "   uri: " + dataUri);
  }
}

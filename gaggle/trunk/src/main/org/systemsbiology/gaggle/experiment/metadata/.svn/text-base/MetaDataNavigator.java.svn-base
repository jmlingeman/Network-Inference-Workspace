/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.experiment.metadata;

import java.util.*;
import java.io.File;
import org.systemsbiology.gaggle.experiment.repository.*;


/**
 * Discover, present, and navigate through the variety of experiments
 * described by the xml files found at the <repositoryUri> passed to the constructor.
 * 
 * This class provides the data structure which lies behing the JTree graphical
 * user interface used by the TreeDataBrowser class.
 * 
 * typical use:
 * 
 * 1) construct with a uri (file and http protocols currently supported)
 * 
 * 2) experiment xml files -- experiment metadata -- may be accompanied
 *    (in their web or local filesystem directory) by a .passwd file
 *      if that is there, then invoke a password-savvy file loader;
 *      otherwise, just read -all- the contents of the named directory
 *      
 * 3) ctor calls loadMetaData () and buildSimpleTree ()
 */ 
public class MetaDataNavigator {

  String repositoryUri;
  HashMap tree;
  HashMap titleToExperimentsHash;
  HashMap nameToExperimentsHash;
  HashMap perturbationsListToExperimentHash;
  Map<String,MetaData> conditionNameToExperimentHash = new HashMap<String,MetaData>();


  /**
   * Read the metadata from the given respository and construct
   * indexes by experiment name and title, and build condition
   * tree.
   */
  public MetaDataNavigator(String repositoryUri) throws Exception
  {
    this.repositoryUri = repositoryUri;
    tree = new HashMap();
    titleToExperimentsHash = new HashMap();
    nameToExperimentsHash = new HashMap();
    perturbationsListToExperimentHash = new HashMap();

    // create a hash so that clients (the JTree based client in particular)
    // can take a selection string (composed from the successive nodes in a
    // JTree path)
    // and get back the corresponding experiment
    // "genetic:knockout:boa1:illumination:Dark" -> experiment object

    loadMetaData();
    buildSimpleTree();
  }
  
  /**
   * strip off the filename, returning protocol (if any) and path. for instance:
   * http://db.systemsbiology.org/repos/copper.xml returns http://db.systemsbiology.org/repos
   * copper.xml returns ""
   * http://db.systemsbiology.org/repos returns unchanged
   */
  public String getUriBase()
  {
    if (!repositoryUri.trim().endsWith(".xml"))
      return repositoryUri;

    int lastSeparator = repositoryUri.lastIndexOf(File.separator);

    if (lastSeparator > 0)
      return repositoryUri.substring(0, lastSeparator);
    else
      return "";
  }
  
  public HashMap getPerturbationExperimentHash ()
  {
    return perturbationsListToExperimentHash;
  }
  
  /**
   * use the <repositoryUri> (the single argument to the ctor of this class) to
   * absolutize any relative uri's found in the DataSetDescriptions of this experiment
   */
  private void absolutizeDataSetUrisIfNecessary (MetaData experiment)
  {
      // TODO: MetaData.getDataSetDescriptions() calls MetaData.absolutizeDataSetUris(...)
      // which absolutizes paths also, so this might be redundant.
    DataSetDescription[] dsds = experiment.getDataSetDescriptions();
    for (int i = 0; i < dsds.length; i++) {
      String uri = dsds[i].getUri().trim();
      boolean hasProtocol = uri.indexOf("://") > 0;
      char firstChar = uri.charAt(0);
      if (!hasProtocol && Character.isLetterOrDigit(firstChar)) {
        //System.out.println ("needs absolutizing: " + uri + " add " + getUriBase ());
        String newUri = getUriBase() + "/" + uri;
        dsds[i].setUri(newUri);
      }
    }
  }
  
  protected void loadMetaData () throws Exception
  {
    ExperimentRepository repos = ExperimentRepositoryFactory.create (repositoryUri);
    String [] experimentNames = repos.getExperimentNames ();
    Arrays.sort (experimentNames);
  
    for (int i = 0; i < experimentNames.length; i++) {
      String experimentName = experimentNames[i];
      MetaData experiment = repos.getMetaData(experimentNames[i]);
      String experimentTitle = experiment.getTitle();
      absolutizeDataSetUrisIfNecessary(experiment);
      if (titleToExperimentsHash.containsKey(experimentTitle)) {
        String msg = "warning!  skipping duplicate experiment title: "
            + experimentTitle + " found in metadata.";
        System.err.println(msg);
      }
      titleToExperimentsHash.put(experimentTitle, experiment);
      nameToExperimentsHash.put(experimentName, experiment);
      for (Condition cond : experiment.getConditions()) {
          conditionNameToExperimentHash.put(cond.getAlias(), experiment);
      }
    }
  }

  public Condition getConditionForSpecificExperiment(String conditionName, String experimentName) {
      MetaData md = (MetaData)titleToExperimentsHash.get(experimentName);
      return md.getCondition(conditionName);
  }

  public MetaData getMetaDataForCondition(String conditionName) {
      return conditionNameToExperimentHash.get(conditionName);
  }
  
  public String [] getExperimentNames ()
  {    
    return (String []) nameToExperimentsHash.keySet().toArray (new String [0]);
  }     
  
  public String [] getExperimentTitles ()
  {    
    return (String []) titleToExperimentsHash.keySet().toArray (new String [0]);
  }     
  
  /**
   * i.e., "genetic:knockout:bop"
   *       "environmental:metals:feso4"
   */
  public String [] getPerturbationStrings ()
  {
    String [] hashMapKeys = (String [])perturbationsListToExperimentHash.keySet().toArray (new String [0]);
    Arrays.sort (hashMapKeys);
    return hashMapKeys;
  } 
  
  public HashMap getExperimentsHashedByName ()
  {    
    return nameToExperimentsHash;
  }     
  
  public HashMap getExperimentsHashedByTitle ()
  {    
    return titleToExperimentsHash;
  }     
  
  public MetaData getExperimentByTitle (String experimentTitle)
  {    
    return (MetaData)titleToExperimentsHash.get(experimentTitle);
  }
  
  public MetaData getExperimentByName (String experimentName)
  {    
    if (nameToExperimentsHash.containsKey(experimentName))
      return (MetaData)nameToExperimentsHash.get(experimentName);
    else
      return null;
  }
  
  /** we start with an array of strings, e.g., {"genetic", "knockout", "bop"}
   *  make this into a single, colon-separated string, for easy comparison with
   *  each experiement's perturbation string, eg, "genetic:knockout:bop"
   */
  protected String createColonSeparatedString(String[] perturbationList)
  {
    StringBuffer sb = new StringBuffer();

    sb.append(perturbationList[0]);
    for (int i = 1; i < perturbationList.length; i++) {
      sb.append(":");
      sb.append(perturbationList[i]);
    }

    return sb.toString();
  }
  
  /**
   * given a perturbationList: {"genetic", "knockout", "boa1", "illumination",
   * "Dark"} (produced, typically, from a JTree selection, where each of these
   * strings is a jtree node name on a path create a single colon-separated
   * string, 'genetic:knockout:boa1:illumination:Dark' then traverse the full
   * collection of those colon-separated strings, which were read from
   * 'perturbation' predicate of the xml files
   * 
   * this method returns a list of string pairs, in which each pair consists of
   * the perturbationString (or experimentKey), and a condition.
   * 
   * for example (client wants to know which experiments
   * genetic:knockout:boa1:ilumination:Dark returns
   * (genetic:knockout:boa1:illumination, Dark)
   * 
   */
  public ArrayList findExperimentKeyForPerturbation (String [] perturbationList)
  {
    // System.out.println ("MDN.findExperimentKeyForPerturbation, list size: " +
    // perturbationList.length);
    ArrayList result = new ArrayList();
    // for (int i=0; i < perturbationList.length; i++)
    // System.out.println (" " + perturbationList [i]);
    if (perturbationList.length == 0)
      return result;

    String target = createColonSeparatedString(perturbationList);

    String[] perturbationStrings = getPerturbationStrings();
    for (int i = 0; i < perturbationStrings.length; i++) {
      String oneExperimentsPerturbationString = perturbationStrings[i];


      // target == oneExperimentsPerturbationString
      // both are, for example, "genetic:knockout:bop",
      // append the oneExperimentsPerturbationString, with condition = None,
      // implying
      // that all conditions in that experiment will be selected

      if (target.equals(oneExperimentsPerturbationString)) {
        // System.out.println ("matched exact, target (" + target + ") against
        // experiment: " +
        // oneExperimentsPerturbationString);
        result.add(new String[] { oneExperimentsPerturbationString, null });
      }


      // target CONTAINS oneExperimentsPerturbationString -- perhaps the normal
      // case
      // "genetic:knockout:bop:oxygen:10" contains "genetic:knockout:bop"
      // the matched experiment is then 'genetic:knockout:bop'
      // the matched condition is 'oxygen:10'
      // but be careful! exclude incomplete matches of this sort:
      // target (genetic:knockout:phr1 and phr2) incompletely includes
      // experiment (genetic:knockout:phr1)
      // this simple test should distinguish good partial matches from bogus
      // ones:
      // does a colon immediately following the match?

      else if (target.indexOf(oneExperimentsPerturbationString) == 0) {
        // System.out.println ("found experiment (" +
        // oneExperimentsPerturbationString +
        // ") in target (" + target + ")");
        char nextCharFollowingMatch = target
            .charAt(oneExperimentsPerturbationString.length());
        // System.out.println ("next char: >" + nextCharFollowingMatch + "<");
        if (nextCharFollowingMatch == ':') {
          int start = oneExperimentsPerturbationString.length() + 1;
          String rawCondition = target.substring(start);
          result.add(new String[] { oneExperimentsPerturbationString,
              rawCondition });
        }
      }

      
      // oneExperimentsPerturbationString contains target: bring in one or more
      // full experiments
      // "genetic:knockout:bop" "genetic:knockout:boa1", etc, all
      // contain "genetic:knockout"
      // 
      // caution: in order to avoid a match like this:
      // target: environmental:metals:zinc
      // experiment: environmental:metals:zincAndSink
      // add a further test: the first character after the end of the match must
      // be ':'

      else if (oneExperimentsPerturbationString.indexOf(target) == 0) {
        // System.out.println ("found target (" + target + ") in experiment (" +
        // oneExperimentsPerturbationString + ")");
        int nextCharPosition = target.length();
        char nextChar = oneExperimentsPerturbationString
            .charAt(nextCharPosition);
        boolean isAColon = nextChar == ':';
        // System.out.println ("\n------------- next char: " + nextChar + " is a
        // colon: " + isAColon + "\n");
        if (!isAColon)
          break;
        result.add(new String[] { oneExperimentsPerturbationString, null });
      }
    }
      
    return result;
  }
  
  /**
   * retrieve an experiment by the value of its perturbation string:
   * "genetic:knockout:boa1" as specfied in its xml file: <predicate
   * category="perturbation" value="genetic:knockout:boa1"/> these strings are
   * returned (along with a condition name/value pair) by method
   * findExperimentKeyForPerturbation, above the perturbationString must be an
   * exact match to that found in the experiment
   */
  public MetaData []  getExperimentByPerturbationList (String perturbationString)
  {
    if (perturbationsListToExperimentHash.containsKey(perturbationString)) {
      ArrayList tmp = (ArrayList) perturbationsListToExperimentHash.get(perturbationString);
      return (MetaData[]) tmp.toArray(new MetaData[0]);
    }
  
    return new MetaData [0];
  }     

  /**
   * @return the condition tree as nested HashMaps
   */
  public HashMap getTree ()
  {
    return tree;
  }

  /**
   * build condition tree. Run after metadata is loaded.
   */
  protected void buildSimpleTree ()
  {
    String[] experimentTitles = getExperimentTitles();
    for (int i = 0; i < experimentTitles.length; i++) {
      String experimentTitle = experimentTitles[i];
      // System.out.println (" build simple tree, experiment title: " +
      // experimentTitle);
      MetaData experiment = getExperimentByTitle(experimentTitle);
      // System.out.println (" MDN.buildSimpleTree, experiment: " + experiment);
      HashMap predicates = experiment.getPredicates();
      Set keys = predicates.keySet();
      for (Iterator it = keys.iterator(); it.hasNext();) {
          String key = (String)it.next();
          addPerturbation(key, (String)predicates.get(key), experiment);
      }
    }
  }

  protected void addPerturbation(String key, String value, MetaData experiment) {
      if (key.contains("perturbation")) {
        String[] perturbations = value.split(":");

        ArrayList list = null;
        if (perturbationsListToExperimentHash.containsKey(value))
          list = (ArrayList) perturbationsListToExperimentHash
              .get(value);
        else
          list = new ArrayList();
        list.add(experiment);
        perturbationsListToExperimentHash.put(value, list);
        addToTree(experiment, perturbations);
      } // if "perturbation" key

  }




  /**
   * Insert the given experiment into the tree. The branches of the tree
   * are the entries in String[] perturbations. To that are appended the
   * "factor" and "level". Those are the name and value of a condition aka
   * a column of data.
   */
  protected void addToTree (MetaData experiment, String [] perturbations)
  {
    HashMap currentBranch = tree;
    for (int i = 0; i < perturbations.length; i++) {
      String category = perturbations[i];
      if (!currentBranch.containsKey(category))
        currentBranch.put(category, new HashMap());
      currentBranch = (HashMap) currentBranch.get(category);

      if (i == (perturbations.length - 1)) {
        String[] factors = experiment.getVariableNames();
        for (int f = 0; f < factors.length; f++) {
          String factor = factors[f];
          if (!currentBranch.containsKey(factor))
            currentBranch.put(factor, new HashMap());
          HashMap variableValues = experiment
              .getVariableValuesWithUnits(factor);
          String[] levels = (String[]) variableValues.keySet().toArray(new String[0]);
          for (int j = 0; j < levels.length; j++) {
            HashMap tmp = (HashMap) currentBranch.get(factor);
            tmp.put(levels[j], new HashMap());
          } // for j
        } // for f
      } // if i
    } // for i
  }
  
  public void dumpTree (HashMap node, String currentIndent)
  {
    if (node.size () == 0)
      return;
  
    String[] children = (String[]) node.keySet().toArray(new String[0]);
    Arrays.sort(children);
    currentIndent += "  ";
    for (int i = 0; i < children.length; i++) {
      String kid = children[i];
      System.out.println(currentIndent + kid);
      dumpTree((HashMap) node.get(kid), currentIndent);
    } // for i
  }
  
} // class MetaDataNavigator


// MetaData.java
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.metadata;
//-----------------------------------------------------------------------------------------

import java.util.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
//-----------------------------------------------------------------------------------------
// MetaData:  a prototype class for metadata associated with a microarray
//              experiment
// todo (pshannon, 31 may 05): add 'selectAll ()' to select all conditions
//                             perhaps, in the absence of selection, all
//                            conditions should be implicitly selected?

//-----------------------------------------------------------------------------------------
public class MetaData implements java.io.Serializable {

    String title;
    String date;
    String uri;          // optional, may be -- in fact, should be -- set by client
    HashMap predicates;
    HashMap links;
    ArrayList dataSetDescriptions;
    ArrayList conditions;
    List<Variable> constants;
    List<String> treePaths;

    //------------------------------------------------------------------------------
    // declare a hash, keyed by variable name, of non-redundant variable values,
    // with units for each value (when appropriate)
    // for example:   {'time': {'10': 'minutes', '0': 'minutes', '20': 'minutes'},
    //                  'gamma irradiation':   {'false': None, 'true': None}}
    //-----------------------------------------------------------------------------

    HashMap variableSummary;
    HashMap selectionCriteria;

    //-----------------------------------------------------------------------------------------
    public MetaData(String title, String date) {
        this.title = title;
        this.date = date;
        uri = null;
        predicates = new HashMap();
        links = new HashMap();
        dataSetDescriptions = new ArrayList();
        conditions = new ArrayList();
        variableSummary = new HashMap();
        selectionCriteria = new HashMap();
        constants = new ArrayList<Variable>();

    } // ctor

    //-----------------------------------------------------------------------------------------
    public MetaData(String title) {
        this(title, null);
    }

    //-----------------------------------------------------------------------------------------
    public String[] getReferenceUrls() {
        return new String[]{"http://www.sewardpark.net"};
    }

    //-----------------------------------------------------------------------------------------
    public String getTitle() {
        return title;
    }

    //-----------------------------------------------------------------------------------------
    public String getDate() {
        return date;
    }

    //-----------------------------------------------------------------------------------------
    public void setUri(String uri) {
        this.uri = uri;
    }

    //-----------------------------------------------------------------------------------------
    public String getUri() {
        return uri;
    }

    //-----------------------------------------------------------------------------------------
    public String getSpecies() {
        String[] preds = getPredicateNamesSorted();

        String result = getPredicate("species");
        if (result == null)
            return "unknown";
        else
            return result;
    }


    public List<Variable> getConstants() {
        return constants;
    }

    public void setConstants(List<Variable> constants) {
        this.constants = constants;
    }
    
    public void setPredicates(HashMap newPredicates)

    /**
     predicates (implemented here as a hash) provide general support
     for setting and getting constant attributes of an experiment:
     species, strain,  unchanging experimental conditions,
     perturbations enacted before the experiment, ....
     here (in xml) are some examples:

     <predicate category='species' value='Halobacterium NRC-1'/>
     <predicate category='strain'  value='wild type'/>
     <predicate category='manipulation' value='environmental'/>


     one motivation for these predicates is to provide means to
     identify, group, and select among a collection of experiments
     **/
    {
        predicates = newPredicates;
    }

    //-----------------------------------------------------------------------------------------
    public HashMap getPredicates() {
        return predicates;
    }

    //-----------------------------------------------------------------------------------------
    public String[] getPredicateNamesSorted() {
        String[] result = (String[]) predicates.keySet().toArray(new String[0]);
        Arrays.sort(result);
        return result;

    }

    //-----------------------------------------------------------------------------------------
    public String getPredicate(String name) {
        if (predicates.containsKey(name))
            return (String) predicates.get(name);

        return null;
    }

    //-----------------------------------------------------------------------------------------
    public void setDataSetDescriptions(ArrayList newValues)
    /***
     these are encapsulated in the DataSetDescription class; there may
     be many of them (but typically just 1 or two); (in xml) they look like this:
     <dataset status='primary' description='log10 ratios'>
     <uri> /users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio </uri>
     </dataset>

     <dataset status='derived' description='lambdas'>
     <uri> /users/pshannon/data/halo/microarrayXml/unitTests/gamma.lambda </uri>
     </dataset>
     these describe parallel matrices, with the same shape, and same column & row names.
     a common idiom is for the first (and 'primary') dataset to contain experimental
     measurements; the second dataset will contain error estimates on the primary data.
     a third dataset may be calculated from simulation codes
     ***/
    {
        dataSetDescriptions = newValues;
    }

    //-----------------------------------------------------------------------------------------
/**
 * strip off the filename, returning protocol (if any) and path, from the full uri
 * of this MetaData object.
 * for instance:
 * http://db.systemsbiology.org/repos/copper.xml
 * returns  http://db.systemsbiology.org/repos
 * copper.xml returns ""
 * http://db.systemsbiology.org/repos returns unchanged
 */
    public String getUriBase() {
        // the uri is conceivably either a filename, a directory, or a URL, so be careful.
        // on windows, uri might look like: file://C:\mydir\myrepo\file.xml
        // or something totally stupid like: file://C:\mydir\myrepo/file.xml
        int lastSeparator = Math.max(uri.lastIndexOf(File.separator), uri.lastIndexOf("/"));

        if (lastSeparator > 0)
            return uri.substring(0, lastSeparator + 1);
        else
            return "";

    } // getUriBase

    //---------------------------------------------------------------------------------
/**
 * expand the possibly relative uri of any included dataset descriptions, using the
 * absolute uri of the metadata itself.
 */
    private DataSetDescription[] absolutizeDataSetUris(DataSetDescription[] dsds) {
        // TODO: this code is duplicated in MetaDataNavigator.absolutizeDataSetUrisIfNecessary(...)
        if (uri == null || uri.length() == 0)
            return dsds;

        for (int i = 0; i < dsds.length; i++) {
            String uri = dsds[i].getUri().trim();
            boolean hasProtocol = uri.indexOf("://") > 0;
            char firstChar = uri.charAt(0);
            if (!hasProtocol && Character.isLetterOrDigit(firstChar)) {
                String newUri = getUriBase() + uri;
                dsds[i].setUri(newUri);
            }
        } // for i

        return dsds;

    } // absolutizeDataSetUris

    //---------------------------------------------------------------------------------
    public DataSetDescription[] getRawDataSetDescriptions() {
        return (DataSetDescription[]) dataSetDescriptions.toArray(new DataSetDescription[0]);
    }

    //---------------------------------------------------------------------------------
    public DataSetDescription[] getDataSetDescriptions() {
        return absolutizeDataSetUris(getRawDataSetDescriptions());
    }

    //-----------------------------------------------------------------------------------------
    public HashMap getVariableSummary()
    /***
     return the non-redundant list (actually a hash) all known values of
     all known variables.  this list is created dynamically as 
     'Expeiment.addCondition ()' is repeatedly called.  the result
     looks like this:

     {'time': {'0': 'minutes',
     '10': 'minutes',
     '20': 'minutes'},
     'copper': {'700': 'micromolar',
     '850': 'micromolar',
     '1000': 'micromolar',}
     'gamma irradiation': {'true': None,
     'false': None}
     }

     ***/
    {
        return variableSummary;
    }

    //-----------------------------------------------------------------------------------------
    public void addLink(String type, String url) {
        links.put(type, url);

    }  // addLinke

    //-----------------------------------------------------------------------------------------
    public HashMap getReferenceLinks() {
        return links;
    }

    //-----------------------------------------------------------------------------------------
    public void addCondition(Condition condition)

    /***
     condition is an instance of the Condition class.  an example in xml

     <condition alias='C0'>
     <variable name='gamma irradiation' value='false'/>
     <variable name='time' value='0'/>
     </condition>
     ***/
    {
        conditions.add(condition);
        updateVariableSummary(condition);
    }

    //-----------------------------------------------------------------------------------------
    protected void updateVariableSummary(Condition condition) {
        /***
         check all the variables in <condition> adding any new variable/value pair
         (with units) to the whole-experiment's summary of variables.  remember that
         this summary is on-redundant:  a "time = 30 minutes" variable will, for
         example, only appear once
         ***/

        String[] variableNames = condition.getVariableNames();
        for (int i = 0; i < variableNames.length; i++) {
            Variable variable = condition.getVariable(variableNames[i]);
            String name = variable.getName();
            String value = variable.getValue();
            String units = variable.getUnits();
            HashMap variableDescription;
            if (!variableSummary.containsKey(name))
                variableDescription = new HashMap();
            else
                variableDescription = (HashMap) variableSummary.get(name);
            if (!variableDescription.containsKey(value))
                variableDescription.put(value, units);
            variableSummary.put(name, variableDescription);
        }

    } // updateVariableSummary

    //-----------------------------------------------------------------------------------------
    public Condition[] getConditions() {
        return (Condition[]) conditions.toArray(new Condition[0]);
    }

    //-----------------------------------------------------------------------------------------
    public boolean hasCondition(String alias) {
        Condition conditions[] = getConditions();
        for (int c = 0; c < conditions.length; c++)
            if (conditions[c].getAlias().equalsIgnoreCase(alias))
                return true;

        return false;

    } // hasCondition

    //-----------------------------------------------------------------------------------------
    public Condition getCondition(String alias) {
        Condition conditions[] = getConditions();
        for (int i = 0; i < conditions.length; i++)
            if (conditions[i].getAlias().equalsIgnoreCase(alias))
                return conditions[i];

        return null;

    } // getCondition

    //-----------------------------------------------------------------------------------------
    public String[] getVariableNames() {
        return (String[]) variableSummary.keySet().toArray(new String[0]);
    }

    //-----------------------------------------------------------------------------------------
    public HashMap getVariableValuesWithUnits(String variableName) {
        if (!variableSummary.containsKey(variableName))
            return null;

        return (HashMap) variableSummary.get(variableName);
    }

    //-----------------------------------------------------------------------------------------
    public String getUnits(String variableName, String variableValue) {
        if (!variableSummary.containsKey(variableName))
            return null;

        HashMap oneVariable = (HashMap) variableSummary.get(variableName);
        if (!oneVariable.containsKey(variableValue))
            return null;

        return (String) oneVariable.get(variableValue);

    }

    //-----------------------------------------------------------------------------------------
    public String[] getSortedVariableValues(String variableName) {
        if (!variableSummary.containsKey(variableName))
            return new String[0];

        HashMap valuesWithUnits = getVariableValuesWithUnits(variableName);
        if (valuesWithUnits == null)
            return new String[0];
        String[] values = (String[]) valuesWithUnits.keySet().toArray(new String[0]);
        Arrays.sort(values);
        return values;

    }

    //-----------------------------------------------------------------------------------------
    public String[] getNamesOfSelectedVariables() {
        String[] result = (String[]) selectionCriteria.keySet().toArray(new String[0]);
        Arrays.sort(result);
        return result;
    }

    //-----------------------------------------------------------------------------------------
    public boolean columnSelected(Condition condition, HashMap selectionCriteria)

    // a condition is selected if
    //   1) at least one of its variables is explicitly selected
    //   2) none of its variables are implicitly excluded, by the explicit
    //      mention of another value for that variable
    //
    // the gamma radiation experiment demonstrates this:
    // there are two variables:  gamma (true|false), and time (0|10|20 minutes)
    // the condition aliases (the column names) are
    //   C0, C10, C20   (gamma = false; 'C' stands for control)
    //   G0, G10, G20   (gamma = true)
    // if the selection criteria consist of
    //   1)              gamma = true: G0, G10, G20 are all selected
    //   2)                   time=10: C10, G10 are selected
    //   3)   gamma=false, time=10,20: C10, C20 are selected
    //
    // to restate: a condition is selected if one of its variables has a positive match
    //  and none of its variables contradict

    {
        boolean result = true;
        String[] selectedVariables = getNamesOfSelectedVariables();
        Variable[] conditionVariables = condition.getVariables();
        boolean atLeastOneExactMatchFound = false;
        boolean atLeastOneContradictionFound = false;

        for (int cv = 0; cv < conditionVariables.length; cv++) {
            Variable variable = conditionVariables[cv];
            String varName = variable.getName();
            String varValue = variable.getValue();
            if (selectionCriteria.containsKey(varName)) {
                ArrayList selectedValues = (ArrayList) selectionCriteria.get(varName);
                if (selectedValues.size() == 0)
                    continue;
                if (selectedValues.contains(varValue))
                    atLeastOneExactMatchFound = true;
                else
                    atLeastOneContradictionFound = true;
            } // if selectionCriteria has this variable
        } // for cv

        if (atLeastOneExactMatchFound && !atLeastOneContradictionFound)
            return true;
        else
            return false;

    } // columnSelected

    //-----------------------------------------------------------------------------------------
    public Condition[] getSelectedConditions() {
        if (selectionCriteria.size() == 0)   // nothing is selected, return empty list
            return new Condition[0];

        ArrayList result = new ArrayList();
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = (Condition) conditions.get(i);
            if (columnSelected(condition, selectionCriteria))
                result.add(condition);
        } // for i

        return (Condition[]) result.toArray(new Condition[0]);

    } // getSelectedConditions



    //-----------------------------------------------------------------------------------------
    public String[] getSelectedConditionsAsAliases() {
        Condition[] conditions = getSelectedConditions();
        String[] result = new String[conditions.length];
        for (int c = 0; c < conditions.length; c++) {
            result[c] = conditions[c].getAlias();
        }

        Arrays.sort(result);
        return result;

    } // getSelectedConditionsAsAliases

    //-----------------------------------------------------------------------------------------
    public void clearSelectionCriteria() {
        selectionCriteria = new HashMap();
    }

    //-----------------------------------------------------------------------------------------
    public HashMap getSelectionCriteria() {
        return selectionCriteria;
    }

    //-----------------------------------------------------------------------------------------
    public void selectConditionByName(String conditionName) {
        // assert (variableSummary.containsKey (conditionName));
        HashMap variableValuesHash = getVariableValuesWithUnits(conditionName);
        if (variableValuesHash == null)
            return;

        String[] variableValues = (String[]) variableValuesHash.keySet().toArray(new String[0]);
        for (int v = 0; v < variableValues.length; v++)
            addSelectionCriterion(conditionName, variableValues[v]);

    } // selectConditionByName

    //-----------------------------------------------------------------------------------------
    public void selectAllConditions()
/**
 *  get all variable names (
 *  for each, get all values it may take
 *  select all values for each variable
 */
    {
        String[] variableNames = getVariableNames();
        for (int v = 0; v < variableNames.length; v++) {
            HashMap varValueCluster = getVariableValuesWithUnits(variableNames[v]);
            if (varValueCluster == null)
                continue;
            String[] varValueNames = (String[]) varValueCluster.keySet().toArray(new String[0]);
            for (int i = 0; i < varValueNames.length; i++) {
                addSelectionCriterion(variableNames[v], varValueNames[i]);
            } // for i
        } // for v

    } // selectAllConditions

    //-----------------------------------------------------------------------------------------
    public void addSelectionCriterion(String variableName, String variableValue)

    /***
     the client may be certain that the experiment has a variable 
     with the specified name and value, thus making it sensible to
     add it as a selection criterion.

     but (as of 27 aug 2004, pshannon), the TreeDataBrowser client may not know that.
     it may have a number of MetaDatas which it stores as a group,
     (for instance, having the same perturbation string:
     batVbat_cmyc1Vcmyc1.xml:
     <predicate category='perturbation' value='Protein-DNA:general TF:cmyc'/>
     tfb-ABC-cmyc.xml:
     <predicate category='perturbation' value='Protein-DNA:general TF:cmyc'/>)
     since it may be convenient for that client to iterate across a set of
     related experiments, adding a new selection criterion to each -- but only
     if it applies.  so a few checks are made below, and early returns are made
     if there is no such variableName/variableValue to add as a selectionCriterion
     here

     ***/
    {
        if (!variableSummary.containsKey(variableName))
            return;

        HashMap hash = (HashMap) variableSummary.get(variableName);
        if (!((HashMap) variableSummary.get(variableName)).containsKey(variableValue))
            return;

        ArrayList list = new ArrayList();
        if (selectionCriteria.containsKey(variableName))
            list = (ArrayList) selectionCriteria.get(variableName);

        list.add(variableValue);
        selectionCriteria.put(variableName, list);

    } // addSelectionCriterion

    //-----------------------------------------------------------------------------------------
    public void setConditions(ArrayList newValue)
// an array of hashes, keyed by variable name, and each hash representing a
// matrix column
    {
        conditions = newValue;
    }

    //-----------------------------------------------------------------------------------------
    public String[] getConditionAliases() {
        ArrayList list = new ArrayList();
        Condition[] conditions = getConditions();
        for (int i = 0; i < conditions.length; i++)
            list.add(conditions[i].getAlias());

        return (String[]) list.toArray(new String[0]);
    }


    public List<String> getTreePaths() {
        return treePaths;
    }

    public void setTreePaths(List<String> treePaths) {
        this.treePaths = treePaths;
    }

    public String toString() { // todo show constants and tree paths
        StringBuffer sb = new StringBuffer();
        sb.append("title: ");
        sb.append(title);
        sb.append("\n");

        if (uri != null)
            sb.append("uri: ").append(uri).append("\n");

        sb.append("datasets: ");
        DataSetDescription[] ds = getDataSetDescriptions();
        for (int i = 0; i < ds.length; i++) {
            sb.append(ds[i].toString());
            sb.append("\n");
        }
        sb.append("\n");

        sb.append("predicates: ");
        String[] keys = (String[]) predicates.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            sb.append(" = ").append(predicates.get(keys[i]));
            sb.append("\n");
        }

        sb.append("variableDefinitions: ");
        sb.append(getVariableSummary());
        sb.append("\n");

        sb.append("conditions: ");
        for (int i = 0; i < conditions.size(); i++) {
            sb.append(getConditions()[i].toString());
            sb.append("\n");
        }

        return sb.toString();

    } // toString
//-----------------------------------------------------------------------------------------
} // class MetaData

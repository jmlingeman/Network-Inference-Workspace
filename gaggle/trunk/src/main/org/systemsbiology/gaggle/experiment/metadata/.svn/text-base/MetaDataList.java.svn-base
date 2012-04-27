package org.systemsbiology.gaggle.experiment.metadata;


import org.systemsbiology.gaggle.experiment.repository.ExperimentRepository;
import org.systemsbiology.gaggle.experiment.repository.ExperimentRepositoryFactory;

import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

// todo - make sure this works (or fails gracefully) if the uri passed in just represents a single file

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class MetaDataList {
    private URI uri;
    private Map<String,Map<Condition,String>> tagMap = null;
    private MetaData[] listOfAllMetadata = null;
    private Map<String,List<TreePath>> conditionNameToPerturbationStringsHash =
            new HashMap<String,List<TreePath>>();

    public MetaDataList(String uriString) throws URISyntaxException {
        this.uri = new URI(uriString);
    }

    public MetaData[] getList() throws Exception {
        if (listOfAllMetadata != null) {
            return listOfAllMetadata;
        }

        ExperimentRepository repos = ExperimentRepositoryFactory.create (uri.toString());
        List<MetaData> list =  new ArrayList<MetaData>();
        for (String name : repos.getExperimentNames()) {
            list.add(repos.getMetaData(name));
        }
        listOfAllMetadata = list.toArray(new MetaData[0]);
        return listOfAllMetadata;
    }

    public Map<String,List<TreePath>> getConditionToPerturbationStringHash(JTree tree) throws Exception{
        if (conditionNameToPerturbationStringsHash.size() > 0) {
            return conditionNameToPerturbationStringsHash;
        }
        MetaData[] allMetadata = getList();
        for (MetaData metadata : allMetadata) {
            for (Condition cond : metadata.getConditions()) {
                List<TreePath> treePaths = new ArrayList<TreePath>();
                for (String treePathString : metadata.getTreePaths()) {
                    for (Variable var : cond.getVariables()) {
                        String path = treePathString + ":" + var.getName();
                        path += ":" + var.getValue();
                        treePaths.add(getTreePathFromPerturbationString(path,tree));
                    }
                }
                conditionNameToPerturbationStringsHash.put(cond.getAlias(), treePaths);
            }
        }
        return conditionNameToPerturbationStringsHash;
    }

    private TreePath getTreePathFromPerturbationString(String perturbationString, JTree tree) {
        List<DefaultMutableTreeNode> pathElements = new ArrayList<DefaultMutableTreeNode>();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        pathElements.add(root);
        DefaultMutableTreeNode currentTopNode = root;
        String[] pathSegments = perturbationString.split(":");
        for (String pathSegment : pathSegments) {
            for (int j = 0; j < currentTopNode.getChildCount(); j++) {
                DefaultMutableTreeNode candidate = (DefaultMutableTreeNode) currentTopNode.getChildAt(j);
                if (candidate.toString().equals(pathSegment)) {
                    pathElements.add(candidate);
                    currentTopNode = candidate;
                    break;
                }
            }
        }
        return new TreePath(pathElements.toArray(new DefaultMutableTreeNode[0]));
    }

    public TreePath[] getTreePathsForConditionNames(String[] conditionNames, JTree tree) throws Exception {
        Map<String,List<TreePath>> map = getConditionToPerturbationStringHash(tree);
        List<TreePath> treePathList = new ArrayList<TreePath>();
        for (String alias : conditionNames) {
            List<TreePath> results = map.get(alias);
            // remove dupes
            for (TreePath result : results) {
                if (!treePathList.contains(result)) {
                    treePathList.add(result);
                }
            }
            //treePathList.addAll(map.get(alias));
        }

        return treePathList.toArray(new TreePath[0]);
    }



    public String[] getAllTags() throws Exception {
        if (tagMap == null) {
            buildTagMap();
        }
        String[] result = tagMap.keySet().toArray(new String[0]);
        Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }


    // todo - figure out why blank spaces end up in list of tags
    // todo - refactor the following two methods to avoid duplication:

    private void addAllConditionsInExperimentToTagMap(String tag, MetaData metadata) {
        tag = tag.toLowerCase();
        if (tagMap.get(tag) == null) {
            tagMap.put(tag, new HashMap<Condition,String>());
        }
        for (Condition cond : metadata.getConditions()) {
            if (tag != null && !tag.trim().equals("")) {
                tagMap.get(tag).put(cond, null);
            }
        }
    }

    private void addConditionToTagMap(String tag, Condition condition) {
        tag = tag.toLowerCase();
        if (tagMap.get(tag) == null) {
            tagMap.put(tag, new HashMap<Condition,String>());
        }
        if (tag != null && !tag.trim().equals("")) {
            tagMap.get(tag).put(condition, null);
        }
    }

    private void buildTagMap() throws Exception {
        tagMap = new HashMap<String,Map<Condition,String>>();
        MetaData[] all = getList();
        for (MetaData metadata : all) {
            /* todo - figure out how best to handle constants
            // add constant names and values with units
            for (Variable constant : metadata.getConstants()) {
                addAllConditionsInExperimentToTagMap(constant.getName(), metadata);
                String value = constant.getValue();
                if (constant.getUnits() != null) {
                    value += " " + constant.getUnits();
                }
                addAllConditionsInExperimentToTagMap(value, metadata);
            }
            */
            // add per-condition variable names and values with units
            for (Condition condition : metadata.getConditions()) {
                for (Variable variable : condition.getVariables()) {
                    addConditionToTagMap(variable.getName(), condition);
                    String value = variable.getValue();
                    if (variable.getUnits() != null) {
                        value += " " + variable.getUnits();
                    }
                    addConditionToTagMap(value, condition);
                }
            }
            for (String predicate : metadata.getPredicateNamesSorted()) {
                if (predicate.toLowerCase().contains("perturbation")) {
                    String path = metadata.getPredicate(predicate);
                    String[] segments = path.split(":");
                    // add perturbation path segments
                    for (String segment : segments) {
                        addAllConditionsInExperimentToTagMap(segment, metadata);
                    }
                } else {
                    // add other predicate values
                    addAllConditionsInExperimentToTagMap(metadata.getPredicate(predicate), metadata);
                }
            }
        }
    }

    private Map<Condition,String> getWildCardMatches(String tag) {
        tag = tag.toLowerCase();
        if (!tag.endsWith("*")) {
            return tagMap.get(tag);
        }
        tag = tag.substring(0, (tag.length() -1)); // remove *
        Map<Condition,String> returnValue = new HashMap<Condition,String>();
        //        tagMap = new HashMap<String,Map<Condition,String>>();

        for (String candidate :tagMap.keySet()) {
            if (candidate.startsWith(tag)) {
                for (Condition cond : tagMap.get(candidate).keySet()) {
                    returnValue.put(cond, null);
                }
            }
        }
        return returnValue;
    }

    public Condition[] filterMetaDataByTags(String[] tags) throws Exception {
        if (tags == null || tags.length == 0) {
            return new Condition[0];
        }

        for (int i = 0; i < tags.length; i++) {
            tags[i] = tags[i].toLowerCase();
        }

        if (tagMap == null) {
            buildTagMap();
        }

        

        Map<Condition,String>  prevMatches = getWildCardMatches(tags[0]);
        assert (prevMatches != null);

        for (int i = 1; i < tags.length; i++) {
            Map<Condition,String>  matches = getWildCardMatches(tags[i]);
            if (matches == null) {
                System.out.println("matches was null, tag is " + tags[i]);
                return new Condition[0];
            }
            Map<Condition,String> nextMatches = new HashMap<Condition,String>();
            for (Condition cond : matches.keySet()) {
                if (prevMatches.containsKey(cond)) {
                    nextMatches.put(cond, null);
                }
            }
            prevMatches = nextMatches;
        }
        try {
            return prevMatches.keySet().toArray(new Condition[0]);
        } catch (NullPointerException npe) {
            return new Condition[0];
        }
    }

    public static void main(String[] args) {
        MetaDataList mdl;
        try {
            //String repos = "file:///Users/dtenenbaum/emi-sandbox/halobacterium/repos";
            String repos = "file:///net/arrays/emi/halobacterium/repos";
            mdl = new MetaDataList(repos);

            mdl.buildTagMap();
/*
            Map<Condition,String> map = mdl.getWildCardMatches("fg");
            System.out.println("map key length = " + map.keySet().size());
*/

            //Condition[] matches = mdl.filterMetaDataByTags(new String[] {"environmental", "Cu"});
            Condition[] matches = mdl.filterMetaDataByTags(new String[] {"VNG1179C", "cu"});
            //Condition[] matches = mdl.filterMetaDataByTags(new String[] {"cobal*", "wild type"});
            System.out.println("# of matches: " + matches.length);
            for (Condition match : matches) {
                System.out.println(match.getAlias());
            }
            System.out.println("# of matches: " + matches.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

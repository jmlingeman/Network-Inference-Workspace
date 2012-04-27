// MetaDataXmlParser.java
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.readers;
//-----------------------------------------------------------------------------------------

import java.util.*;

import org.jdom.*;
import org.jdom.input.*;

import java.io.*;

import org.systemsbiology.gaggle.experiment.metadata.*;

//-----------------------------------------------------------------------------------------
public class MetaDataXmlParser {

    SAXBuilder builder;
    Document doc;
    String documentSource = "unknown";
    MetaData metaData;
    String uri;

    //-----------------------------------------------------------------------------------------
    public MetaDataXmlParser(String uri) throws Exception {
        this.uri = uri;
        builder = new SAXBuilder();
        boolean explicitProtocol = (uri.indexOf("://") > 0);

        if (!explicitProtocol || uri.startsWith("file://")) {
            String filename;
            if (explicitProtocol)
                filename = uri.substring(7);
            else
                filename = uri;
            try {
                doc = builder.build(new FileInputStream(filename));
            }
            catch (Exception e) {
                System.err.println("Error parsing file \"" + filename + "\": " + e);
                throw e;
            }
            documentSource = "localFile";
        } else if (uri.startsWith("http://")) {
            try {
                doc = builder.build(uri);
            }
            catch (Exception e) {
                System.err.println("Error parsing uri \"" + uri + "\": " + e);
                throw e;
            }
            documentSource = "web";
        } else if (uri.startsWith("httpIndirect://")) {
            TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader(uri);
            reader.read();
            String text = reader.getText();
            File tmpFile = File.createTempFile("gaggle", "xml");
            FileWriter fileWriter = new FileWriter(tmpFile);
            fileWriter.write(text, 0, text.length());
            fileWriter.close();
            try {
                doc = builder.build(new FileInputStream(tmpFile.getAbsolutePath()));
            }
            catch (Exception e) {
                System.err.println("Error parsing uri \"" + uri + "\": " + e);
                throw e;
            }
            documentSource = "webIndirect";
            tmpFile.delete();
        } else
            throw new IllegalArgumentException("unrecognized data source protocol in uri: " + uri);

        parseDocument(doc);

    } // ctor

    //-----------------------------------------------------------------------------------------
    protected void parseDocument(Document jdomDoc) {
        try {
            Element root = jdomDoc.getRootElement();
            String name = root.getAttribute("name").getValue();
            String date = root.getAttribute("date").getValue();
            metaData = new MetaData(name, date);

            metaData.setUri(uri);

            metaData.setPredicates(parsePredicates(root));
            metaData.setDataSetDescriptions(parseDataSetDescriptions(root));
            metaData.setConstants(parseConstants(root));
            metaData.setTreePaths(parseTreePaths(root));

            HashMap referenceLinks = parseReferenceLinks(root);
            String[] linkTypes = (String[]) referenceLinks.keySet().toArray(new String[0]);

            for (int r = 0; r < linkTypes.length; r++) {
                String linkType = linkTypes[r];
                String url = (String) referenceLinks.get(linkType);
                metaData.addLink(linkType, url);
            }

            Condition[] conditions = parseConditions(root);
            for (int c = 0; c < conditions.length; c++)
                metaData.addCondition(conditions[c]);
        }
        catch (Exception ex0) {
            throw new IllegalArgumentException("illegal xml document: " + uri);
        }

    } // parseDocuement

    //-----------------------------------------------------------------------------------------
    public MetaData getMetaData() {
        return metaData;
    }
//-----------------------------------------------------------------------------------------

    protected List<Variable> parseConstants(Element root) {
        List<Variable> result = new ArrayList<Variable>();
        Element parent = root.getChild("constants");
        if (parent == null) {
            return result;
        }
        for (Object o : parent.getChildren("variable")) {
            Element variable = (Element) o;
            String name = variable.getAttribute("name").getValue();
            String value = variable.getAttribute("value").getValue();
            Attribute unitsAttribute = variable.getAttribute("units");
            String units = null;
            if (unitsAttribute != null)
                units = unitsAttribute.getValue();
            result.add(new Variable(name, value, units));
        }
/*
    Element [] variableElements =
       (Element [])parent.getChildren ("variable").toArray (new Element [0]);
    for (int v=0; v < variableElements.length; v++) {
      Element variable = variableElements [v];
      String name = variable.getAttribute ("name").getValue ();
      String value = variable.getAttribute ("value").getValue ();
      Attribute unitsAttribute = variable.getAttribute ("units");
      String units = null;
      if (unitsAttribute != null)
        units = unitsAttribute.getValue ();
      result.add(new Variable (name, value, units));
      }
*/

        return result;
    }

    protected HashMap parsePredicates(Element root) {
        HashMap result = new HashMap();
        //List  children = root.getChildren ("predicate");
        Element[] children = (Element[]) root.getChildren("predicate").toArray(new Element[0]);
        for (int c = 0; c < children.length; c++) {
            Element child = children[c];
            String category = child.getAttribute("category").getValue();
            String value = child.getAttribute("value").getValue();
            result.put(category, value);
        }

        return result;

    }  // parsePredicates


    protected List<String> parseTreePaths(Element root) {
        List<String> treePaths = new ArrayList<String>();
        Element[] children = (Element[]) root.getChildren("predicate").toArray(new Element[0]);
        for (int c = 0; c < children.length; c++) {
            Element child = children[c];
            String category = child.getAttribute("category").getValue();
            String value = child.getAttribute("value").getValue();
            if (category.toLowerCase().trim().contains("perturbation")) {
                treePaths.add(value);
            }
        }
        return treePaths;
    }

    protected ArrayList parseDataSetDescriptions(Element root) {
        ArrayList list = new ArrayList();

        Element[] children = (Element[]) root.getChildren("dataset").toArray(new Element[0]);
        for (int c = 0; c < children.length; c++) {
            Element child = children[c];
            String status = child.getAttribute("status").getValue();
            String type = child.getAttribute("type").getValue();
            String uri = child.getChild("uri").getTextTrim();
            DataSetDescription dsd = new DataSetDescription(uri, status, type);
            list.add(dsd);
        }

        //return (DataSetDescription []) list.toArray (new DataSetDescription [0]);
        return list;

    } // parseDataSetDescriptions

    //-----------------------------------------------------------------------------------------
    protected HashMap parseReferenceLinks(Element root) {
        Element[] children = (Element[]) root.getChildren("link").toArray(new Element[0]);
        HashMap result = new HashMap();

        for (int c = 0; c < children.length; c++) {
            Element child = children[c];
            String type = child.getAttribute("type").getValue();
            String url = child.getAttribute("url").getValue();
            result.put(type, url);
        } // for c

        return result;

    } // parseReferenceLinks

    //-----------------------------------------------------------------------------------------
/**
 * return an array of Condition objects
 * for example, from
 * <p/>
 * <condition alias="C30">
 * <variable name="gamma irradiation" value="false"/>
 * <variable name="time" value="30" units="minutes"/>
 * </condition>
 * <p/>
 * <condition alias="G0">
 * <variable name="gamma irradiation" value="true"/>
 * <variable name="time" value="0" units="minutes"/>
 * </condition>
 * <p/>
 * string representation of the two resulting condition objects:
 * [note that no units ("None") are specified for irradiation]
 * <p/>
 * condition: C30, gamma irradiation:  false, None,  time:  30, minutes
 * condition: G0   gamma irradiation:  true,  None   time:   0, minutes
 */
    protected Condition[] parseConditions(Element root) {

        Element[] children = (Element[]) root.getChildren("condition").toArray(new Element[0]);
        List<Condition> list = new ArrayList<Condition>();

        for (int c = 0; c < children.length; c++) {
            Element child = children[c];
            String alias = child.getAttribute("alias").getValue();
            Integer order = null;
            try {
                order = child.getAttribute("order").getIntValue();
            } catch (DataConversionException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to parse 'order' attribute in EMI-ML", e);
            } catch (Exception ex) {
                //System.out.println("Is there an order attribute on this condition?");
                //ex.printStackTrace();
            }
            Condition condition = new Condition(alias, order);
            Element[] variableElements =
                    (Element[]) child.getChildren("variable").toArray(new Element[0]);
            for (int v = 0; v < variableElements.length; v++) {
                Element variable = variableElements[v];
                String name = variable.getAttribute("name").getValue();
                String value = variable.getAttribute("value").getValue();
                Attribute unitsAttribute = variable.getAttribute("units");
                String units = null;
                if (unitsAttribute != null)
                    units = unitsAttribute.getValue();
                condition.addVariable(new Variable(name, value, units));
            } // for v


            list.add(condition);

            final Map<Condition, Integer> originalOrderMap = new HashMap<Condition, Integer>();
            for (int i = 0; i < list.size(); i++) {
                Condition cond = list.get(i);
                originalOrderMap.put(cond, i);
            }

            Collections.sort(list, new Comparator<Condition>() {
                private int defaultSort(Condition c1, Condition c2) {
                    return originalOrderMap.get(c1).compareTo(originalOrderMap.get(c2));
                }

                public int compare(Condition c1, Condition c2) {
                    if ((c1.getOrder() == null) && (c2.getOrder() == null)) {
                        return defaultSort(c1, c2);
                    }
                    if ((c1.getOrder() != null) && (c2.getOrder() == null)) {
                        return defaultSort(c1, c2);
                    }
                    if ((c1.getOrder() == null) && (c2.getOrder() != null)) {
                        return defaultSort(c1, c2);
                    }
                    return c1.getOrder().compareTo(c2.getOrder());
                }
            });


        } // for c


        return (Condition[]) list.toArray(new Condition[0]);
    }
//-----------------------------------------------------------------------------------------
} // class MetaDataXmlParser

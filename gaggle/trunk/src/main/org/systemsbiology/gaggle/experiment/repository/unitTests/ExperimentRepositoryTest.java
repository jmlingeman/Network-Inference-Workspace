// ExperimentRepositoryTest.java
//--------------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/01/13 21:27:25 $
// $Author: dtenenba $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.repository.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import org.systemsbiology.gaggle.experiment.repository.*;
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.unitTests.MetaDataXmlParserTest;

//--------------------------------------------------------------------------------------
public class ExperimentRepositoryTest extends TestCase {

//--------------------------------------------------------------------------------------
/**
 *  apache and tomcat render directory listings differently.  this test make sure that
 *  the parsing class, HttpdirectRepository.getExperimentNames (), can handle both equally well.
 */
public void testExtractFilenamesFromWebPage () throws Exception
{
  System.out.println ("testExtractFilenamesFromWebPage");
  String apacheReposUri = "http://db.systemsbiology.net/devPM/sbeams/tmp/cytoscape/gaggle_metadata_test";
  String tomcatReposUri = "http://db:8060/halo/data/unitTests";
  ExperimentRepository apacheRepos = ExperimentRepositoryFactory.create (apacheReposUri);
  ExperimentRepository tomcatRepos = ExperimentRepositoryFactory.create (tomcatReposUri);

  String [] apacheXmlFileNames = apacheRepos.getExperimentNames ();
  assertTrue (apacheXmlFileNames.length == 1);
  assertTrue (apacheXmlFileNames [0].equals ("Conditions.xml"));

  // no tomcat webserver available right now (pshannon, 25 jan 2006)
  // skip this test
  //String [] tomcatXmlFileNames = tomcatRepos.getExperimentNames ();
  //assertTrue (tomcatXmlFileNames.length == 1);
  //assertTrue (tomcatXmlFileNames [0].equals ("sample.xml"));
    
} // testDirectoryRepository
//--------------------------------------------------------------------------------------
public void testDirectoryRepository () throws Exception
{
  System.out.println ("testDirectoryRepository");
  String uri = "../../sampleData";

  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("file"));
  // System.out.println ("repose path: " + repos.getPath ());
  assertTrue (repos.getPath ().equals ("../../sampleData"));
  assertTrue (repos.getUri ().equals (uri));
  assertTrue (validRepository (repos));
    
} // testDirectoryRepository
//--------------------------------------------------------------------------------------
public void testHttpDirectExplicitXmlFileRepository () throws Exception
{
  System.out.println ("testHttpDirectExplicitXmlFileRepository");
  String uri = "http://gaggle.systemsbiology.net/projects/hpy/2005-10/data/feAddBack.xml";
  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("http"));
  String [] names = repos.getExperimentNames ();
  assertTrue (names.length == 1);
  assertTrue (names [0].equals ("feAddBack.xml"));
  MetaData metaData = repos.getMetaData ("feAddBack.xml");
  String metaDataText = metaData.toString ();
  String [] actual = metaData.getConditionAliases ();
  String [] expected = {"FeAddBack-1-0", "FeAddBack-1-30", "FeAddBack-1-60", "FeAddBack-1-120",
                        "FeAddBack-2-0", "FeAddBack-2-30", "FeAddBack-2-60", "FeAddBack-2-120"};

  Arrays.sort (expected);
  Arrays.sort (actual);
  assertTrue (Arrays.equals (expected, actual));
    
} // testHttpDirectExplicitXmlFileRepository 
//--------------------------------------------------------------------------------------
public void testHttpDirectDirectoryRepository () throws Exception
{
  System.out.println ("testHttpDirectDirectoryRepository");
  String uri = "http://gaggle.systemsbiology.net/projects/hpy/2005-10/data";
  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("http"));
  String [] names = repos.getExperimentNames ();

  assertTrue (names.length >= 6);
  assertTrue (names [0].equals ("feAddBack.xml"));
  MetaData metaData = repos.getMetaData ("feAddBack.xml");
  String metaDataText = metaData.toString ();
  String [] actual = metaData.getConditionAliases ();
  String [] expected = {"FeAddBack-1-0", "FeAddBack-1-30", "FeAddBack-1-60", "FeAddBack-1-120",
                        "FeAddBack-2-0", "FeAddBack-2-30", "FeAddBack-2-60", "FeAddBack-2-120"};

  Arrays.sort (expected);
  Arrays.sort (actual);
  assertTrue (Arrays.equals (expected, actual));
    
} // testHttpDirectDirectoryRepository 
//--------------------------------------------------------------------------------------
public void notestSingleFileAsRepository () throws Exception
{
  System.out.println ("testSingleFileAsRepository");
  String uri = "../../sampleData/sample.xml";

  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("file"));
  assertTrue (repos.getPath ().equals ("../../sampleData/sample.xml"));
  assertTrue (repos.getUri ().equals (uri));
  assertTrue (validRepository (repos));
    
} // testDirectoryRepository
//--------------------------------------------------------------------------------------
public void notestWebDirectRepository () throws Exception
{
  System.out.println ("testWebDirectRepository");
  String uri = "http://db:8060/halo/data/unitTests";

  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("http"));
  assertTrue (repos.getPath ().equals ("db:8060/halo/data/unitTests"));
  assertTrue (repos.getUri ().equals (uri));
  String [] experimentNames = repos.getExperimentNames ();
  assertTrue (validRepository (repos));

} // testWebDirectRepository
//--------------------------------------------------------------------------------------
/**
 * a file 'gaggle.props' must provide user and password. 
 * this file may be in the current working directory, or in the user's
 * home directory. for this test, these values are used:
 *
 *  user=tester0
 *  password=pw0
 *
 * these match user and password info found in two files in the repository:
 * db.unitTests> grep sample ~/halo/data/.permissions 
 *   sample: tester0
 * db.unitTests> grep tester0 ~/halo/data/.passwd
 *   tester0: pw0
 *
 */
public void notestWebIndirectRepositoryWithUserPasswordFromPropsFile () throws Exception
{
  System.out.println ("testWebIndirectRepositoryWithUserPasswordFromPropsFile");

  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";
  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri);

  assertTrue (repos.getProtocol ().equals ("httpIndirect"));
  assertTrue (validRepository (repos));
  
}  // testWebIndirectRepositoryWithPasswordFromPropsFile
//--------------------------------------------------------------------------------------
public void notestWebIndirectRepositoryWithExplicitIncorrectPassword () throws Exception
{
  System.out.println ("testWebIndirectRepositoryWithExplicitIncorrectPassword");

  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";
  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri, "tester0", "bogus");

  assertTrue (repos.getProtocol ().equals ("httpIndirect"));
  String [] experimentNames = repos.getExperimentNames ();
  assertTrue (experimentNames.length == 0);
  
}  // testWebIndirectRepositoryWithPasswordFromPropsFile
//--------------------------------------------------------------------------------------
public void notestWebIndirectRepositoryWithExplicitUserPassword () throws Exception
{
  System.out.println ("testWebIndirectRepositoryWithExplicitUserPassword");

  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";
  boolean debug = false;
  ExperimentRepository repos = ExperimentRepositoryFactory.create (uri, "tester0", "pw0", debug);

  assertTrue (repos.getProtocol ().equals ("httpIndirect"));
  assertTrue (validRepository (repos));
  
}  // testWebIndirectRepositoryWithPasswordFromPropsFile
//--------------------------------------------------------------------------------------
private boolean validRepository (ExperimentRepository repos) throws Exception
{
  String [] experimentNames = repos.getExperimentNames ();
  Arrays.sort (experimentNames);
  assertTrue (experimentNames.length > 0);

  assertTrue (Arrays.binarySearch (experimentNames, "sample.xml") >= 0);

  MetaData e = repos.getMetaData ("sample.xml");
  assertTrue (MetaDataXmlParserTest.sampleMetaDataHasExpectedContents (e));

  DataSetDescription [] dsds = e.getDataSetDescriptions ();
  assertTrue (sampleMatricesHaveExpectedContents (repos, dsds));
  assertTrue (dsds.length == 2);

  return true;
}
//--------------------------------------------------------------------------------------
private boolean sampleMatricesHaveExpectedContents (ExperimentRepository repos,
                                                    DataSetDescription [] dsds) throws Exception
{
  for (int i=0; i < dsds.length; i++) {
    // System.out.println ("dsds [" + i + "]: " + dsds [i]);
    org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = repos.getDataSet (dsds [i]);
    assertTrue (matrix.getRowCount () == 2400);
    assertTrue (matrix.getColumnCount () == 16);
    } // for i

  return true;

}// sampleMatricesHaveExpectedContents
//--------------------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (ExperimentRepositoryTest.class));
}
//--------------------------------------------------------------------------------------
} // ExperimentRepositoryTest

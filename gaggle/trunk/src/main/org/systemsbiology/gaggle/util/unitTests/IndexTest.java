package org.systemsbiology.gaggle.util.unitTests;

import java.util.Collection;

import org.systemsbiology.gaggle.util.Index;

import junit.framework.TestCase;


public class IndexTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSingleEntry() {
    Index index = new Index();
    index.put("delillo", "White Noise");
    index.put("banks",   "Player of Games");
    index.put("durbin",  "Biological sequence analysis");
    index.put("eddy",    "Biological sequence analysis");
    
    Collection<String> titles = index.get("delillo");
    assertNotNull(titles);
    assertEquals(1, titles.size());
    assertTrue(titles.contains("White Noise"));

    assertTrue(index.get("banks").contains("Player of Games"));
    assertTrue(index.get("eddy").contains("Biological sequence analysis"));
  }


  public void testMultipleEntry() {
    Index index = new Index();
    index.put("delillo", "White Noise");
    index.put("delillo", "Cosmopolis");
    index.put("delillo", "The Names");
    index.put("delillo", "Underworld");
    index.put("eddy",    "Biological sequence analysis");

    Collection<String> titles = index.get("delillo");
    assertEquals(4, titles.size());
    assertTrue(titles.contains("White Noise"));
    assertTrue(titles.contains("Cosmopolis"));
    assertTrue(titles.contains("The Names"));
    assertTrue(titles.contains("Underworld"));

    titles = index.get("eddy");
    assertEquals(1, titles.size());
    assertTrue(titles.contains("Biological sequence analysis"));
  }

}

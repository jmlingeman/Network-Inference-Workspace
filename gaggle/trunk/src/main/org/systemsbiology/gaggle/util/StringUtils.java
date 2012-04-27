package org.systemsbiology.gaggle.util;

import java.util.Iterator;


/**
 * String utility functions
 * @author cbare
 */
public class StringUtils {

  /**
   * takes an iterable and returns a delimited string representation.
   */
  public static String toDelimitedString(Iterable<? extends Object> list, String delimiter) {
    StringBuilder sb = new StringBuilder();
    if (list==null) {
      return "null";
    }
    Iterator<? extends Object> iterator = list.iterator();
    if (iterator.hasNext()) {
      sb.append(String.valueOf(iterator.next()));
    }
    while (iterator.hasNext()) {
      sb.append(delimiter).append(String.valueOf(iterator.next()));
    }
    return sb.toString();
  }

  /**
   * takes an array and returns a delimited string representation.
   */
  public static String toDelimitedString(Object[] array, String delimiter) {
    StringBuilder sb = new StringBuilder();
    if (array==null) {
      return "null";
    }
    if (array.length > 0) {
      sb.append(String.valueOf(array[0]));
    }
    for (int i=1; i<array.length; i++) {
      sb.append(delimiter).append(String.valueOf(array[i]));
    }
    return sb.toString();
  }
}

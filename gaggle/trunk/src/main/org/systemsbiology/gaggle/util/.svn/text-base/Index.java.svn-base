package org.systemsbiology.gaggle.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * maps from a key String to a list of Strings.
 */
public class Index extends HashMap<String, List<String>> {

  boolean caseSensitive = false;

  public Index() {
    super();
  }

  public Index(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public Index(int initialCapacity) {
    super(initialCapacity);
  }

  public Index(Map<? extends String, ? extends List<String>> m) {
    super(m);
  }

  public void put(String key, String value) {
    key = treat(key);
    List<String> list = this.get(key);
    if (list == null) {
      list = new ArrayList<String>(1);
      put(key, list);
    }
    if (!list.contains(value))
      list.add(value);
  }

  private String treat(String key) {
    if (key==null || caseSensitive)
      return key;
    else
      return key.toLowerCase();
  }

  public void setCaseSensitivity(boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public boolean containsKey(String key) {
    return super.containsKey(treat(key));
  }

  public List<String> get(String key) {
    return super.get(treat(key));
  }

  public List<String> put(String key, List<String> value) {
    return super.put(treat(key), value);
  }

  public List<String> remove(String key) {
    return super.remove(treat(key));
  }

}

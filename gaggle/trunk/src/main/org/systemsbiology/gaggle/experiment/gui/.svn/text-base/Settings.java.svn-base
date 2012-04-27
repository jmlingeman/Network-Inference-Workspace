package org.systemsbiology.gaggle.experiment.gui;

/**
 * A singleton class for holding user preference settings.
 * @author cbare
 */
public class Settings {
  protected static Settings instance = new Settings();

  private String workingDirectory = System.getProperty("user.home");
  private String conditionNameDisplayStyle;

  protected Settings() {}

  /**
   * @return the singleton instance
   */
  public static Settings getInstance() {
    return instance;
  }

  /**
   * current working directory for loading or saving plots.
   * Defaults to null.
   */
  public synchronized String getWorkingDirectory() {
    return workingDirectory;
  }

  public synchronized void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  /**
   * Determines how x-axis labels are displayed in Plots.
   * Defaults to null.
   * @see org.systemsbiology.gaggle.experiment.gui.plotters.matrix.MatrixPlotter
   */
  public String getConditionNameDisplayStyle() {
    return conditionNameDisplayStyle;
  }

  public void setConditionNameDisplayStyle(String conditionNameDisplayStyle) {
    this.conditionNameDisplayStyle = conditionNameDisplayStyle;
  }
}

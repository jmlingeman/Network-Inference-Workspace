package org.systemsbiology.gaggle.experiment.datamatrix;

import org.systemsbiology.gaggle.experiment.gui.DataMatrixView;

import java.util.Date;

public class MatrixProject {

    String name;
    String description;
    Date date;
    DataMatrixView[] views;


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataMatrixView[] getViews() {
        return views;
    }

    public void setViews(DataMatrixView[] views) {
        this.views = views;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project name: ")
                .append(name)
                .append("\n")
                .append("Description:\n")
                .append(description)
                .append("\n\nDate saved: ")
                .append(date)
                .append("\nNumber of views: ")
                .append(views.length);
        return sb.toString();
    }
}

package org.systemsbiology.gaggle.experiment.gui;

import org.systemsbiology.gaggle.experiment.datamatrix.MatrixProject;

import javax.swing.*;

public class LoadMatrixProjectListModel extends AbstractListModel {
    MatrixProject[] projects;

    public LoadMatrixProjectListModel(MatrixProject[] projects) {
        this.projects = projects;
    }

    public Object getElementAt(int index) {
        return projects[index].getName();
    }

    public int getSize() {
        return projects.length;
    }

    public MatrixProject[] getProjects() {
        return projects;
    }
}

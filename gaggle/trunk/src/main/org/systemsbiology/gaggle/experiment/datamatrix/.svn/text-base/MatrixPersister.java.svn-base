package org.systemsbiology.gaggle.experiment.datamatrix;

import com.db4o.ObjectContainer;
import com.db4o.Db4o;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

import java.io.File;
import java.util.List;

public class MatrixPersister {
    private String dirPath;
    private String dbFileName;

    public MatrixPersister() {
        dirPath = System.getProperty("user.home") + File.separator + ".dmv";
        dbFileName = dirPath + File.separator + "dmv-projects.yap";
    }

    ObjectContainer database;

    public ObjectContainer getDatabase() {
        Db4o.configure().optimizeNativeQueries(true);
        //Db4o.configure().activationDepth(Integer.MAX_VALUE);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir(); // todo check return value (bool) and act accordingly
        }

        database = Db4o.openFile(dbFileName);
        return database;
    }

    public boolean doesDatabaseExist() {
        File f = new File(dbFileName);
        return f.exists();
    }

    public void saveProject(MatrixProject project) {
        getDatabase().set(project);
        closeDatabase();
    }

    public void closeDatabase() {
        if (database == null) {
            return;
        }
        database.close();
    }

    public MatrixProject getProjectByName(String name) {
        List<MatrixProject> projects = getDatabase().query(new FindByNameQuery(name));
        if (projects.size() == 0) {
            return null;
        }

        return projects.get(0);
    }

    public MatrixProject[] getAllProjects() {
        // todo figure out how to make this a native query with sorting
        Query query = getDatabase().query();
        query.constrain(MatrixProject.class);
        query.descend("date").orderDescending();
        List<MatrixProject> results = query.execute(); // can't do anything about this warning, I guess
        return results.toArray(new MatrixProject[0]);
    }


    class FindByNameQuery extends Predicate<MatrixProject> {
        String name;

        public FindByNameQuery(String name) {
            this.name = name;
        }

        public boolean match(MatrixProject project) {
            return project.getName().equals(name);
        }
    }

    public static void main(String[] args) {
        /*
        MatrixPersister mp = new MatrixPersister();
        //mp.closeDatabase();
        MatrixProject p = mp.getProjectByName("dan's test project");
        //mp.closeDatabase();
        System.out.println("desc = " + p.getDescription());
        System.out.println("date = " + p.getDate());
        System.out.println("Are views null? " + (p.getViews() == null));
        System.out.println("# of views: " + p.getViews().length);
        System.out.println("class = " + p.getViews()[0].getClass().getName());
        System.out.println("m0:0,0 = " + p.getViews()[0].get(0,0));
        System.out.println("columns of m0: " + p.getViews()[0].getColumnCount());
        System.out.println("rows of m0: " + p.getViews()[0].getRowCount());
        */
    }

}

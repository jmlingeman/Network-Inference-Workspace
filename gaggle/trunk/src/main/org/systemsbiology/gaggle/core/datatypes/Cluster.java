package org.systemsbiology.gaggle.core.datatypes;

/**
 * An object encapsulating the notion "these items (genes, proteins, etc.) are interesting in these conditions".
 */
public class Cluster implements GaggleData {
	private String name;
	private String species;
	private String[] rowNames;
	private String[] columnNames;
    private Tuple metadata;

    public Cluster() {}

    public Cluster(String species, String[] rowNames, String[] columnNames) {
        this(null, species, rowNames, columnNames);
    }


    public Cluster(String name, String species, String[] rowNames, String[] columnNames) {
        this.name = name;
        this.species = species;
        this.rowNames = rowNames;
        this.columnNames = columnNames;
    }

    public String getName() {
		return name;
	}

	public String getSpecies() {
		return species;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public String[] getRowNames() {
		return rowNames;
	}

	public void setRowNames(String[] rowNames) {
		this.rowNames = rowNames;
	}


    public Tuple getMetadata() {
        return metadata;
    }

    public void setMetadata(Tuple metadata) {
        this.metadata = metadata;
    }
}


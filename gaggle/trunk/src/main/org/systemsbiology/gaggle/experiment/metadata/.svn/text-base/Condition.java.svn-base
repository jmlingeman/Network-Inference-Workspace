// Condition.java
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.metadata;

import java.util.*;

/**
 * Condition: an experimental condition (for our purposes here) describes a column of data
 * in an experiment data set.  by example, in xml:
 * <p/>
 * <condition alias='C0'>
 * <variable name='gamma irradiation' value='false'/>
 * <variable name='time'              value='0'       units='minutes'/>
 * </condition>
 * <p/>
 * <condition alias='C10'>
 * <variable name='gamma irradiation' value='false'/>
 * <variable name='time'              value='10'      units='minutes'/>
 * </condition>
 * <p/>
 * the 'alias' is the name of the column, as found in the experiment data file.
 * the one or more variables specify the experimental condition (and can often found
 * to be encoded in the terse 'alias' name: 'C0' means 'control [no radiation], time 0
 */
public class Condition implements java.io.Serializable {

    String alias;
    Integer order;
    ArrayList<Variable> variables;


    public Condition(String alias) {
        this(alias, null);
    }

    public Condition(String alias, Integer order) {
        this.alias = alias;
        this.order = order;
        this.variables = new ArrayList<Variable>();
    }

    public String getAlias() {
        return alias;
    }

    public void setVariables(ArrayList<Variable> newValue) {
        variables = newValue;
    }

    public Variable[] getVariables() {
        return variables.toArray(new Variable[0]);
    }

    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    public String[] getVariableNames() {
        Variable[] variables = getVariables();
        ArrayList<String> list = new ArrayList<String>();

        for (Variable variable : variables) list.add(variable.getName());

        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    public Variable getVariable(String name) {
        Variable[] variables = getVariables();

        for (Variable variable : variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }

        return null;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("condition: ");
        sb.append(alias);
        sb.append("\n");
        sb.append("Order: " );
        String orderStr = (order == null) ? "(null)" : order.toString();
        sb.append(orderStr).append("\n");

        for (Variable var : getVariables()) {
            sb.append("     ");
            sb.append(var.getName())
                    .append(" = ")
                    .append(var.getValue());
            if (var.getUnits() != null) {
                sb.append(" ")
                        .append(var.getUnits());
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}


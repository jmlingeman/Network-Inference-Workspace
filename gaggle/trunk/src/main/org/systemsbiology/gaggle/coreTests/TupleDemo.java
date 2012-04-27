package org.systemsbiology.gaggle.coreTests;
// todo - fix this
/*
import org.systemsbiology.gaggle.core.datatypes.TupleList;
import org.systemsbiology.gaggle.core.datatypes.Tuple;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;

import java.util.List;
import java.util.ArrayList;

public class TupleDemo {
    // todo - make this a set of unit tests rather than a standalone demo

    /*
    TupleList is the basic Gaggle data type, a list of Tuples.
    Tuple is a single n-tuple, composed of n Single objects.
    A Single object contains just one value, with an optional name.
     *\

    String[] nodes = new String[]{
            "VNG0001H", "VNG0002G", "VNG0003C", "VNG0005H", "VNG0006G"
    };
    List<GaggleTuple> movie = new ArrayList<GaggleTuple>(); //contains all frames of the movie


    public void setUp() {
        String[] conditions = new String[]{
                "Dun_F2_000mina_vs_NRC-1g.sig", "Dun_F2_005mina_vs_NRC-1g.sig", "Dun_F2_010mina_vs_NRC-1g.sig",
                "Dun_F2_020mina_vs_NRC-1g.sig", "Dun_F2_040mina_vs_NRC-1g.sig"};


        for (int i = 0; i < conditions.length; i++) {
            GaggleTuple tupleList = new GaggleTuple();
            tupleList.setName("a tuple list that could express anything but expresses a movie frame in this case");
            tupleList.setSpecies("pretty crowing chicken");
            Tuple conditionIdentifier = new Tuple();
            conditionIdentifier.addSingle(new Single("condition", conditions[i]));
            tupleList.getMetadata().addTuple(conditionIdentifier);

            addTuples(tupleList.getData(), i);
            movie.add(tupleList);
        }
    }

    private  void addTuples(TupleList tupleList, int conditionNumber) {
        double[] log10 = null;
        double[] lambda = null;

        switch (conditionNumber) {
            case 0:
                log10 = new double[]{-0.076, -0.171, -0.077, -0.055, -0.151};
                lambda = new double[]{19.166, 55.485, 14.059, 8.551, 35.59};
                break;
            case 1:
                log10 = new double[]{-0.094, -0.148, -0.094, -0.049, -0.118};
                lambda = new double[]{22.112, 45.742, 18.595, 5.652, 22.49};
                break;
            case 2:
                log10 = new double[]{-0.134, -0.171, -0.118, -0.095, -0.05};
                lambda = new double[]{38.498, 43.175, 27.643, 16.059, 4.624};
                break;
            case 3:
                log10 = new double[]{-0.12, -0.184, -0.086, -0.083, -0.003};
                lambda = new double[]{31.116, 50.378, 15.022, 15.408, 0.018};
                break;
            case 4:
                log10 = new double[]{-0.167, -0.203, -0.136, -0.084, -0.051};
                lambda = new double[]{48.089, 55.638, 34.941, 12.769, 4.509};
                break;
        }

        for (int i = 0; i < nodes.length; i++) {
            Tuple log10Tuple = new Tuple();
            Tuple lambdaTuple = new Tuple();

            log10Tuple.addSingle(new Single(null, nodes[i])); // add node name
            log10Tuple.addSingle(new Single(null, "log10 ratios")); // add attribute name
            assert log10 != null;
            log10Tuple.addSingle(new Single(null, log10[i])); // add value
            tupleList.addTuple(log10Tuple);

            // do the same for lambdas:
            lambdaTuple.addSingle(new Single(null, nodes[i]));
            lambdaTuple.addSingle(new Single(null, "lambdas"));
            lambdaTuple.addSingle(new Single(null, lambda[i]));
            tupleList.addTuple(lambdaTuple);
        }
    }

    public void playMovie() {
        for (int i = 0; i < movie.size(); i++) {
            // let's pretend we just got a broadcast (the frame object):
            GaggleTuple frame = movie.get(i);
            String condition = (String)frame.getMetadata().getTupleAt(0).getSingleAt(0).getValue();
            validateMovieFrame(frame);
            System.out.println("\n\n *** playing a frame, condition = " + condition + " ***");
            for (int j = 2; j < frame.getData().getTupleList().size(); j++) {
                Tuple currentTuple = frame.getData().getTupleAt(j);
                System.out.print("Setting node ");
                System.out.print(currentTuple.getSingleList().get(0).getValue());
                System.out.print(" attribute ");
                System.out.print(currentTuple.getSingleList().get(1).getValue());
                System.out.print(" to ");
                if (!(currentTuple.getSingleList().get(2).getValue() instanceof Double))  { // or int, or string
                    throw new RuntimeException("Movie data is the wrong type!");
                }
                System.out.println(currentTuple.getSingleList().get(2).getValue());
            }
            if (i <(movie.size() -1)) {
                System.out.println("\n --- pretend to sleep for some interval --- \n");
            }


        }
    }

    public void validateMovieFrame(GaggleTuple frame) {
        if (frame.getData().getTupleList().size() < 1 || frame.getMetadata().getTupleList().size() < 1) {
            throw new RuntimeException("This is too short to be a movie!");
        }
        Tuple firstTuple = frame.getMetadata().getTupleAt(0);
        Single conditionIdentifier = firstTuple.getSingleList().get(0);
        if (!(conditionIdentifier.getName().equals("condition")) ||
                conditionIdentifier.getValue() == null ||
                !(conditionIdentifier.getValue() instanceof String)) {
            throw new RuntimeException("Can't play this frame because I can't find its condition!");
        }
    }

    public static void main(String[] args) {
        TupleDemo demo = new TupleDemo();
        demo.setUp();
        demo.playMovie();
    }
    
}
*/
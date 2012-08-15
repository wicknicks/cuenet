package esl.cuenet.algorithms.firstk.impl;

import java.util.HashMap;

public class AggSocNet {

    private HashMap<String, Boolean> asnMap = new HashMap<String, Boolean>();

    public AggSocNet() {
        String[] asnNames = new String[]{"Atish Das Sarma",
                "Amarnath Gupta",
                "Nicola Onose",
                "Jennie Zhang",
                "Ying Zhang",
                "Danupon Nanongkai",
                "Galen Reeves",
                "Ramesh Jain",
                "Chen Li",
                "Arjun Satish"
        };
        for (String asnName : asnNames) asnMap.put(asnName, true);
    }

    public boolean isSkippable(String name) {
        return !asnMap.containsKey(name);
    }

}

package esl.cuenet.generative.structs;

public class STHelper {

    public static boolean contains(ContextNetwork.Instance parent, ContextNetwork.Instance child) {
        return parent.intervalStart <= child.intervalStart && parent.intervalEnd >= child.intervalEnd;
    }

    public static boolean lequals(ContextNetwork.Instance parent, ContextNetwork.Instance child) {
        return parent.location.equals(child.location);
    }

    public static boolean tequals(ContextNetwork.Instance parent, ContextNetwork.Instance child) {
        return parent.intervalStart == child.intervalStart && parent.intervalEnd == child.intervalEnd;
    }

}

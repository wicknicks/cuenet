package esl.cuenet.source;

public class QueryOperator {

    // TEMPORAL

    public enum Temporal {
        EQUALS,
        STARTS,
        FINISHES,
        INTERSECTS
    }

    // SPATIAL

    public enum Spatial {
        EQUALS,
        OVERLAPS,
        NEARBY
    }

    // STRING MATCHING

    public enum StringType {
        EQUALS,
        CONTAINS,
        FUZZY
    }

}

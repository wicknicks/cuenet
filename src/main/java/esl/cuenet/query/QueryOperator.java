package esl.cuenet.query;

public class QueryOperator {

    private Operators operators = null;

    public enum Operators {
        EQUALS,

        TEMPORAL_STARTS,
        TEMPORAL_FINISHES,
        TEMPORAL_INTERSECTS,

        SPATIAL_OVERLAPS,
        SPATIAL_NEARBY,

        STRING_CONTAINS,
        STRING_FUZZY
    }

    public QueryOperator(Operators operators) {
        this.operators = operators;
    }

    public Operators get() {
        return this.operators;
    }

}

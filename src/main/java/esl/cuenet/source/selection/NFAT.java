package esl.cuenet.source.selection;

public interface NFAT {

    State[] startStates();

    State[] getFinalStates();

    boolean isFinalState(State state);

    boolean match(String s);

}

package esl.cuenet.source.selection;

public interface State {

    Transition[] getTransitions();

    String name();

}

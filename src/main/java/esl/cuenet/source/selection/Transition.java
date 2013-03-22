package esl.cuenet.source.selection;

public interface Transition {

    State outState();

    Matcher matcher();

}

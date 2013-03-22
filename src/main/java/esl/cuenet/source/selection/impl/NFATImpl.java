package esl.cuenet.source.selection.impl;

import esl.cuenet.source.selection.Matcher;
import esl.cuenet.source.selection.NFAT;
import esl.cuenet.source.selection.State;
import esl.cuenet.source.selection.Transition;

import java.util.Collections;
import java.util.HashSet;

public class NFATImpl implements NFAT {

    private State[] states = null;
    private HashSet<State> start = new HashSet<State>();
    private HashSet<State> finals = new HashSet<State>();

    public NFATImpl(State[] states) {
        this.states = states;
    }

    @Override
    public State[] startStates() {
        State[] states = new State[start.size()];
        start.toArray(states);
        return states;
    }

    @Override
    public State[] getFinalStates() {
        State[] states = new State[start.size()];
        finals.toArray(states);
        return states;
    }

    @Override
    public boolean isFinalState(State state) {
        return finals.contains(state);
    }

    public void markAsStartState(State state) {
        start.add(state);
    }

    public void markAsFinalState(State state) {
        finals.add(state);
    }

    private boolean current(char c) {
        return false;
    }

    @Override
    public boolean match(String string) {
        return false;
    }

    public static State[] constructStates(String[] stateNames) {
        State[] states = new State[stateNames.length];
        for (int i=0; i<stateNames.length; i++) states[i] = new StateImpl(stateNames[i], null);
        return states;
    }

    public Transition constructTransition(State start, State end, char matcher) {
        TransitionImpl transition = new TransitionImpl(end, new StringMatcher(matcher));
        ((StateImpl) start).transitions.add(transition);
        return transition;
    }

    public static class StateImpl implements State {

        private final HashSet<Transition> transitions = new HashSet<Transition>();
        private final String name;

        public StateImpl(String name, Transition[] transitions) {
            Collections.addAll(this.transitions, transitions);
            this.name = name;
        }

        @Override
        public Transition[] getTransitions() {
            Transition[] ts= new Transition[transitions.size()];
            transitions.toArray(ts);
            return ts;
        }

        @Override
        public String name() {
            return name;
        }

    }

    public static class TransitionImpl implements Transition {

        private final State oState;
        private final Matcher matcher;

        public TransitionImpl(State oState, Matcher matcher) {
            this.oState = oState;
            this.matcher = matcher;
        }

        @Override
        public State outState() {
            return oState;
        }

        @Override
        public Matcher matcher() {
            return matcher;
        }

    }


    public class StringMatcher implements Matcher {

        private final char character;

        public StringMatcher(char c) {
            this.character = c;
        }

        @Override
        public boolean match() {
            return current(character);
        }
    }

}

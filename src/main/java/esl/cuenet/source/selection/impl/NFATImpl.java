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
    public static final char EPSILON = '~';
    private char currentItem;

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

    private void current(char c) {
        currentItem = c;
    }

    @Override
    public boolean match(String _string) {
        HashSet<State> currentStates = new HashSet<State>();
        Collections.addAll(currentStates, startStates());
        HashSet<State> nextStates = new HashSet<State>();

        currentStates = epsilonAll(currentStates);
        for (char character : _string.toCharArray()) {
            HashSet<State> temp = new HashSet<State>();
            for (State current: currentStates) temp.add(current);

            for (State current: temp) {
                nextStates.addAll(step(current, character));
                currentStates.remove(current);
                currentStates.addAll(nextStates);
            }

            epsilonAll(currentStates);
            nextStates = new HashSet<State>();
        }

        for (State state: currentStates) {
            if (isFinalState(state)) return true;
            for (State e: epsilonClosure(state))
                if (isFinalState(e)) return true;
        }

        return false;
    }

    private HashSet<State> epsilonAll(HashSet<State> currentStates) {
        HashSet<State> nextStates = new HashSet<State>();
        boolean allEpsilons = true;
        for (State current: currentStates) {
            for (Transition transition: current.getTransitions()) {
                if (transition.isEpsilonTransition())
                    nextStates.add(transition.outState());
                else
                    allEpsilons = false;
            }
            if ( !allEpsilons ) nextStates.add(current);
        }
        return nextStates;
    }

    private HashSet<State> epsilonClosure(State state) {
        HashSet<State> epsilonNexts = new HashSet<State>();
        for (Transition transition: state.getTransitions()) {
            if (transition.isEpsilonTransition()) epsilonNexts.add(transition.outState());
        }
        return epsilonNexts;
    }

    private HashSet<State> step(State current, char character) {
        HashSet<State> nextStates = new HashSet<State>();
        for (Transition transition: current.getTransitions()) {
            current(character);
            if (transition.isEpsilonTransition()) continue;
            if (transition.matcher().match())
                nextStates.add(transition.outState());
        }
        return nextStates;
    }

    public static State[] constructStates(String[] stateNames) {
        State[] states = new State[stateNames.length];
        for (int i=0; i<stateNames.length; i++) states[i] = new StateImpl(stateNames[i], null);
        return states;
    }

    public Transition addTransition(State start, State end, char matcher) {
        TransitionImpl transition;
        if (matcher == NFATImpl.EPSILON)
            transition = new TransitionImpl(end);
        else
            transition = new TransitionImpl(end, new StringMatcher(matcher));
        ((StateImpl) start).transitions.add(transition);
        return transition;
    }

    public static class StateImpl implements State {

        private final HashSet<Transition> transitions = new HashSet<Transition>();
        private final String name;

        public StateImpl(String name, Transition[] transitions) {
            if (transitions != null) Collections.addAll(this.transitions, transitions);
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
        private final boolean isEpsilon;

        public TransitionImpl(State oState) {
            this.oState = oState;
            this.matcher = null;
            this.isEpsilon = true;
        }

        public TransitionImpl(State oState, Matcher matcher) {
            this.oState = oState;
            this.matcher = matcher;
            this.isEpsilon = false;
        }

        @Override
        public State outState() {
            return oState;
        }

        @Override
        public Matcher matcher() {
            return matcher;
        }

        @Override
        public boolean isEpsilonTransition() {
            return isEpsilon;
        }

    }


    public class StringMatcher implements Matcher {

        private final char character;

        public StringMatcher(char c) {
            this.character = c;
        }

        @Override
        public boolean match() {
            return this.character == currentItem;
        }
    }

}

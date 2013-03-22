package esl.cuenet.source.selection;

import esl.cuenet.source.selection.impl.NFATImpl;
import org.junit.Test;

public class StringNFATest {

    @Test
    public void doTest() {

        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4"});
        NFATImpl nfa = new NFATImpl(states);

        nfa.constructTransition(states[0], states[1], 'a');
        nfa.constructTransition(states[1], states[2], 'b');
        nfa.constructTransition(states[2], states[4], 'c');
        nfa.constructTransition(states[4], states[0], 'd');
        nfa.constructTransition(states[2], states[3], ' ');

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[3]);

        nfa.match("abc");
    }


}

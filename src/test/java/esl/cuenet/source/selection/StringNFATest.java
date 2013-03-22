package esl.cuenet.source.selection;

import esl.cuenet.source.selection.impl.NFATImpl;
import org.junit.Test;

public class StringNFATest {

    @Test
    public void doTest() {

        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4"});
        NFATImpl nfa = new NFATImpl(states);

        NFATImpl.constructTransition(states[0], states[1], 'a');
        NFATImpl.constructTransition(states[1], states[2], 'b');
        NFATImpl.constructTransition(states[2], states[4], 'c');
        NFATImpl.constructTransition(states[4], states[0], 'd');
        NFATImpl.constructTransition(states[2], states[3], ' ');

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[3]);

        nfa.match("abc");
    }


}

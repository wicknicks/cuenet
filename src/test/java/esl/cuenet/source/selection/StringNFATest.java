package esl.cuenet.source.selection;

import esl.cuenet.source.selection.impl.NFATImpl;
import org.junit.Test;

public class StringNFATest {

    @Test
    public void doTest() {

        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4"});
        NFATImpl nfa = new NFATImpl(states);

        nfa.addTransition(states[0], states[1], 'a');
        nfa.addTransition(states[1], states[2], 'b');
        nfa.addTransition(states[2], states[4], 'c');
        nfa.addTransition(states[4], states[0], 'd');
        nfa.addTransition(states[2], states[3], NFATImpl.EPSILON);

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[3]);

        //FALSE
        test("abcdab", nfa);
        test("ab", nfa);

        //TRUE
        test("abc", nfa);
    }

    public void test(String test, NFAT nfa) {
        System.out.println(test + " " + nfa.match(test));
    }

    @Test
    public void doTest2() {
        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4", "q5"});
        NFATImpl nfa = new NFATImpl(states);

        nfa.addTransition(states[0], states[1], 'a');
        nfa.addTransition(states[0], states[2], 'a');
        nfa.addTransition(states[1], states[3], 'b');
        nfa.addTransition(states[3], states[4], 'a');
        nfa.addTransition(states[4], states[1], 'b');
        nfa.addTransition(states[2], states[5], 'b');
        nfa.addTransition(states[5], states[2], 'a');

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[1]);
        nfa.markAsFinalState(states[2]);

        //TRUE
        test("a", nfa);
        test("abab", nfa);
        test("ababa", nfa);
        test("abababa", nfa);

        //FALSE
        test("aaa", nfa);
        test("abb", nfa);

    }

    @Test
    public void testEpsilons() {
        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4", "q5"});
        NFATImpl nfa = new NFATImpl(states);

        nfa.addTransition(states[0], states[1], NFATImpl.EPSILON);
        nfa.addTransition(states[0], states[2], NFATImpl.EPSILON);
        nfa.addTransition(states[1], states[3], 'a');
        nfa.addTransition(states[3], states[1], 'b');
        nfa.addTransition(states[2], states[4], 'a');
        nfa.addTransition(states[4], states[5], 'b');
        nfa.addTransition(states[5], states[2], 'a');

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[1]);
        nfa.markAsFinalState(states[2]);

        //TRUE
        test("ab", nfa);
        test("aba", nfa);
        test("abab", nfa);
        test("abaabaaba", nfa);

        //FALSE
        test("aa", nfa);
        test("abaab", nfa);
    }


    @Test
    public void testLoops() {
        State[] states = NFATImpl.constructStates(new String[]{"q0", "q1", "q2", "q3", "q4"});
        NFATImpl nfa = new NFATImpl(states);

        nfa.addTransition(states[0], states[1], NFATImpl.EPSILON);
        nfa.addTransition(states[0], states[2], NFATImpl.EPSILON);
        nfa.addTransition(states[0], states[3], NFATImpl.EPSILON);
        nfa.addTransition(states[0], states[4], NFATImpl.EPSILON);

        nfa.addTransition(states[1], states[1], 'b');
        nfa.addTransition(states[1], states[1], 'c');
        nfa.addTransition(states[1], states[1], 'd');

        nfa.addTransition(states[2], states[2], 'a');
        nfa.addTransition(states[2], states[2], 'c');
        nfa.addTransition(states[2], states[2], 'd');

        nfa.addTransition(states[3], states[3], 'a');
        nfa.addTransition(states[3], states[3], 'b');
        nfa.addTransition(states[3], states[3], 'd');

        nfa.addTransition(states[4], states[4], 'a');
        nfa.addTransition(states[4], states[4], 'b');
        nfa.addTransition(states[4], states[4], 'c');

        nfa.markAsStartState(states[0]);
        nfa.markAsFinalState(states[1]);
        nfa.markAsFinalState(states[2]);
        nfa.markAsFinalState(states[3]);
        nfa.markAsFinalState(states[4]);

        //TRUE
        test("bcd", nfa);
        test("acd", nfa);
        test("abd", nfa);
        test("abc", nfa);
        test("abcabccbabca", nfa);
        test("abadbadbadbabdbbdabdba", nfa);
        test("dcbdcdcdbbcbbdbcdbcbdcb", nfa);
        test("acbcbcbcabababbababcbb", nfa);

        //FALSE
        test("abcd", nfa);
    }
}

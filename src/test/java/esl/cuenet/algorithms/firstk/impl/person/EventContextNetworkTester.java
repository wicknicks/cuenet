package esl.cuenet.algorithms.firstk.impl.person;

import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import org.apache.log4j.Logger;
import org.junit.Test;

public class EventContextNetworkTester {

    Logger logger = Logger.getLogger(EventContextNetworkTester.class);

    @Test
    public void unitTest() {

        Candidates candidates = Candidates.getInstance();
        candidates.createCandidate("name", "ABCD");
        candidates.createCandidate("name", "EFGH");
        candidates.createCandidate("name", "XYZ");

        EventContextNetwork network = new EventContextNetwork();
        EventContextNetwork.ECNRef p1 = network.createPerson("name", "ABCD");
        logger.info(p1);
        EventContextNetwork.ECNRef p2 = network.createPerson("name", "ABCD");
        logger.info(p2);
        EventContextNetwork.ECNRef p3 = network.createPerson("name", "ABCD");
        logger.info(p3);
        EventContextNetwork.ECNRef p4 = network.createPerson("name", "EFGH");
        logger.info(p4);
        EventContextNetwork.ECNRef p5 = network.createPerson("name", "XYZ");
        logger.info(p5);


        EventContextNetwork.ECNRef e1 = network.createEvent("photo-capture", 0, 50);
        logger.info(e1);

        EventContextNetwork.ECNRef e2 = network.createEvent("photo-capture", 20, 30, "some place");
        logger.info(e2);

        network.initializeSubeventTree(e1);

        try {
            network.initializeSubeventTree(e2);
        } catch (Exception e) {
            System.out.println("Exception caught for adding multiple trees");
        }

        network.createSubeventEdge(e1, e2);
        logger.info("Subevent added");

        network.createPartiticipationEdge(e1, p1);
        network.createPartiticipationEdge(e1, p2);
        network.createPartiticipationEdge(e1, p3);
        network.createPartiticipationEdge(e1, p4);
        network.createPartiticipationEdge(e1, p5);

        network.createPartiticipationEdge(e2, p4);
        network.createPartiticipationEdge(e2, p5);

        network.printTree(true);

        network.pruneUp();

        System.out.println("-----------------");
        System.out.println(" After prune up ");
        System.out.println("-----------------");

        network.printTree(true);

    }

}

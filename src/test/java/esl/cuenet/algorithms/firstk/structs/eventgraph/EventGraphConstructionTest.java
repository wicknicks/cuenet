package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.mapper.parser.ParseException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EventGraphConstructionTest extends TestBase {

    private Logger logger = Logger.getLogger(EventGraphConstructionTest.class);

    @Test
    public void constructSTGraphs() throws IOException, ParseException, EventGraphException {

        BaseAlgorithm baseAlgorithm = new EventGraphTest.ConcreteAlgorithmClass();
        OntModel model = baseAlgorithm.getModel();
        EventGraph graph  = new EventGraph(model);

        Event photoCapture1 = graph.createEvent("photo-capture");
        Event photoCapture2 = graph.createEvent("photo-capture");

        TimeInterval interval1 = TimeInterval.createFromInterval(500, 900, model);
        TimeInterval interval2 = TimeInterval.createFromInterval(10, 1000, model);

        logger.info("Before: " + interval1.isBefore(interval2));
        logger.info("Meets: " + interval1.meets(interval2));
        logger.info("Starts: " + interval1.starts(interval2));
        logger.info("Finish: " + interval1.finish(interval2));
        logger.info("Overlaps: " + interval1.overlaps(interval2));
        logger.info("Contains: " + interval1.contains(interval2));
        logger.info("Cotemporal: " + interval1.cotemporal(interval2));

        try {
            Location beijing = Location.createFromGPS(39.933973, 116.384777, baseAlgorithm.getModel());
            Location verano1 = Location.createFromGPS(33.643331,-117.829489, baseAlgorithm.getModel());
            Location verano2 = Location.createFromAddress("Verano Pl, Irvine, CA - 92617", baseAlgorithm.getModel());
            Location paloalto = Location.createFromAddress("Palo Alto, CA", baseAlgorithm.getModel());

            logger.info("Same City (verano1, verano2): " + verano1.getURI());
            logger.info("Same City (verano1, verano2): " + verano1.liesWithinSameCity(verano2));
            logger.info("Same City (verano1, paloalto): " + verano1.liesWithinSameCity(paloalto));
            logger.info("Same State (verano1, paloalto): " + verano1.liesWithinSameState(paloalto));
            logger.info("Same State (beijing, verano2): " + beijing.liesWithinSameCity(verano2));
            logger.info("Euclidean Distance (verano1, paloalto): " + verano1.getEuclideanDistance(paloalto) + " kms");

            photoCapture1.addResource(model.getObjectProperty(model.getNsPrefixMap().get("") + "occurs-at"), verano1);
            photoCapture2.addResource(model.getObjectProperty(model.getNsPrefixMap().get("") + "occurs-at"), verano2);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}

package esl.cuenet.algorithms.firstk.impl.person;

import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class TimeLocationParamTests {

    private Logger logger = Logger.getLogger(TimeLocationParamTests.class);

    static {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void test() throws IOException {
        Time interval1 = Time.createFromInterval(500, 900);
        Time interval2 = Time.createFromInterval(10, 1000);

        logger.info("Before: " + interval1.isBefore(interval2));
        logger.info("Meets: " + interval1.meets(interval2));
        logger.info("Starts: " + interval1.starts(interval2));
        logger.info("Finish: " + interval1.finish(interval2));
        logger.info("Overlaps: " + interval1.overlaps(interval2));
        logger.info("Contains: " + interval1.contains(interval2));
        logger.info("Cotemporal: " + interval1.cotemporal(interval2));

        Location beijing = Location.createFromGPS(39.933973, 116.384777);
        Location verano1 = Location.createFromGPS(33.643331,-117.829489);
        Location verano2 = Location.createFromAddress("Verano Pl, Irvine, CA - 92617");
        Location paloalto = Location.createFromAddress("Palo Alto, CA");

        logger.info("Same City (verano1, verano2): " + verano1.getURI());
        logger.info("Same City (verano1, verano2): " + verano1.liesWithinSameCity(verano2));
        logger.info("Same City (verano1, paloalto): " + verano1.liesWithinSameCity(paloalto));
        logger.info("Same zipcode (verano1, verano2" + verano1.sameZipcode(verano2));
        logger.info("Same State (verano1, paloalto): " + verano1.liesWithinSameState(paloalto));
        logger.info("Same State (beijing, verano2): " + beijing.liesWithinSameCity(verano2));
        logger.info("Euclidean Distance (verano1, paloalto): " + verano1.getEuclideanDistance(paloalto) + " kms");

    }
}

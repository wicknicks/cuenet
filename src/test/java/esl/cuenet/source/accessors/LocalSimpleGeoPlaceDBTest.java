package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class LocalSimpleGeoPlaceDBTest {

    private Logger logger = Logger.getLogger(LocalSimpleGeoPlaceDBTest.class);

    public LocalSimpleGeoPlaceDBTest() {
        super();
    }

    @Test
    public void runTest() throws IOException {

        LocalSimpleGeoPlaceDB localSimpleGeoPlaceDB = new LocalSimpleGeoPlaceDB();
        BasicDBList list = localSimpleGeoPlaceDB.nearbyPlaces(33.642795, -117.845196);
        for (Object o: list) {
            BasicDBObject object = (BasicDBObject) o;
            logger.info(o.toString());
        }
    }

}

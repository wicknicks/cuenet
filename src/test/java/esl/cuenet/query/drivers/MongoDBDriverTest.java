package esl.cuenet.query.drivers;

import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

public class MongoDBDriverTest extends TestBase {

    private Logger logger = Logger.getLogger(MongoDBDriverTest.class);

    @Test
    public void doConnectTest() {

        MongoDB driver = new MongoDB("test");
        driver.close();

    }

}

package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;
import org.junit.Test;

public class FacebookRelationAccessorTest {

    Logger logger = Logger.getLogger(FacebookRelationAccessorTest.class);
    
    @Test
    public void doTest() throws SourceQueryException {

        FacebookRelationAccessor accessor = new FacebookRelationAccessor();

        accessor.start();
        IResultSet rs = accessor.executeQuery(717562539);
        BasicDBList k = (BasicDBList) JSON.parse(rs.printResults());
        logger.info("717562539 has " + k.size() + " relationships");

        rs = accessor.executeQuery(111290);
        logger.info(rs.printResults());

        rs = accessor.executeQuery(570206500);
        logger.info(rs.printResults());

    }

}

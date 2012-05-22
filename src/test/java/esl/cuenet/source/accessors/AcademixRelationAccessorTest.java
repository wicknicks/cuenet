package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.TestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AcademixRelationAccessorTest extends TestBase {

    private Logger logger = Logger.getLogger(AcademixRelationAccessorTest.class);

    @Test
    public void doTest() throws IOException, ParseException, SourceQueryException {
        TestAlgorithm ta = new TestAlgorithm();
        AcademixRelationAccessor axr = new AcademixRelationAccessor(ta.getModel());
        IResultSet rs = axr.executeQuery("Martin Kersten");
        IResultIterator rit = rs.iterator();

        List<String> projectVarURIs = new ArrayList<String>();
        projectVarURIs.add(Constants.CuenetNamespace + "person");

        while(rit.hasNext()) {
            Map<String, List<Individual>> res = rit.next(projectVarURIs);
            List<Individual> inds = res.get(Constants.CuenetNamespace + "person");
            if (inds == null) logger.info("inds is null");
            else logger.info("inds.size = " + inds.size());
        }
    }

}

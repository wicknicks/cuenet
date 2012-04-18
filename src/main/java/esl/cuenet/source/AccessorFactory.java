package esl.cuenet.source;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import esl.cuenet.source.accessors.*;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class AccessorFactory {

    private static AccessorFactory factoryInstance = new AccessorFactory();
    private Logger logger = Logger.getLogger(AccessorFactory.class);
    private OntModel model = null;

    private AccessorFactory() {
        model = ModelFactory.createOntologyModel();
        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static AccessorFactory getInstance() {
        return factoryInstance;
    }

    public IAccessor getAccessor(String name) {
        if (name.compareTo("yahoo-geocoder") == 0) {
            logger.info("Initializing YahooPlaceFinderAPI Accessor");
            return new YahooPlaceFinderAPI(model);
        }

        else if (name.compareTo("places-db") == 0) {
            logger.info("Initializing LocalSimpleGeoPlaceDB Accessor");
            return new LocalSimpleGeoPlaceDB(model);
        }

        else if (name.compareTo("google-calendar") == 0) {
            logger.info("Initializing GoogleCalendarCollection Accessor");
            return new GoogleCalendarCollection(model);
        }

        else if (name.compareTo("fb-user") == 0) {
            logger.info("Initializing FacebookUser Accessor");
            return new FacebookUserAccessor(model);
        }

        else if (name.compareTo("fb-relation") == 0) {
            logger.info("Initializing FacebookRelation Accessor");
            return new FacebookRelationAccessor(model);
        }

        return null;
    }

}

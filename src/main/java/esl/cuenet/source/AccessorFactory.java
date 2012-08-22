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

        else if (name.compareTo("email") == 0) {
            logger.info("Initializing Email Accessor");
            return new EmailAccessor(model);
        }

        else if (name.compareTo("conferences") == 0) {
            logger.info("Initializing Conference Accessor");
            return new ConferenceAttendeeAccessor(model);
        }

        else if (name.compareTo("academix") == 0) {
            logger.info("Initializing Academix Relations Accessor");
            return new AcademixRelationAccessor(model);
        }

        else if (name.compareTo("keynotes") == 0) {
            logger.info("Initializing Keynote Subevent Relations Accessor");
            return new KeynoteAccessor(model);
        }

        else if (name.compareTo("talks") == 0) {
            logger.info("Initializing Keynote Subevent Relations Accessor");
            return new ConferenceTalkAccessor(model);
        }

        else if (name.compareTo("sessions") == 0) {
            logger.info("Initializing Keynote Subevent Relations Accessor");
            return new SessionAccessor(model);
        }

        else if (name.compareTo("conflunches") == 0) {
            logger.info("Initializing Keynote Subevent Relations Accessor");
            return new ConferenceLunchAccessor(model);
        }

        else if (name.compareTo("tweets") == 0) {
            logger.info("Initializing Tweet Accessor");
            return new ConferenceTweetAccessor(model);
        }

        return null;
    }

}

package esl.cuenet.source;

import esl.cuenet.source.accessors.*;
import org.apache.log4j.Logger;

public class AccessorFactory {

    private static AccessorFactory factoryInstance = new AccessorFactory();
    private Logger logger = Logger.getLogger(AccessorFactory.class);

    private AccessorFactory() {

    }

    public static AccessorFactory getInstance() {
        return factoryInstance;
    }

    public IAccessor getAccessor(String name) {
        if (name.compareTo("yahoo-geocoder") == 0) {
            logger.info("Initializing YahooPlaceFinderAPI Accessor");
            return new YahooPlaceFinderAPI();
        }

        else if (name.compareTo("places-db") == 0) {
            logger.info("Initializing LocalSimpleGeoPlaceDB Accessor");
            return new LocalSimpleGeoPlaceDB();
        }

        else if (name.compareTo("google-calendar") == 0) {
            logger.info("Initializing GoogleCalendarCollection Accessor");
            return new GoogleCalendarCollection();
        }

        else if (name.compareTo("fb-user") == 0) {
            logger.info("Initializing FacebookUser Accessor");
            return new FacebookUserAccessor();
        }

        else if (name.compareTo("fb-relation") == 0) {
            logger.info("Initializing FacebookRelation Accessor");
            return new FacebookRelationAccessor();
        }

        return null;
    }

}

package esl.cuenet.source;

import esl.cuenet.source.accessors.GoogleCalendarCollection;
import esl.cuenet.source.accessors.LocalSimpleGeoPlaceDB;
import esl.cuenet.source.accessors.UpcomingEventsAPI;
import esl.cuenet.source.accessors.YahooPlaceFinder;

import java.util.HashMap;

public class AccessorFactory {

    private static HashMap<String, Class> accessorMap = new HashMap<String, Class>();

    private AccessorFactory() {
        accessorMap.put("yahoo-geocoder", YahooPlaceFinder.class);
        accessorMap.put("places-db", LocalSimpleGeoPlaceDB.class);
        accessorMap.put("google-calendar", GoogleCalendarCollection.class);
        accessorMap.put("upcoming-events", UpcomingEventsAPI.class);
    }

    public static AccessorFactory INSTANCE = new AccessorFactory();
    
    public Class getClassForSource(String name) {
        return accessorMap.get(name);
    }

}

package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.webjson.HttpDownloader;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class UpcomingEventsAPI implements IAccessor {

    private Logger logger = Logger.getLogger(UpcomingEventsAPI.class);
    private HttpDownloader downloader = new HttpDownloader();
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[6]; //lat, lon, start-date, end-date, name, description

    private double lat, lon;
    private String startDate, endDate;
    private String nameSubstring, descriptionSubstring;

    private OntModel model = null;

    public UpcomingEventsAPI() {}

    public UpcomingEventsAPI(OntModel model) {
        this.model = model;
    }

    // startDate, endDate format: YYYY-MM-DD
    public BasicDBList searchUpcoming() throws IOException {

        int radius = 5;

        StringBuilder url = new StringBuilder("http://upcoming.yahooapis.com/services/rest/?method=event.search");
        url.append("&api_key=123157aeb5");
        if (setFlags[0] && setFlags[1]) url.append("&location=").append(lat).append(',').append(lon);
        url.append("&radius=").append(radius);

        if (setFlags[2]) url.append("&min_date=").append(startDate);
        if (setFlags[3]) url.append("&max_date=").append(endDate);
        url.append("&category_id=").append("1,2");
        url.append("&format=json");

        logger.info("url: " + url.substring(0));
        byte[] contents = downloader.get(url.substring(0));

        if (contents == null) return null;
        if (contents.length < 5) return null;

        BasicDBObject object = (BasicDBObject) JSON.parse(new String(contents));

        if (!object.containsField("rsp")) return null;
        BasicDBObject response = (BasicDBObject) object.get("rsp");

        if (response.getString("stat").compareTo("ok") != 0) {
            logger.info("No events on UpcomingEventsAPI");
            return null;
        }

        int rc = response.getInt("resultcount");
        if (rc < 1) return null;

        if (!response.containsField("event")) return null;
        
        BasicDBList events = (BasicDBList) response.get("event");
        BasicDBList filteredEvents = new BasicDBList();
        
        for (Object e: events) {
            BasicDBObject event = (BasicDBObject) e;
            if (setFlags[4]) if ( !event.getString("name").contains(nameSubstring) ) continue;
            if (setFlags[5]) if ( !event.getString("description").contains(descriptionSubstring) ) continue;
            filteredEvents.add(event);
        }
        
        return filteredEvents;

    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        if (attributes.length != 6) throw
                new AccesorInitializationException("Invalid number of attributes for "
                        + UpcomingEventsAPI.class.getName());

        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i = 0; i < setFlags.length; i++) setFlags[i] = false;
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No time interval attributes in "
                + YahooPlaceFinderAPI.class.getName());

    }

    @Override
    public void associateLocation(Attribute attribute, Location timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No location attributes in "
                + YahooPlaceFinderAPI.class.getName());

    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2]) == 0) {
            setFlags[2] = true;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value);
            startDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
        } else if (attribute.compareTo(attributes[3]) == 0) {
            setFlags[3] = true;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value);
            endDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
        }

        else throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                    + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[4]) == 0) {
            setFlags[4] = true;
            nameSubstring = value;
        } else if (attribute.compareTo(attributes[5]) == 0) {
            setFlags[5] = true;
            descriptionSubstring = value;
        }

        else throw new AccesorInitializationException("String value being initialized for wrong attribute "
                    + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0]) == 0) {
            setFlags[0] = true;
            this.lat = value;
        }
        else if (attribute.compareTo(attributes[1])==0) {
            setFlags[1] = true;
            this.lon = value;
        }

        else throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                    + YahooPlaceFinderAPI.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        boolean areParamsAvailable = false;
        for (boolean b : setFlags) if (b) areParamsAvailable = true;
        if (!areParamsAvailable) throw new SourceQueryException("No parameters set");

        BasicDBList result;

        try {
            result = searchUpcoming();
        } catch (IOException e) {
            throw new SourceQueryException("Internal IOException: " + e.getMessage());
        }

        if (result != null) return new ResultSetImpl(result.toString(), model);
        return null;
    }

    public BasicDBList searchUpcoming(double lat, double lon, String startDate, String endDate) throws IOException {
        this.lat = lat; setFlags[0] = true;
        this.lon = lon; setFlags[1] = true;
        this.startDate = startDate; setFlags[2] = true;
        this.endDate = endDate; setFlags[3] = true;
        return searchUpcoming();
    }

}

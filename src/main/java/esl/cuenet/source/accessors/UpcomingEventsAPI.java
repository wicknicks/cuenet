package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.webjson.HttpDownloader;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class UpcomingEventsAPI implements IAccessor {
    
    private Logger logger = Logger.getLogger(UpcomingEventsAPI.class);
    private HttpDownloader downloader = new HttpDownloader();

    // startDate, endDate format: YYYY-MM-DD
    public BasicDBList searchUpcoming(double lat, double lon, String startDate, String endDate) throws IOException {

        int radius = 5;

        StringBuilder url = new StringBuilder("http://upcoming.yahooapis.com/services/rest/?method=event.search");
        url.append("&api_key=123157aeb5");
        url.append("&location=").append(lat).append(',').append(lon);
        url.append("&radius=").append(radius);

        url.append("&min_date=").append(startDate);
        url.append("&max_date=").append(endDate);
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
        return (BasicDBList) response.get("event");

    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void start() {
        //todo: implement method

    }

    @Override
    public void associateInt(Attribute attribute, int value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        //todo: implement method
        return null;
    }
}

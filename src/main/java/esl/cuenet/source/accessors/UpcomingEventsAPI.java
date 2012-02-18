package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.drivers.webjson.HttpDownloader;
import org.apache.log4j.Logger;

import java.io.IOException;

public class UpcomingEventsAPI {
    
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

}

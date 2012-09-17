package esl.datastructures.util;

import com.mongodb.BasicDBObject;
import esl.cuenet.query.drivers.mongodb.MongoDB;

import java.util.HashMap;

public class GeocodingCache extends MongoDB {

    public GeocodingCache() {
        super("geo");
    }

    public void addToCache(String location, BasicDBObject gcInfo) {
        DBWriter writer = startWriter("geocode_cache");
        BasicDBObject gcObject = new BasicDBObject("location", location);
        gcObject.put("gc", gcInfo);
        writer.write(gcObject);
    }

    public BasicDBObject getFromCache(String location) {
        DBReader reader = startReader("geocode_cache");
        reader.query(new BasicDBObject("location", location));

        while(reader.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) reader.next();
            BasicDBObject gcInfo = (BasicDBObject) dbo.get("gc");
            if (gcInfo != null) return gcInfo;
        }

        return null;
    }

    public HashMap<String, BasicDBObject> getAll() {
        DBReader reader = startReader("geocode_cache");
        reader.query(new BasicDBObject());

        HashMap<String, BasicDBObject> map = new HashMap<String, BasicDBObject>(200);

        while(reader.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) reader.next();
            String location = dbo.getString("location");
            BasicDBObject gcInfo = (BasicDBObject) dbo.get("gc");
            map.put(location, gcInfo);
        }

        return map;
    }
}

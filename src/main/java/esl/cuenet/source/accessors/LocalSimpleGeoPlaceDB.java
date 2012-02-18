package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.drivers.mongodb.MongoDB;

public class LocalSimpleGeoPlaceDB extends MongoDB {

    public LocalSimpleGeoPlaceDB() {
        super("test");
    }

    @Override
    public void open(String databaseName) {
        openDB("128.195.52.175", databaseName);
    }

    public BasicDBList nearbyPlaces(double lat, double lon) {
        DBReader cursor = startReader("places");
        cursor.query(new BasicDBObject());

        double rad = 0.002;
        BasicDBObject queryObject = (BasicDBObject) JSON.parse("{\"geometry.coordinates\": {\"$within\": {\"$center\": [[" +
                +lon + ", " + lat + "], " + rad + "]}}}");
        cursor.query(queryObject);

        BasicDBList places = new BasicDBList();

        int i = 0;
        while (cursor.hasNext()) {
            BasicDBObject record = (BasicDBObject) cursor.next();
            BasicDBObject props = (BasicDBObject) record.get("properties");

            BasicDBObject p = new BasicDBObject();
            p.put("name", props.getString("name"));

            BasicDBList classifiers = (BasicDBList) props.get("classifiers");

            if (classifiers != null &&
                    classifiers.size() > 0 &&
                    classifiers.get(0) != null &&
                    ((BasicDBObject)classifiers.get(0)).getString("category") != null) {
                p.put("category", ((BasicDBObject)classifiers.get(0)).getString("category"));
            }

            places.add(p);
            i++;
        }

        return places;

    }

}

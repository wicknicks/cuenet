package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;

public class LocalSimpleGeoPlaceDB extends MongoDB implements IAccessor {

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

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        //todo: implement method

    }

    @Override
    public void start() {
        //todo: implement method

    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
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

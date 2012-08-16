package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;

import java.util.List;

public class LocalSimpleGeoPlaceDB extends MongoDB implements IAccessor {

    private Attribute[] attributes;
    private boolean[] setFlags = new boolean[3];
    private Double lat, lon;
    private String address;
    private OntModel model = null;

    public LocalSimpleGeoPlaceDB() {
        super(AccessorConstants.DBNAME);
    }

    public LocalSimpleGeoPlaceDB(OntModel model) {
        this();
        this.model = model;
    }

    @Override
    public void open(String databaseName) {
        openDB("128.195.52.175", databaseName);
    }

    public BasicDBList nearbyPlaces() {
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
        if (attributes.length != 3) throw
                new AccesorInitializationException("Invalid number of attributes for "
                        + YahooPlaceFinderAPI.class.getName());

        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
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
        throw new AccesorInitializationException("Incorrect Assignment: No integer attributes in "
                + LocalSimpleGeoPlaceDB.class.getName());

    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[2]) == 0) {
            address = value;
            setFlags[2] = true;
        }


        throw new AccesorInitializationException("Incorrect Assignment: No string attributes in "
                + LocalSimpleGeoPlaceDB.class.getName());

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
        return new ResultSetImpl(nearbyPlaces().toString());
    }

    public BasicDBList nearbyPlaces(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return nearbyPlaces();
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        private ResultIterator resultIterator = new ResultIterator(model);

        public ResultSetImpl (String result) {this.result = result;}

        public void addResult(List<Individual> individuals) {
            this.resultIterator.add(individuals);
        }

        @Override
        public String printResults() {
            return result;
        }

        @Override
        public IResultIterator iterator() {
            return resultIterator;
        }
    }
}

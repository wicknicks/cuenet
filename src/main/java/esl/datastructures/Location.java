package esl.datastructures;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.datastructures.util.YahooPlaceFinderReverseGeo;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Location extends IndividualImpl {

    private double lat, lon;
    private String fullAddress;
    private String city;
    private String state;
    private String country;
    private String zipcode;

    private String id = null;
    private static HashMap<String, Location> lCache = new HashMap<String, Location>();

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    protected Location(Node n, EnhGraph g, double lat, double lon, String _id) throws IOException {
        super(n, g);
        this.lat = lat;
        this.lon = lon;
        BasicDBObject rgeo = YahooPlaceFinderReverseGeo.reverseGeoCode(lat, lon);

        if (rgeo.containsField("city")) city = rgeo.getString("city");
        if (rgeo.containsField("state")) state = rgeo.getString("state");
        if (rgeo.containsField("country")) country = rgeo.getString("country");
        if (rgeo.containsField("uzip")) zipcode = rgeo.getString("uzip");

        fullAddress = "";
        if (rgeo.containsField("line1")) fullAddress += rgeo.getString("line1") + " ";
        if (rgeo.containsField("line2")) fullAddress += rgeo.getString("line2") + " ";
        if (rgeo.containsField("line3")) fullAddress += rgeo.getString("line3") + " ";
        if (rgeo.containsField("line4")) fullAddress += rgeo.getString("line4");
        fullAddress = fullAddress.trim();

        this.id = _id;
        lCache.put(id, this);
    }

    public String getID() {
        return id;
    }

    public static Location getFromCache(String id) {
        return lCache.get(id);
    }

    protected Location(Node n, EnhGraph g, String address, String _id) throws IOException {
        super(n, g);
        this.fullAddress = address;
        BasicDBObject rgeo = YahooPlaceFinderReverseGeo.geoCode(address);

        if (rgeo.containsField("city")) city = rgeo.getString("city");
        if (rgeo.containsField("state")) state = rgeo.getString("state");
        if (rgeo.containsField("country")) country = rgeo.getString("country");
        if (rgeo.containsField("uzip")) zipcode = rgeo.getString("uzip");
        if (rgeo.containsField("latitude")) lat = Double.parseDouble(rgeo.getString("latitude"));
        if (rgeo.containsField("longitude")) lon = Double.parseDouble(rgeo.getString("longitude"));

        this.id = _id;
        lCache.put(id, this);
    }

    public static Location createFromGPS(double lat, double lon, OntModel graph) throws IOException {
        String id = UUID.randomUUID().toString();
        return new Location(new LocationNodeURI(id), (EnhGraph) graph, lat, lon, id);
    }

    public static Location createFromAddress(String address, OntModel graph) throws IOException {
        String id = UUID.randomUUID().toString();
        return new Location(new LocationNodeURI(id), (EnhGraph) graph, address, id);
    }

    private static class LocationNodeURI extends Node_URI {
        protected LocationNodeURI(String id) {
            super(Constants.DOLCELocationURI + "_" + id);
        }
    }

    public double getEuclideanDistance(Location other) {
        double radiusOfEarthInKms = 6378.137;
        double distance = Math.acos(Math.sin(DtoR(this.lat))*Math.sin(DtoR(other.lat)) +
                Math.cos(DtoR(this.lat))*Math.cos(DtoR(other.lat))*Math.cos(DtoR(other.lon)-DtoR(this.lon)));
        return (distance * radiusOfEarthInKms);
    }

    public boolean liesWithinSameCity(Location other) {
        return (this.country.compareTo(other.country) == 0) && (this.city.compareTo(other.city) == 0);
    }

    public boolean liesWithinSameState(Location other) {
        return (this.country.compareTo(other.country) == 0) && (this.state.compareTo(other.state) == 0);
    }

    private double DtoR(double d) {
        return d*0.0174532925;
    }

    @Override
    public OntClass getOntClass() {
        return getOntModel().getOntClass(Constants.DOLCELocationURI);
    }
}

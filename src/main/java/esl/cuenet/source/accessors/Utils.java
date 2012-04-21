package esl.cuenet.source.accessors;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.datastructures.Location;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Utils {

    private static Logger logger = Logger.getLogger(Utils.class);

    public static Individual createPersonFromFacebookRecord(BasicDBObject personDBObject, OntModel model) {
        OntClass person = model.getOntClass(Constants.CuenetNamespace + "person");
        DatatypeProperty nameProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "name");
        DatatypeProperty emailProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "email");
        ObjectProperty livesAt = model.getObjectProperty(
                Constants.CuenetNamespace + "lives-at");
        ObjectProperty worksAt = model.getObjectProperty(
                Constants.CuenetNamespace + "works-at");

        Individual personIndividual = person.createIndividual(Constants.CuenetNamespace + "fb_" + personDBObject.get("id"));

        if (personDBObject.containsField("name"))
            personIndividual.addLiteral(nameProperty, model.createTypedLiteral(personDBObject.getString("name")));
        if (personDBObject.containsField("email"))
            personIndividual.addLiteral(nameProperty, model.createTypedLiteral(personDBObject.getString("email")));
        if (personDBObject.containsField("location")) {
            BasicDBObject loc = (BasicDBObject) personDBObject.get("location");
            if (loc.containsField("name")) {
                Location location = null;
                try {
                    location = Location.createFromAddress(loc.getString("name"), (EnhGraph) model);
                } catch (IOException e) {
                    logger.warn("Location Fetch Failed: " + e.getMessage());
                }
                if (location != null) personIndividual.addProperty(livesAt, location);
            }
        }

        if (personDBObject.containsField("work")) {
            BasicDBList workplacelist = (BasicDBList) personDBObject.get("work");
            for (Object o: workplacelist) {
                BasicDBObject workplace = (BasicDBObject) o;
                if (workplace.containsField("location")) {
                    BasicDBObject workplaceLocation = (BasicDBObject) workplace.get("location");
                    String name =  workplaceLocation.getString("name");
                    if (name != null) {
                        Location wp = null;
                        try {
                            wp = Location.createFromAddress(name, (EnhGraph) model);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (wp != null) personIndividual.addProperty(worksAt, wp);
                    }
                }
            }
        }

        return personIndividual;
    }

}

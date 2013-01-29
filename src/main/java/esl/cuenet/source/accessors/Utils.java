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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class Utils {

    private static Logger logger = Logger.getLogger(Utils.class);
    private static MailDateFormat format = new MailDateFormat();

    public static Individual createPersonFromNameEmail(String email, String name, OntModel model) {
        OntClass person = model.getOntClass(Constants.CuenetNamespace + "person");
        DatatypeProperty nameProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "name");
        DatatypeProperty emailProperty = model.getDatatypeProperty(
                Constants.CuenetNamespace + "email");

        String id = (name != null) ? name : null;
        if (id == null) id = (email != null) ? email : "";

        Individual personIndividual = person.createIndividual(Constants.CuenetNamespace + id);

        if (name != null)
            personIndividual.addLiteral(nameProperty, model.createTypedLiteral(name));
        if (email != null)
            personIndividual.addLiteral(emailProperty, model.createTypedLiteral(email));

        return personIndividual;
    }

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
            personIndividual.addLiteral(emailProperty, model.createTypedLiteral(personDBObject.getString("email")));
        if (personDBObject.containsField("location")) {
            BasicDBObject loc = (BasicDBObject) personDBObject.get("location");
            if (loc.containsField("name")) {
                Location location = null;
                try {
                    location = Location.createFromAddress(loc.getString("name"), model);
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
                            wp = Location.createFromAddress(name, model);
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


    public static List<Map.Entry<String, String>> parseEmailAddresses(String addressList) {
        List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
        try {
            for (InternetAddress ia: InternetAddress.parse(addressList)) {
                Map.Entry<String, String> entry =
                        new AbstractMap.SimpleEntry<java.lang.String, java.lang.String>(ia.getAddress(), ia.getPersonal());
                entries.add(entry);
            }
        } catch (AddressException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static Date parseEmailDate (String sDate) {

        if (sDate == null) return new Date(0);

        Date dt;
        try {
            dt = format.parse(sDate);
        } catch (ParseException e) {
            logger.error("Date couldn't be parsed: "  + sDate);
            logger.error("MESSAGE: " + e.getMessage());
            dt = new Date(0);
        }

        return dt;
    }

}

package esl.cuenet.ranking.sources;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.ranking.SourceInstantiator;
import esl.cuenet.source.accessors.AccessorConstants;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

import javax.mail.internet.MailDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacebookPhotoSource extends MongoDB implements SourceInstantiator {

    private Logger logger = Logger.getLogger(FacebookPhotoSource.class);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public FacebookPhotoSource() {
        super(AccessorConstants.DBNAME);
    }

    private Date parseDate(String sDate) {
        if (sDate == null) return new Date(0);

        Date dt;
        try {
            dt = dateFormat.parse(sDate);
        } catch (ParseException e) {
            logger.error("Date couldn't be parsed: "  + sDate);
            logger.error("MESSAGE: " + e.getMessage());
            dt = new Date(0);
        }

        return dt;

    }

    @Override
    public void populate(OntModel model) {
        DBReader reader = this.startReader("fb_photos");
        BasicDBObject keys = new BasicDBObject();
        keys.put("_id", 0);

        reader.getAll(keys);

        OntClass photoCaptureEventClass = model.getOntClass(Constants.CuenetNamespace + Constants.PhotoCaptureEvent);
        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + Constants.Person);
        Property occursDuringProperty = model.getProperty(Constants.CuenetNamespace + Constants.OccursDuring);
        Property participatesInProperty = model.getProperty(Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn);
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");

        String tagId, tagName, date, photoId, ownerId;
        int c = 0;
        while (reader.hasNext()) {
            BasicDBObject obj = (BasicDBObject) reader.next();

            photoId = obj.getString("id");

            if ( !obj.containsField("created_time") ) {
                logger.error("object doesn't have 'created_time' field: " + obj);
                continue;
            }
            date = obj.getString("created_time");

            Individual photoCapture = photoCaptureEventClass.createIndividual(Constants.CuenetNamespace +
                    Constants.PhotoCaptureEvent + "_" + photoId);
            photoCapture.addProperty(occursDuringProperty,
                    TimeInterval.createFromMoment(parseDate(date).getTime(), model));

            if (obj.containsField("tags")) {
                if (obj.containsField("data")) {
                    for (Object o: (BasicDBList) obj.get("tags.data")) {
                        tagId = null; tagName = null;
                        BasicDBObject tag = (BasicDBObject) o;
                        if (tag.containsField("id")) tagId = tag.getString("id");
                        if (tag.containsField("name")) tagName = tag.getString("name");
                        if (tagId == null) continue;
                        Individual person = personClass.createIndividual(Constants.CuenetNamespace + Constants.Person + "_" + tagId + "_" + (c++));
                        if (tagName != null) person.addProperty(nameProperty, tagName);
                        photoCapture.addProperty(participatesInProperty, person);
                    }
                }
            }
        }
    }
}

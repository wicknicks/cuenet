package esl.cuenet.ranking.sources;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.ranking.EntityBase;
import esl.cuenet.ranking.EventEntityNetwork;
import esl.cuenet.ranking.SourceInstantiator;
import esl.cuenet.ranking.URINode;
import esl.cuenet.ranking.network.OntProperties;
import esl.cuenet.source.accessors.AccessorConstants;
import esl.cuenet.source.accessors.Utils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FacebookPhotoSource extends MongoDB implements SourceInstantiator {

    private Logger logger = Logger.getLogger(FacebookPhotoSource.class);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private HashMap<String, URINode> userIDNodeMap = new HashMap<String, URINode>(500);

    public FacebookPhotoSource() {
        super(AccessorConstants.DBNAME);
    }

    private Date parseFBDate(String sDate) {
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
    public void populate(EventEntityNetwork network, EntityBase entityBase) {
        userIDNodeMap.clear();
        DBReader reader = this.startReader("fb_photos");
        BasicDBObject keys = new BasicDBObject();
        keys.put("_id", 0);

        reader.getAll(keys);

        logger.info(" Total number of photos: " + reader.count());

        String occursDuringPropertyURI = Constants.CuenetNamespace + Constants.OccursDuring;
        String participatesInPropertyURI = Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn;
        String namePropertyURI = Constants.CuenetNamespace + "name";

        String tagId, tagName, date, photoId, ownerId;
        int c = 0; int ix = 0;
        while (reader.hasNext()) {
            BasicDBObject obj = (BasicDBObject) reader.next();

            photoId = obj.getString("id");

            if ( !obj.containsField("created_time") ) {
                logger.error("object doesn't have 'created_time' field: " + obj);
                continue;
            }
            date = obj.getString("created_time");

            URINode photoCaptureInstance = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                    Constants.PhotoCaptureEvent + "_" + photoId);
            URINode timeInterval = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                    Constants.TimeInterval);

            long t = parseFBDate(date).getTime();
            timeInterval.
                    createEdgeTo(SourceHelper.createLiteral(network, t)).
                    setProperty(OntProperties.ONT_URI, Constants.CuenetNamespace + Constants.TimestampMillisStart);
            timeInterval.
                    createEdgeTo(SourceHelper.createLiteral(network, t)).
                    setProperty(OntProperties.ONT_URI, Constants.CuenetNamespace + Constants.TimestampMillisEnd);
            photoCaptureInstance.createEdgeTo(timeInterval).setProperty(OntProperties.ONT_URI, occursDuringPropertyURI);

            if (obj.containsField("tags")) {
                BasicDBObject _tags = (BasicDBObject) obj.get("tags");
                if (_tags.containsField("data")) {
                    for (Object o: (BasicDBList) _tags.get("data")) {

                        tagId = null; tagName = null;
                        BasicDBObject tag = (BasicDBObject) o;

                        if (tag.containsField("id")) tagId = tag.getString("id");
                        if (tag.containsField("name")) tagName = tag.getString("name");
                        if (tagId == null) continue;

                        URINode personInstance;
                        if (userIDNodeMap.containsKey(tagId)) personInstance = userIDNodeMap.get(tagId);
                        else {
                            personInstance = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                                    Constants.Person + "_" + tagId + "_" + (c++));
                        }
                        if (tagName != null) {
                            personInstance.
                                    createEdgeTo(SourceHelper.createLiteral(network, tagName)).
                                    setProperty(OntProperties.ONT_URI, namePropertyURI);
                        }

                        URINode entityNode = entityBase.lookup(EntityBase.V_FB_ID, tagId);
                        if (entityNode != null) {
                            personInstance.createEdgeTo(entityNode)
                                    .setProperty(OntProperties.TYPE, OntProperties.IS_SAME_AS);
                        }

                        personInstance.createEdgeTo(photoCaptureInstance).
                                setProperty(OntProperties.ONT_URI, participatesInPropertyURI);

                    }
                }
            }

            if (ix % 1000 == 0) logger.info("Added " + ix + " photos");
            ix += 1;
        }

        logger.info("FacebookPhotoSource import complete");

    }
}

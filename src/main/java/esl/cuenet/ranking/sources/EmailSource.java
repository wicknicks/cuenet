package esl.cuenet.ranking.sources;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailSource extends MongoDB implements SourceInstantiator {

    private Logger logger = Logger.getLogger(EmailSource.class);

    public EmailSource() {
        super(AccessorConstants.DBNAME);
    }

    @Override
    public void populate(EventEntityNetwork network, EntityBase entityBase) {

        DBReader reader = this.startReader("emails");
        BasicDBObject keys = new BasicDBObject();
        keys.put("_id", 0);

        reader.getAll(keys);

        String date, to, from, cc, uid;
        String occursDuringPropertyURI = Constants.CuenetNamespace + Constants.OccursDuring;
        String participatesInPropertyURI = Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn;
        String namePropertyURI = Constants.CuenetNamespace + "name";
        String emailPropertyURI = Constants.CuenetNamespace + "email";

        //make copy of dbObjects. Mongo gc trashes cursors which are inactive for > 10mins.
        List<BasicDBObject> dbObjects = new ArrayList<BasicDBObject>();
        while (reader.hasNext()) {
            BasicDBObject obj = (BasicDBObject) reader.next();
            dbObjects.add(obj);
        }

        logger.info(" Total number of emails: " + dbObjects.size());

        int c = 0; int ix = 0;
        network.startBulkLoad();

        for (BasicDBObject obj: dbObjects) {
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();

            uid = obj.getString("uid");

            to = obj.getString("to");
            if (to != null) entries.addAll(Utils.parseEmailAddresses(to));

            from = obj.getString("from");
            if (from != null) entries.addAll(Utils.parseEmailAddresses(from));

            cc = obj.getString("cc");
            if (cc != null) entries.addAll(Utils.parseEmailAddresses(cc));

            date = obj.getString("date");

            URINode emailInstance = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                    Constants.EmailExchangeEvent + "_" + uid);

            URINode timeInterval = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                    Constants.TimeInterval);
            long t = Utils.parseEmailDate(date).getTime();
            timeInterval.
                    createEdgeTo(SourceHelper.createLiteral(network, t)).
                    setProperty(OntProperties.ONT_URI, Constants.CuenetNamespace + Constants.TimestampMillisStart);
            timeInterval.
                    createEdgeTo(SourceHelper.createLiteral(network, t)).
                    setProperty(OntProperties.ONT_URI, Constants.CuenetNamespace + Constants.TimestampMillisEnd);
            emailInstance.createEdgeTo(timeInterval).setProperty(OntProperties.ONT_URI, occursDuringPropertyURI);

            for (Map.Entry<String, String> entry: entries) {
                URINode personInstance = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                        Constants.Person + "_" + c);
                boolean f = false;

                if (entry.getKey() != null) {
                    personInstance.
                            createEdgeTo(SourceHelper.createLiteral(network, entry.getKey())).
                            setProperty(OntProperties.ONT_URI, namePropertyURI);

                    URINode entityNode = entityBase.lookup(EntityBase.V_EMAIL, entry.getKey());
                    if (entityNode != null) {
                        personInstance.createEdgeTo(entityNode)
                                .setProperty(OntProperties.TYPE, OntProperties.IS_SAME_AS);
                        f = true;
                    }
                }
                if (entry.getValue() != null) {
                    personInstance.
                            createEdgeTo(SourceHelper.createLiteral(network, entry.getValue())).
                            setProperty(OntProperties.ONT_URI, emailPropertyURI);
                }

                if ( entry.getValue() != null && !f )
                    logger.info("Didn't create link for = " + entry.getKey() + " " + entry.getValue());

                personInstance.createEdgeTo(emailInstance).
                        setProperty(OntProperties.ONT_URI, participatesInPropertyURI);
                c += 1;
            }

            if (ix % 1000 == 0) {
                logger.info("Added " + ix + " mails");
                network.flush();
            }
            ix += 1;
            //if (ix % 2000 == 0) break; //for testing
        }

        network.finishBulkLoad();
        logger.info("EmailSource import complete");
    }

}

package esl.cuenet.ranking.sources;

import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.drivers.mongodb.MongoDB;
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
    public void populate(EventEntityNetwork network) {

        DBReader reader = this.startReader("emails");
        BasicDBObject keys = new BasicDBObject();
        keys.put("_id", 0);

        reader.getAll(keys);

        String date, to, from, cc, uid;
        String occursDuringPropertyURI = Constants.CuenetNamespace + Constants.OccursDuring;
        String participatesInPropertyURI = Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn;
        String namePropertyURI = Constants.CuenetNamespace + "name";
        String emailPropertyURI = Constants.CuenetNamespace + "email";

        logger.info(" Total number of emails: " + reader.count());

        int c = 0; int ix = 0;
        while (reader.hasNext()) {
            BasicDBObject obj = (BasicDBObject) reader.next();
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
                    Constants.Email + "_" + uid);
            emailInstance.
                    createEdgeTo(SourceHelper.createLiteral(network, Utils.parseEmailDate(date).getTime())).
                    setProperty(OntProperties.ONT_URI, occursDuringPropertyURI);

            for (Map.Entry<String, String> entry: entries) {
                URINode personInstance = SourceHelper.createInstance(network, Constants.CuenetNamespace +
                        Constants.Person + "_" + c);
                if (entry.getKey() != null) {
                    personInstance.
                            createEdgeTo(SourceHelper.createLiteral(network, entry.getKey())).
                            setProperty(OntProperties.ONT_URI, namePropertyURI);
                }
                if (entry.getValue() != null) {
                    personInstance.
                            createEdgeTo(SourceHelper.createLiteral(network, entry.getValue())).
                            setProperty(OntProperties.ONT_URI, emailPropertyURI);
                }

                personInstance.createEdgeTo(emailInstance).
                        setProperty(OntProperties.ONT_URI, participatesInPropertyURI);
                c += 1;
            }

            if (ix % 1000 == 0) logger.info("Added " + ix + " mails");
            ix += 1;
        }

        logger.info("EmailSource import complete");
    }
}

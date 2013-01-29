package esl.cuenet.ranking.sources;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.ranking.SourceInstantiator;
import esl.cuenet.source.accessors.AccessorConstants;
import esl.cuenet.source.accessors.Utils;
import esl.datastructures.TimeInterval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailSource extends MongoDB implements SourceInstantiator {

    public EmailSource() {
        super(AccessorConstants.DBNAME);
    }

    @Override
    public void populate(OntModel model) {

        DBReader reader = this.startReader("emails");
        BasicDBObject keys = new BasicDBObject();
        keys.put("_id", 0);

        reader.getAll(keys);

        String date, to, from, cc, uid;
        TimeInterval messageTimestamp = null;
        OntClass emailExchangeEventClass = model.getOntClass(Constants.CuenetNamespace + Constants.EmailExchangeEvent);
        OntClass personClass = model.getOntClass(Constants.CuenetNamespace + Constants.Person);
        Property occursDuringProperty = model.getProperty(Constants.CuenetNamespace + Constants.OccursDuring);
        Property participatesInProperty = model.getProperty(Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn);
        Property nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
        Property emailProperty = model.getProperty(Constants.CuenetNamespace + "email");

        int c = 0;
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
            messageTimestamp = TimeInterval.createFromMoment(Utils.parseEmailDate(date).getTime(), model);

            Individual emailInstance = emailExchangeEventClass.createIndividual(Constants.CuenetNamespace +
                    Constants.Email + "_" + uid);
            emailInstance.addProperty(occursDuringProperty, messageTimestamp);

            for (Map.Entry<String, String> entry: entries) {
                Individual person = personClass.createIndividual(Constants.CuenetNamespace +
                        Constants.Person + "_" + c);
                if (entry.getKey() != null) person.addLiteral(nameProperty, entry.getKey());
                if (entry.getValue() != null) person.addLiteral(emailProperty, entry.getValue());
                emailInstance.addProperty(participatesInProperty, person);
                c += 1;
            }
        }
    }
}

package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

import java.util.List;

public class FacebookRelationAccessor extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(FacebookRelationAccessor.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[1];
    private String id;
    private OntModel model = null;

    public FacebookRelationAccessor() {
        super("test");
    }

    public FacebookRelationAccessor(OntModel model) {
        this();
        this.model = model;
    }

    @Override
    public void setAttributeNames(Attribute[] attributes) throws AccesorInitializationException {
        this.attributes = attributes;
    }

    @Override
    public void start() {
        for (int i=0; i<setFlags.length; i++) setFlags[i] = false;
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {       /* name */
            setFlags[0] = true;
            this.id = "" + value;
        }
        else
            throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {       /* name */
            setFlags[0] = true;
            this.id = "" + getIDFromName(value);
            logger.info("Associated ID from name: " + id);
        }
        else
            throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                + FacebookUserAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        DBReader reader = this.startReader("fb_relationships");

        BasicDBList clauses = new BasicDBList();

        if ( !setFlags[0] )  {
            logger.info("Empty");
            return new ResultSetImpl("");
        }

        clauses.add(new BasicDBObject("id", id));
        clauses.add(new BasicDBObject("relation", id));
        BasicDBObject query = new BasicDBObject("$or", clauses);

        reader.query(query);
        BasicDBList result = new BasicDBList();
        String tmpID, tmpName;
        while (reader.hasNext()) {
            DBObject rel = reader.next();
            BasicDBObject normalizedRelation = new BasicDBObject(rel.toMap());

            /* normalize if the first entry is not the requested ID*/
            if ( ((String)normalizedRelation.get("id")).compareTo(id) != 0 ) {
                tmpID = (String) normalizedRelation.get("relation");
                tmpName = getNameFromFB(normalizedRelation.getString("relation"));
                normalizedRelation.put("relation", normalizedRelation.get("id"));
                normalizedRelation.put("relation_name", getNameFromFB((String)normalizedRelation.get("id")));
                normalizedRelation.put("id",  tmpID);
                normalizedRelation.put("name",  tmpName);
            }

            result.add(normalizedRelation);
        }

        if (result.size() > 3)
            return new ResultSetImpl(id + " has " + result.size() + " relationships");
        else
            return new ResultSetImpl(result.toString());
    }

    private String getIDFromName(String name) {
        DBReader reader = this.startReader("fb_users");

        BasicDBObject query = new BasicDBObject("name", name);
        reader.query(query);
        String result = null;
        while(reader.hasNext()) {
            DBObject o = reader.next();
            if (o.containsField("id")) result = (String) o.get("id");
        }

        return result;
    }

    private String getNameFromFB(String userID) {
        DBReader reader = this.startReader("fb_users");

        BasicDBObject query = new BasicDBObject("id", userID);
        reader.query(query);
        String result = null;
        while(reader.hasNext()) {
            DBObject o = reader.next();
            if (o.containsField("name")) result = (String) o.get("name");
        }

        return result;
    }

    public IResultSet executeQuery (long id) throws SourceQueryException {
        this.id = "" + id;
        setFlags[0] = true;
        return executeQuery();
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

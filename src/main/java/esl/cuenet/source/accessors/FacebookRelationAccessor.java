package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

public class FacebookRelationAccessor extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(FacebookRelationAccessor.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[1];
    private long id;

    public FacebookRelationAccessor() {
        super("test");
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
            this.id = value;
        }
        else
            throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                    + FacebookUserAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        throw new AccesorInitializationException("String value being initialized for wrong attribute "
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

        clauses.add(new BasicDBObject("id", "" + id));
        clauses.add(new BasicDBObject("relation", "" + id));
        BasicDBObject query = new BasicDBObject("$or", clauses);

        reader.query(query);
        BasicDBList result = new BasicDBList();
        while (reader.hasNext()) {
            DBObject o = reader.next();
            result.add(o);
        }

        return new ResultSetImpl(result.toString());
    }

    private class ResultSetImpl implements IResultSet {
        private String result;
        public ResultSetImpl (String result) {this.result = result;}
        @Override
        public String printResults() {
            return result;
        }
    }

    public IResultSet executeQuery (long id) throws SourceQueryException {
        this.id = id;
        setFlags[0] = true;
        return executeQuery();
    }

}

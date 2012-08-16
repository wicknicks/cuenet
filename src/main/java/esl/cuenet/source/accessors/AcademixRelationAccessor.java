package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.*;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.IAccessor;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AcademixRelationAccessor extends MongoDB implements IAccessor {

    private Logger logger = Logger.getLogger(FacebookRelationAccessor.class);
    private Attribute[] attributes = null;
    private boolean[] setFlags = new boolean[1];
    private String name;
    private OntModel model = null;
    private OntClass personClass = null;
    private DatatypeProperty nameProperty = null;

    public AcademixRelationAccessor() {
        super(AccessorConstants.DBNAME);
    }

    public AcademixRelationAccessor(OntModel model) {
        this();
        this.model = model;
        personClass = model.getOntClass(Constants.CuenetNamespace + "person");
        nameProperty = model.getDatatypeProperty(Constants.CuenetNamespace + "name");
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
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No time interval attributes in "
                + AcademixRelationAccessor.class.getName());

    }

    @Override
    public void associateLocation(Attribute attribute, Location timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("Incorrect Assignment: No location attributes in "
                + AcademixRelationAccessor.class.getName());

    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + AcademixRelationAccessor.class.getName());
    }

    @Override
    public void associateString(Attribute attribute, String value) throws AccesorInitializationException {
        if (attribute.compareTo(attributes[0])==0) {       /* name */
            setFlags[0] = true;
            this.name = value;
            logger.info("Associated name: " + name);
        }
        else
            throw new AccesorInitializationException("String value being initialized for wrong attribute "
                    + AcademixRelationAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                + AcademixRelationAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        MongoDB.DBReader reader = this.startReader("academix_relationships");
        ResultSetImpl resultSet = new ResultSetImpl("Academix Relation Accessor for : " + name);

        BasicDBList clauses = new BasicDBList();

        if ( !setFlags[0] )  {
            logger.info("Empty");
            return new ResultSetImpl("Academix Relation Accessor");
        }

        ObjectProperty knowsProperty = model.getObjectProperty(Constants.CuenetNamespace + "knows");

        clauses.add(new BasicDBObject("name", name));
        clauses.add(new BasicDBObject("relation_name", name));
        BasicDBObject query = new BasicDBObject("$or", clauses);

        reader.query(query);
        Individual inputPersonIndividual = personClass.createIndividual(personClass.getURI() + name.replaceAll(" ", "_"));
        inputPersonIndividual.addLiteral(nameProperty, name);
        logger.info("Input Name: " + name);

        String rname = null;
        while (reader.hasNext()) {
            BasicDBObject rel = (BasicDBObject) reader.next();
            BasicDBObject normalizedRelation;
            if ( !rel.getString("name").equals(name) ) {
                normalizedRelation = new BasicDBObject();
                normalizedRelation.put("name", rel.getString("relation_name"));
                normalizedRelation.put("id", rel.getInt("relation"));

                normalizedRelation.put("relation_name", rel.getString("name"));
                normalizedRelation.put("relation", rel.getInt("id"));
            } else {
                normalizedRelation = rel;
            }

            rname = normalizedRelation.getString("relation_name");
            Individual friendIndividual = personClass.createIndividual(personClass.getURI() +
                    rname.replaceAll(" ", "_"));
            friendIndividual.addLiteral(nameProperty, rname);
            inputPersonIndividual.addProperty(knowsProperty, friendIndividual);
            logger.info("Academix Relation Name: " + rname);

            List<Individual> result = new ArrayList<Individual>();
            result.add(inputPersonIndividual);
            result.add(friendIndividual);
            resultSet.addResult(result);
        }

        return resultSet;
    }

    public IResultSet executeQuery (String name) throws SourceQueryException {
        this.name = name;
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

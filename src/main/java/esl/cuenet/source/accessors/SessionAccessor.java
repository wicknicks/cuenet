package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.OntModel;
import com.mongodb.BasicDBList;
import esl.cuenet.model.Constants;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.Attribute;
import esl.cuenet.source.SourceQueryException;
import esl.datastructures.Location;
import esl.datastructures.TimeInterval;

import java.io.IOException;

public class SessionAccessor extends ConferenceSubEventAccessor {

    public SessionAccessor(OntModel model) {
        super(model);
    }

    @Override
    public void associateTimeInterval(Attribute attribute, TimeInterval timeInterval) throws AccesorInitializationException {
        throw new AccesorInitializationException("TimeInterval value being initialized for wrong attribute "
                + SessionAccessor.class.getName());
    }

    @Override
    public void associateLocation(Attribute attribute, Location location) throws AccesorInitializationException {
        throw new AccesorInitializationException("Location value being initialized for wrong attribute "
                + SessionAccessor.class.getName());
    }

    @Override
    public void associateLong(Attribute attribute, long value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Long value being initialized for wrong attribute "
                + SessionAccessor.class.getName());
    }

    @Override
    public void associateDouble(Attribute attribute, double value) throws AccesorInitializationException {
        throw new AccesorInitializationException("Double value being initialized for wrong attribute "
                + SessionAccessor.class.getName());
    }

    @Override
    public IResultSet executeQuery() throws SourceQueryException {
        BasicDBList results = query("sessions");
        try {
            return convert(results, model.getOntClass(Constants.CuenetNamespace + "session"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new SourceQueryException();
    }

}

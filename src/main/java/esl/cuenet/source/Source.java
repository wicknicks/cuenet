package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;
import esl.cuenet.query.IResultSet;

import java.util.List;

public class Source implements ISource {

    private String name;
    private IMapper mapper = null;
    private Attribute[] attributes = null;
    private TYPE type;
    private IO io;
    private IAccessor accessor;

    public Source(String name, IAccessor accessor, IMapper mapper) {
        this.name = name;
        this.mapper = mapper;
        this.accessor = accessor;
    }

    @Override
    public void setType(TYPE type) {
        this.type = type;
    }

    @Override
    public void setIO(IO io) {
        this.io = io;
    }

    @Override
    public TYPE getType() {
        return type;
    }

    @Override
    public IO getIO() {
        return io;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public IMapper getMapper() {
        return mapper;
    }

    @Override
    public IAccessor getAccessor() {
        return accessor;
    }

    @Override
    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
        try {
            accessor.setAttributeNames(this.attributes);
        } catch (AccesorInitializationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Attribute[] getAttributes() {
        return this.attributes;
    }

    @Override
    public IResultSet query(String[] pathExpressions, Literal[] literals) throws SourceQueryException {
        //TODO: Check adornments and query operators during query time.

        accessor.start();

        for (int i = 0; i < pathExpressions.length; i++) {
            Attribute attr = mapper.get(pathExpressions[i]);
            try {
                if (literals[i].getDatatypeURI().compareTo("http://www.w3.org/2001/XMLSchema#string") == 0)
                    accessor.associateString(attr, literals[i].getString());
                else if (literals[i].getDatatypeURI().compareTo("http://www.w3.org/2001/XMLSchema#double") == 0)
                    accessor.associateDouble(attr, literals[i].getDouble());
                else if (literals[i].getDatatypeURI().compareTo("http://www.w3.org/2001/XMLSchema#long") == 0)
                    accessor.associateLong(attr, literals[i].getLong());
            } catch (AccesorInitializationException e) {
                e.printStackTrace();
            }
        }

        return accessor.executeQuery();
    }
    
    @Override
    public IResultSet query(List<String> pathExpressions, List<Literal> literals) throws SourceQueryException {
        int sz = pathExpressions.size();
        String[] pathExps = new String[sz];
        Literal[] lits = new  Literal[sz];

        pathExps = pathExpressions.toArray(pathExps);
        lits = literals.toArray(lits);

        return query(pathExps, lits);
    }
}

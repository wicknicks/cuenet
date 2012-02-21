package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;
import esl.cuenet.query.IResultSet;

public class Source implements ISource {

    private String name;
    private IMapper mapper = new TreeMapper();
    private Attribute[] attributes = null;
    private TYPE type;
    private IO io;
    private IAccessor accessor;

    public Source(String name) {
        this.name = name;
    }

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
    }

    @Override
    public Attribute[] getAttributes() {
        return this.attributes;
    }

    @Override
    public IResultSet query(String[] pathExpression, Literal[] literals) throws SourceQueryException {
        return null;
    }
}

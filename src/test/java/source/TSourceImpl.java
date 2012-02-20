package source;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import esl.cuenet.source.Source;

public class TSourceImpl implements TISource {

    private Source.TYPE type;
    private Source.IO io;

    public TSourceImpl(Source.TYPE type, Source.IO io) {
        this.type = type;
        this.io = io;
    }

    @Override
    public Source.TYPE getType() {
        return type;
    }

    @Override
    public Source.IO getIO() {
        return io;
    }

    @Override
    public TIResultSet query(String[] pathExpression, Literal[] literals) throws TSourceQueryException {
        return null;
    }

}

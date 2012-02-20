package source;

import com.hp.hpl.jena.rdf.model.Statement;
import esl.cuenet.source.Source;

public class TSourceImpl implements TISource, TIMapper {

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
    public TIAttribute[] getAllAttributes() {
        return null;
    }

    @Override
    public void query(Statement[] statements) {

    }

    @Override
    public void map(String pattern, String attribute) {

    }

    @Override
    public void map(String pattern, int attribute) {

    }

    @Override
    public void map(String pattern, Statement[] statements) {

    }
}

package esl.cuenet.source;

public class SourceQueryException extends Exception {

    public SourceQueryException() {
        super("Query Execution Failed");
    }

    public SourceQueryException(String msg) {
        super(msg);
    }

}

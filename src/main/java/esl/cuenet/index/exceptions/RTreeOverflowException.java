package esl.cuenet.index.exceptions;

public class RTreeOverflowException extends RuntimeException {
    public RTreeOverflowException() {
        super("RTree overflow exception");
    }

    public RTreeOverflowException(String msg) {
        super(msg);
    }
}

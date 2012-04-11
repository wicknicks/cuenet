package esl.cuenet.algorithms.firstk;


/**
 *  Interface for discoverable node queues.
 */
public interface NodeQueue<T> {

    public T pop();

    public long count();

}

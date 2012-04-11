package esl.cuenet.algorithms.firstk;

import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;

public interface Verification<T> {

    public double verify(Entity entity, Dataset<T> dataset);

}

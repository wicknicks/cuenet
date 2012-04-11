package esl.cuenet.algorithms.firstk;

import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;
import esl.datastructures.graph.relationgraph.RelationGraph;

public interface Preprocessing<T> {

    /**
     * Looks up the first item in the dataset, and returns a relationgraph for it.
     * Each successive invocation will return a new RelationGraph for the next item.
     * A null implies there are no more items in the dataset.
     * @param dataset input dataset
     * @return relationgraph for dataset.current_item
     */
    public RelationGraph process(Dataset<T> dataset) throws CorruptDatasetException;

    /**
     * Associate an event with this dataset
     * @param event event
     */
    public void associate(Event event);

    /**
     * Associate an entity with this dataset
     * @param entity entity
     */
    public void associate(Entity entity);
}

package esl.cuenet.algorithms.firstk;

import com.hp.hpl.jena.ontology.Individual;
import esl.cuenet.algorithms.firstk.exceptions.CorruptDatasetException;
import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;

public interface Preprocessing<T> {

    /**
     * Looks up the first item in the dataset, and returns a relationgraph for it.
     * Each successive invocation will return a new EventGraph for the next item.
     * A null implies there are no more items in the dataset.
     * @param dataset input dataset
     * @return relationgraph for dataset.current_item
     */
    public EventGraph process(Dataset<T> dataset) throws CorruptDatasetException;

    /**
     * Associate an individual of type t with this dataset
     * @param individual event
     * @param t type
     */
    public void associate(Individual individual, EventGraph.NodeType t);

}

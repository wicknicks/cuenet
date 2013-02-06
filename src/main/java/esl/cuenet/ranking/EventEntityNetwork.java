package esl.cuenet.ranking;

import java.util.Iterator;

public interface EventEntityNetwork extends Versioned {

    public static final String EVENT_INDEX = "event_index";

    public static final String EVENT = "event";

    URINode createNode();

    URINode getNodeById(long id);

    EventEntityNetwork subnet(Iterator<URINode> nodeIterator);

    NetworkTraverser traverser();

    SpatioTemporalIndex stIndex(String indexName);

    TextIndex textIndex(String indexName);

    OntoInstanceFactory ontoInstanceFactory();

    Iterator<TypedEdge> getEdgesIterator();

    Iterator<URINode> getEventsIterator();

    void rank(Ranker ranker);

    void startBulkLoad();

    void finishBulkLoad();

    void flush();

}

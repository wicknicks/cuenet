package esl.cuenet.ranker;

import com.hp.hpl.jena.ontology.OntClass;

public interface IRanker {

    public OntClass[] dataInputs();

    public OntClass[] cxInputs();

    public void addDataClass(OntClass ontClass, String propertyChain);

    public void addContextClass(OntClass ontClass, String propertyChain);

    public void setPriorRanks(RankerResult[] priorRanks);

    public void rankOn(OntClass ontClass);

    public RankerResult[] rank();

}

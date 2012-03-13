package esl.cuenet.ranker.rankers;

import com.hp.hpl.jena.ontology.OntClass;
import esl.cuenet.ranker.IRanker;
import esl.cuenet.ranker.RankerResult;
import org.apache.log4j.Logger;

public class LocationRanker implements IRanker {

    private Logger logger = Logger.getLogger(LocationRanker.class);

    @Override
    public OntClass[] dataInputs() {
        return new OntClass[0];
    }

    @Override
    public OntClass[] cxInputs() {
        return new OntClass[0];
    }

    @Override
    public void addDataClass(OntClass ontClass, String propertyChain) {

    }

    @Override
    public void addContextClass(OntClass ontClass, String propertyChain) {

    }

    @Override
    public void setPriorRanks(RankerResult[] priorRanks) {
        //todo: implement method

    }

    @Override
    public void rankOn(OntClass ontClass) {

    }

    @Override
    public RankerResult[] rank() {

        return null;
    }

}

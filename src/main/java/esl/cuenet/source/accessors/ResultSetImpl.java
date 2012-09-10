package esl.cuenet.source.accessors;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.query.IResultIterator;
import esl.cuenet.query.IResultSet;
import esl.cuenet.query.ResultIterator;

import java.util.List;

public class ResultSetImpl implements IResultSet {
    private String result;
    private ResultIterator resultIterator;

    public ResultSetImpl (String result, OntModel model) {
        this.result = result;
        this.resultIterator = new ResultIterator(model);
    }

    public void addResult(List<Individual> individuals) {
        this.resultIterator.add(individuals);
    }

    @Override
    public String printResults() {
        return result;
    }

    @Override
    public IResultIterator iterator() {
        return resultIterator;
    }
}
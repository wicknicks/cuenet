package esl.cuenet.query;

import com.hp.hpl.jena.ontology.Individual;

import java.util.List;
import java.util.Map;

public interface IResultIterator {

    public boolean hasNext();

    public Map<String, List<Individual>> next(List<String> projectVarTypes);

}

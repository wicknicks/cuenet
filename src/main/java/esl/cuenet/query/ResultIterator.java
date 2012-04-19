package esl.cuenet.query;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultIterator implements IResultIterator {

    private OntModel model = null;

    public ResultIterator(OntModel model) {
        this.model = model;
    }

    private int pointer = 0;
    private List<List<Individual>> results = new ArrayList<List<Individual>>();

    public void add(List<Individual> result) {
        results.add(result);
    }

    @Override
    public boolean hasNext() {
        return (pointer < results.size());
    }

    @Override
    public Map<String, List<Individual>> next(List<String> projectVarTypes) {

        List<Individual> allResults = results.get(pointer);

        Map<String, List<Individual>> resultMap = new HashMap<String, List<Individual>>();
        for (String projectVarType: projectVarTypes) resultMap.put(projectVarType, new ArrayList<Individual>());


        for (Individual result: allResults) {
            OntClass ontClass = result.getOntClass();
            for (String projectVarURI: projectVarTypes) {
                if (ontClass.getURI().equals(projectVarURI) || ontClass.hasSuperClass(model.getOntClass(projectVarURI))) {
                    resultMap.get(projectVarURI).add(result);
                }
            }
        }

        pointer++;
        return resultMap;
    }
}
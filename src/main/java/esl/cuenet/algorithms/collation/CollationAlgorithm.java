package esl.cuenet.algorithms.collation;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import esl.cuenet.algorithms.BaseAlgorithm;
import esl.cuenet.algorithms.ICostEvaluator;
import esl.cuenet.algorithms.TripleStore;
import esl.cuenet.algorithms.costs.RandomCostEvaluator;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;
import esl.cuenet.query.IResultSet;
import esl.cuenet.source.AccesorInitializationException;
import esl.cuenet.source.ISource;
import esl.cuenet.source.SourceQueryException;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollationAlgorithm extends BaseAlgorithm {

    private TripleStore tripleStore = null;
    private ICostEvaluator costEvaluator = new RandomCostEvaluator();
    private boolean[] sourceFlags = new boolean[sourceMapper.count()];
    private boolean[] queriedSources = new boolean[sourceMapper.count()];
    private Logger logger = Logger.getLogger(CollationAlgorithm.class);
    ISource[] sources = null;

    public CollationAlgorithm() throws IOException, ParseException {
        super();
        sources = sourceMapper.getSources();
    }

    public OntModel getModel() {
        return model;
    }

    public void addSeeds(Individual[] individuals) {
        tripleStore = new TripleStore(individuals);
    }

    public void start() {

        for (int i = 0; i < queriedSources.length; i++) queriedSources[i] = false;

        while (true) {
            if ( !populator() ) break;
            verifier();
        }
    }

    private boolean populator() {

        for (int i = 0; i < sourceFlags.length; i++) sourceFlags[i] = false;

        Individual[] data = tripleStore.getIndividualsFromDataStore();
        for (Individual _data : data) {
            logger.info("Id: " + _data.getId() + ", Uri: " + _data.getOntClass().getURI());
            OntClass ont = _data.getOntClass();
            String nsh = sourceMapper.getNamespaceShorthand(ont.getNameSpace());
            if (nsh.compareTo("this") == 0) {
                String classname = ont.getURI().substring(ont.getURI().indexOf('#') + 1);
                mark(classname);
            }
        }

        int queryableSourceCount=0;
        for (int i=0;i < sourceFlags.length; i++) if (sourceFlags[i] && !queriedSources[i]) queryableSourceCount++;

        ISource[] queryableSources = new ISource[queryableSourceCount];
        for (int j=0, i=0;i < sourceFlags.length; i++)
            if (sourceFlags[i] && !queriedSources[i])
                queryableSources[j++] = sources[i];


        logger.info("Choices: " + (queryableSourceCount) + "/" + (sourceFlags.length));
        if (queryableSourceCount == 0) return false;

        // construct query
        ISource minCostSource = findMinCostSource (queryableSources);
        List<Literal> literals = new ArrayList<Literal>();
        List<String> pathExpressions = new ArrayList<String>();

        for (Individual _data : data) {
            OntClass ont = _data.getOntClass();
            String nsh = sourceMapper.getNamespaceShorthand(ont.getNameSpace());
            String classname = null;
            if (nsh.compareTo("this") == 0) 
                classname = ont.getURI().substring(ont.getURI().indexOf('#') + 1);
            else classname = ont.getURI();
            
            StmtIterator iter = _data.listProperties();
            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                if (!stmt.getObject().isLiteral()) continue;
                
                String prop = null;
                nsh = sourceMapper.getNamespaceShorthand(stmt.getPredicate().getNameSpace());
                if (nsh.compareTo("this")==0) 
                    prop = stmt.getPredicate().getURI().substring(stmt.getPredicate().getURI().indexOf('#')+1);
                else prop = stmt.getPredicate().getURI();

                if (minCostSource.getMapper().containsPattern(classname + "." + prop)) {
                    pathExpressions.add(classname + "." + prop);
                    literals.add(stmt.getObject().asLiteral());
                }
            }
        }

        try {
            IResultSet results = minCostSource.query(pathExpressions, literals);
            logger.info(results.printResults());
        } catch (SourceQueryException e) {
            e.printStackTrace();
        } catch (AccesorInitializationException e) {
            e.printStackTrace();
        }

        markAsQueried(minCostSource);
        return true;
    }

    private void markAsQueried(ISource minCostSource) {
        for (int i=0; i<sources.length; i++) {
            if (sources[i] == minCostSource) queriedSources[i] = true;
        }
    }

    private ISource findMinCostSource(ISource[] queryableSources) {
        double[] costs = costEvaluator.evaluate(queryableSources);
        double min = Double.MAX_VALUE;

        int pos = 0;
        for (int i=0; i<costs.length;i++) {
            if (costs[i] <= min ) {
                min = costs[i];
                pos = i;
            }
        }

        for (int i=0; i<queryableSources.length; i++) logger.info("QS: " + queryableSources[i].getName() + ", cost: " + costs[i]);
        return queryableSources[pos];
    }


    private void mark(String classname) {
        int ix = 0;
        for (ISource source : sources) {
            logger.info("Classname: " + classname + ", Name: " + source.getName() + ", isPresent: "
                    + source.getMapper().containsPattern(classname));
            sourceFlags[ix] |= source.getMapper().containsPattern(classname);
            ix++;
        }
    }

    private void verifier() {

    }

}


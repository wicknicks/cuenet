package esl.cuenet.ranking.network;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import esl.cuenet.ranking.*;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class NeoOntoInstanceImporter implements OntoInstanceFactory {

    private final EventEntityNetwork network;
    private final SourceInstantiator[] sourceInstantiators;
    private Logger logger = Logger.getLogger(NeoOntoInstanceImporter.class);

    public NeoOntoInstanceImporter(EventEntityNetwork network, SourceInstantiator[] sourceInstantiators) {
        this.network = network;
        this.sourceInstantiators = sourceInstantiators;
    }

    @Override
    public URINode createNode(Individual ontologyInstance) {
        return null;
    }

    public void populate(EntityBase entityBase) {
        OntModel model = null;
        model = ModelFactory.createOntologyModel();

        try {
            model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                    "http://www.semanticweb.org/arjun/cuenet-main.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int i = 1;
        for (SourceInstantiator srcInst: sourceInstantiators) {
            logger.info("Loading model " + (i++) + " of " + sourceInstantiators.length);
            srcInst.populate(network, entityBase);
        }
    }
}

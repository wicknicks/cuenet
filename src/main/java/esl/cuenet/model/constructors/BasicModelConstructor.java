package esl.cuenet.model.constructors;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BasicModelConstructor {

    public BasicModelConstructor() {
    }

    public void constructOntModel() {
        OntModel model = ModelFactory.createOntologyModel(ProfileRegistry.OWL_LITE_LANG);

        model.
    }

}

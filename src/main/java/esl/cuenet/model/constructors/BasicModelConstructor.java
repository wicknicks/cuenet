package esl.cuenet.model.constructors;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class BasicModelConstructor {

    public BasicModelConstructor() {
    }
    
    public void constructOntModel() throws FileNotFoundException {

        OntModel model = ModelFactory.createOntologyModel();

        model.read(new FileReader("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.owl"),
                "http://www.semanticweb.org/arjun/cuenet-main.owl");

        String queryString = "SELECT ?x ?y ?z" +
                " WHERE { <http://www.semanticweb.org/arjun/cuenet-main.owl#arjun> ?y ?z . } ";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecutor = QueryExecutionFactory.create(query, model);

        ResultSet results = queryExecutor.execSelect();
        ResultSetFormatter.out(System.out, results, query);

        queryExecutor.close();

    }

}

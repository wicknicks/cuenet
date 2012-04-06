package esl.cuenet.source;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.query.QueryOperator;

public interface IMapper {

    /*
         Maps a path to a node in the tree mapper to an attribute in the source.
         Also associates the adornment, operatory type to this node.
     */
    void map (String pathExpression, Adornment adornment, QueryOperator operator, Attribute attribute);

    /*
         Overloaded methods to map patterns
     */

    void map (String pathExpression, Adornment adornment);

    void map (String pathExpression, QueryOperator operator);

    void map (String pathExpression, Attribute attribute);

    /*
        Returns true if the input pattern maps to some source attribute in the tree mapper.
        False, otherwise.
     */

    boolean containsPattern(String pathExpression);

    /*
         Get attribute for a given pathExpression
     */

    Attribute getAttribute(String pathExpression);

    /*
    Get adornment for a given pathExpression
     */
    Adornment getAdornment(String pathExpression);

    /*
         Set/Get ontology models
     */

    void setOntologyModel(OntModel model);

    OntModel getOntologyModel();
}

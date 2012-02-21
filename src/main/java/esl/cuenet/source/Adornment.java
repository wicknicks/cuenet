package esl.cuenet.source;

import com.hp.hpl.jena.rdf.model.Literal;

public class Adornment {

    private Literal[] literals;
    private AdornmentType type;

    public enum AdornmentType {

        /* The attribute may or may not be specified in the query */
        Free,

        /* The attribute cannot be specified in the query */
        Unspecified,

        /* The attribute must be specified in the query */
        Bound,

        /* The attribute must be specified, and chosen from a set of constants */
        Constant,

        /* The attribute may or may not be specified, and chosen from a set of constants */
        Optional

    }

    public Adornment(AdornmentType type) {
        this.type = type;
    }

    public void setOptions(Literal[] literals) {
        this.literals = literals;
    }

    public Literal[] getOptions() {
        return literals;
    }

}

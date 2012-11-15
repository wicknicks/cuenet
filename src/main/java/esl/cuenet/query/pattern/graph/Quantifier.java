package esl.cuenet.query.pattern.graph;

public enum Quantifier {

    NONE(""),

    STAR("*"),   /* kleene star */

    PLUS("+"),   /*kleene plus */

    QUESTION("?"),   /* exists or not */

    STAR_S("*_S"),   /* space suffix to kleene star */

    STAR_T("*_T"),   /* time suffix to kleene star */

    PLUS_S("+_S"),   /* plus suffix to kleene star */

    PLUS_T("+_T");   /* plus suffix to kleene star */

    private String quant = "";

    private Quantifier(String quant) {
        this.quant = quant;
    }

    @Override
    public String toString() {
        return quant;
    }

}

package esl.cuenet.query.pattern.graph;

public enum Quantifier {

    NONE,

    STAR,   /* kleene star */

    PLUS,   /*kleene plus */

    QUESTION,   /* exists or not */

    STAR_S,   /* space suffix to kleene star */

    STAR_T,   /* time suffix to kleene star */

    PLUS_S,   /* plus suffix to kleene star */

    PLUS_T   /* plus suffix to kleene star */

}

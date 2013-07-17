package esl.cuenet.algorithms.firstk.personal.accessor;

public class SourceFactory {

    public static Source[] getSources() {

        return new Source[]{
                new Email(), new Facebook()
        };

    }

}

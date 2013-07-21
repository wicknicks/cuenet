package esl.cuenet.algorithms.firstk.personal.accessor;

public class SourceFactory {

    private Source[] sources = null;


    private static SourceFactory instance = new SourceFactory();
    public static SourceFactory getFactory() {
        return instance;
    }

    protected SourceFactory() {
        Source email = Email.getInstance();
        Candidates.getInstance().logistics(false);

        Source facebook = Facebook.getInstance();
        Candidates.getInstance().logistics(false);

        Source calendar = Calendar.getInstance();
        Candidates.getInstance().logistics(false);

        sources = new Source[] {
                email, facebook, calendar
        };
    }


    public Source[] getSources() {

        return sources;

    }

}

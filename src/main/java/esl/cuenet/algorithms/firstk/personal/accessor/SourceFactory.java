package esl.cuenet.algorithms.firstk.personal.accessor;

import java.io.File;
import java.io.IOException;

public class SourceFactory {

    private Source[] sources = null;


    private static SourceFactory instance = new SourceFactory();
    public static SourceFactory getFactory() {
        return instance;
    }

    protected SourceFactory() {
        Source facebook = Facebook.getInstance();
        Candidates.getInstance().logistics(false);

        Source email = Email.getInstance();
        Candidates.getInstance().logistics(false);

        Source calendar = Calendar.getInstance();
        Candidates.getInstance().logistics(false);

        sources = new Source[] {
                email, facebook, calendar
        };

        File instanceFile = new File("/data/ranker/real/instances." + PConstants.DBNAME + ".txt");
        try {
            facebook.writeInstances(instanceFile);
            calendar.writeInstances(instanceFile);
            email.writeInstances(instanceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Source[] getSources() {

        return sources;

    }

}

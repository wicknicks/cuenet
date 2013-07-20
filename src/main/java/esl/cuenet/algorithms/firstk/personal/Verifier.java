package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Verifier {

    private String[] annotations;
    private Logger logger = Logger.getLogger(Verifier.class);
    private int numVerificationCalls = 0;

    protected Verifier() {
        try {
            annotations = getAnnotations(PConstants.IMAGE + ".annotations");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Verifier verifier = new Verifier();

    public static Verifier getInstance() {
        return verifier;
    }

    public String[] getAnnotations(String path) throws IOException {
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String tmp = null;
        ArrayList<String> annotations = new ArrayList<String>();
        int ix=0, eix = 0;
        while (true) {
            tmp = reader.readLine();
            if (tmp == null) break;
            if (!tmp.contains("\"")) continue;
            ix = tmp.indexOf('"');
            eix = tmp.indexOf('"', ix+1);
            String annotation = tmp.substring(ix+1, eix);

            annotations.add(annotation);
        }

        String a[] = new String[annotations.size()];
        annotations.toArray(a);
        logger.info("Annotations: " + Arrays.toString(a));
        return a;
    }

    public boolean verify(Candidates.CandidateReference reference) {
        numVerificationCalls++;
        Candidates candidateSet = Candidates.getInstance();
        for (String _ann: annotations) {
            List<Candidates.CandidateReference> result = candidateSet.search(Candidates.NAME_KEY, _ann.toLowerCase());
            if (result.size() != 1) throw new RuntimeException();
            if (result.get(0).equals(reference)) return true;
        }
        return false;
    }

    public int annotationCount() {
        return annotations.length;
    }

    public int numVerificationCalls() {
        return numVerificationCalls;
    }
}

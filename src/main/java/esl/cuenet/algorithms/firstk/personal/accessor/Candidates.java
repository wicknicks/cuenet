package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Candidates {

    private Logger logger = Logger.getLogger(Candidates.class);

    private static Candidates instance = new Candidates();

    public static CandidateReference UNKNOWN;

    private static int IDMAX = 0;

    private List<Candidate> candidates = Lists.newArrayList();
    private HashMap<CandidateReference, Candidate> cIndex = Maps.newHashMap();

    /** KEYS **/

    public static String EMAIL_KEY = "email_key";
    public static String NAME_KEY = "name_key";
    public static String LOCATION_KEY = "facebook_location_key";
    public static String FB_ID_KEY = "facebook_id_key";

    protected Candidates() {
        UNKNOWN = new CandidateReference(-1);
    }

    public static Candidates getInstance() {
        return instance;
    }

    public Iterator<CandidateReference> candidateIterator() {
        List<CandidateReference> refs = Lists.newArrayList(cIndex.keySet());
        return refs.iterator();
    }

    public Candidate get(CandidateReference ref) {
        return cIndex.get(ref);
    }

    public void add(CandidateReference ref, String key, String value) {
        if (value == null) throw new NullPointerException("value should not be NULL");
        for (Candidate candidate: candidates) {
            if (candidate.reference.equals(ref)) {
                if ( !candidate.map.containsKey(key) )
                    candidate.map.put(key, value);
                else if ( !candidate.map.get(key).contains(value) )
                    candidate.map.put(key, value);
            }
        }
    }

    public CandidateReference createCandidate(String key, String value) {
        Candidate candidate = new Candidate();
        candidate.map.put(key, value);

        candidates.add(candidate);
        cIndex.put(candidate.reference, candidate);

        return candidate.reference;
    }

    public CandidateReference search(String key, String value) {
        List<CandidateReference> references = Lists.newArrayList();
        for (Candidate candidate: candidates) {
            if ( candidate.map.containsKey(key) )
                if (candidate.map.get(key).contains(value))
                    references.add(candidate.reference);
        }

        if (references.size() == 0) {
            return UNKNOWN;
        }
        else if (references.size() == 1) {
            return references.get(0);
        }
        else {
            logger.info("Multiple candidates exist with same key, value! " + key + " " + value);
            for (CandidateReference r: references) {
                Candidate c = cIndex.get(r);
                logger.info(c);
            }
            logger.info("-------- Merging");

            Candidate c = merge(references);
            logger.info(c);
            logger.info("-------- Returned Candidate");
            return c.reference;
        }

    }

    private Candidate merge(List<CandidateReference> references) {
        Candidate main = cIndex.get(references.get(0));
        for (int i=1; i<references.size();i++) {
            Candidate redun = cIndex.get(references.get(i));
            main.map.putAll(redun.map);
            candidates.remove(redun);
            cIndex.remove(redun.reference);
        }
        return main;
    }

    public void logistics(boolean printAll) {
        logger.info("Candidates = " + candidates.size());
        int count = 0;
        for (Candidate c: candidates) count += c.map.values().size();
        logger.info("# of Values = " + count);

        if (printAll) {
            for (Candidate c: candidates) {
                logger.info(c);
            }
        }
    }

    public class Candidate {
        CandidateReference reference;
        Multimap<String, String> map;

        public Candidate() {
            reference = new CandidateReference(IDMAX);
            map = HashMultimap.create(); //ArrayListMultimap.create()
            IDMAX++;
        }

        @Override
        public String toString() {
            String s = "";
            for (String _key: map.keySet()) {
                s += "{" + _key + ":" + map.get(_key).toString();
                s += "}, ";
            }
            return s;
        }
    }

    public static class CandidateReference {

        public CandidateReference(int id) {
            this.id = "" + id;
        }

        String id;

        @Override
        public boolean equals (Object ref2) {
            if (ref2 instanceof CandidateReference) {
                CandidateReference r = (CandidateReference)ref2;
                return this.id.equals(r.id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

    }



}

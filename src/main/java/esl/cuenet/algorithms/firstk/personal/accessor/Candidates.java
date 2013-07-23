package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.*;
import org.apache.log4j.Logger;

import java.util.*;

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


    public CandidateReference createEntity(List<String> keys, List<String> values) {
        int size = keys.size();
        if (size > 0 && size != values.size()) throw new RuntimeException("Size");

        List<CandidateReference> refs = Lists.newArrayList();

        for (int i=0; i<size; i++) {
            String key = keys.get(i);
            String value = values.get(i);

            if (key.equals(LOCATION_KEY)) continue;

            List<CandidateReference> tempRefs = search(key, value);

            for(CandidateReference s: tempRefs) {
                if (!refs.contains(s) && mergable(keys, values, s)) {
                    refs.add(s);
                    //mergeInto(s, keys, values);
                }
            }
        }

        if (refs.size() == 0) {
            CandidateReference c = createCandidate(keys.get(0), values.get(0));
            mergeInto(c, keys, values);
            return c;
        } else  if (refs.size() == 1) {
            mergeInto(refs.get(0), keys, values);
            return refs.get(0);
        } else {
            Candidate finalCandidate = merge(refs);
            mergeInto(finalCandidate.reference, keys, values);
            return finalCandidate.reference;
        }
    }

    private boolean mergable(List<String> keys, List<String> values, CandidateReference reference) {
        Candidate previous = cIndex.get(reference);
        int size = keys.size();
        for (int i=0; i<size; i++) {
            String key = keys.get(i);
            if (key.equals(FB_ID_KEY) && previous.map.containsKey(FB_ID_KEY)) {
                String value = values.get(i);
                String id = previous.map.get(FB_ID_KEY).iterator().next();
                return value.equals(id);
            }
            if (key.equals(EMAIL_KEY) && previous.map.containsKey(FB_ID_KEY)) {
                String value = values.get(i);
                if (previous.map.get(EMAIL_KEY).contains(value)) return true;
            }
        }
        return true;
    }

    public void mergeInto(CandidateReference reference, List<String> keys, List<String> values) {
        int size = keys.size();
        for (int i=0; i<size; i++) {
            add(reference, keys.get(i), values.get(i));
        }
    }

    public void add(CandidateReference ref, String key, String value) {
        if (value == null) throw new NullPointerException("value should not be NULL");

        Candidate candidate = cIndex.get(ref);
        if ( !candidate.map.containsKey(key) )
            candidate.map.put(key, value);
        else if ( !candidate.map.get(key).contains(value) )
            candidate.map.put(key, value);
    }

    private CandidateReference createCandidate(String key, String value) {
        Candidate candidate = new Candidate();
        candidate.map.put(key, value);

        candidates.add(candidate);
        cIndex.put(candidate.reference, candidate);

        return candidate.reference;
    }

    public CandidateReference searchLimitOne(String key, String value) {
        List<CandidateReference> references = search(key, value);
        if (references.size() > 1) throw new RuntimeException("multiple search results found");
        if (references.size() == 1) return references.get(0);
        return null;
    }

    public List<CandidateReference> search(String key, String value) {
        List<CandidateReference> references = Lists.newArrayList();
        for (Candidate candidate: candidates) {
            if ( candidate.map.containsKey(key) )
                if (candidate.map.get(key).contains(value))
                    references.add(candidate.reference);
        }

        return references;
        /*
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
        */

    }

    private Candidate merge(List<CandidateReference> references) {
        Collections.sort(references, new Comparator<CandidateReference>() {
            @Override
            public int compare(CandidateReference o1, CandidateReference o2) {
                int o2id = Integer.parseInt(o2.id);
                int o1id = Integer.parseInt(o1.id);
                return o1id - o2id;
            }
        });

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
                logger.info(c.reference.id + " " + c);
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

        public String toStringKey(String key) {
            return map.get(key).toString()
                    .replace('\n', '\\')
                    .replace('\r', '\\');
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
        public String toString() {
            return this.id;
        }

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

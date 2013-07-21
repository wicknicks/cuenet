package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.generative.structs.ContextNetwork;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;

public class EventContextNetwork extends ContextNetwork {

    private HashMap<ECNRef, Event> eventMap = Maps.newHashMap();
    private HashMap<ECNRef, Person> personMap = Maps.newHashMap();

    private BiMap<ECNRef, Candidates.CandidateReference> personCandidateIndex = HashBiMap.create();
    private Candidates candidateSet = Candidates.getInstance();

    private HashMap<String, Integer> eventURIHashTable = new HashMap<String, Integer>(10);

    private Logger logger = Logger.getLogger(EventContextNetwork.class);

    public ECNRef createEvent(String ontologyURI, long startTime, long endTime) {
        return createEvent(ontologyURI, startTime, endTime, null);
    }

    public ECNRef createEvent(String ontologyURI, long startTime, long endTime, String location) {
        if ( !eventURIHashTable.containsKey(ontologyURI) ) {
            int count = eventURIHashTable.size();
            eventURIHashTable.put(ontologyURI, count);
        }

        int uriID = eventURIHashTable.get(ontologyURI);
        ECNRef ref = ECNRef.newRef();

        Event event = new Event(uriID, ref);
        event.setInterval(startTime, endTime);
        if (location != null) event.setLocation(location);
        eventMap.put(ref, event);

        return ref;
    }

    public void initializeSubeventTree (ECNRef reference) {
        if ( !eventMap.containsKey(reference) ) throw new RuntimeException(reference + " not found");
        if (this.eventTrees.size() > 0) throw new RuntimeException("Tree has already been created");
        addAtomic(eventMap.get(reference));
    }


    public ECNRef createPerson (Candidates.CandidateReference reference) {
        ECNRef ref;
        if (personCandidateIndex.containsValue(reference))
            ref = personCandidateIndex.inverse().get(reference);
        else ref = ECNRef.newRef();

        personCandidateIndex.put(ref, reference);

        int id = 0;
        Person person = new Person(Ontology.PERSON, ref);
        personMap.put(ref, person);
        return ref;
    }

    public Candidates.CandidateReference getCandidateReference(ECNRef ref) {
        return personCandidateIndex.get(ref);
    }

    public ECNRef createPerson (String candidateKey, String candidateValue) {
        List<Candidates.CandidateReference> reference = candidateSet.search(candidateKey, candidateValue);

        if (reference.size() != 1)
            throw new RuntimeException("unknown person " + candidateKey + " " + candidateValue);

        return createPerson(reference.get(0));
    }

    public void createSubeventEdge(ECNRef _super, ECNRef _sub) {
        if ( !eventMap.containsKey(_super) || !eventMap.containsKey(_sub)) {
            logger.info("Unkown keys = " + _super + " " + _sub);
            return;
        }
        addSubeventEdge(eventTrees.get(0).root, eventMap.get(_super), eventMap.get(_sub));
    }

    public void createPartiticipationEdge(ECNRef event, ECNRef person) {
        if ( !eventMap.containsKey(event) || !personMap.containsKey(person)) {
            logger.info("Unkown keys = " + event + " " + person);
            return;
        }
        eventMap.get(event).addPariticipant(person);
    }

    public void visit(Visitor visitor) {
        for (Event event: eventMap.values()) {
            visitor.visit(event);
            event.visit(visitor);
        }
    }

    public ECNRef getPhotoCaptureEventRef() {
        List<ECNRef> participants = Lists.newArrayList();
        for (Event event: eventMap.values()) {
            if (eventURIHashTable.get("photo-capture") == event.getId())
                return event.reference;
        }
        return null;
    }

    public List<ECNRef> getVotableEntities() {
        List<ECNRef> participants = Lists.newArrayList();
        for (Event event: eventMap.values()) {
            if (eventURIHashTable.get("photo-capture") == event.getId())
                participants.addAll(event.participants);
        }
        return participants;
    }

    public static class ECNRef {
        int id;

        private static int count = 0;

        private ECNRef() {}

        public static ECNRef newRef() {
            ECNRef ref = new ECNRef();
            ref.id = count++;
            return ref;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ECNRef ecnRef = (ECNRef) o;

            if (id != ecnRef.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }

        public String toString() {
            return "" + id;
        }
    }


    public class Event extends Instance {

        ECNRef reference;
        List<ECNRef> participants = Lists.newArrayList();

        public Event(int eventId, ECNRef ref) {
            super(eventId, ref.id);
            this.reference = ref;
        }

        public void addPariticipant(ECNRef personReference) {
            if ( this.participants.contains(personReference) )
                return;
            this.participants.add(personReference);
        }

        public void visit(Visitor visitor) {
            for (ECNRef p: this.participants) {
                visitor.visit(personMap.get(p));
            }
        }

        public int getId() {
            return this.id.eventId;
        }
    }

    public class Person extends Entity {

        ECNRef reference;

        public Person(String type, ECNRef ref) {
            super(type, "" + ref.id);
            this.reference = ref;
        }

    }

    public interface Visitor {
        public void visit(Event event);
        public void visit(Person person);
    }

}

package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

import java.util.List;

public class Facebook implements Source {

    private Logger logger = Logger.getLogger(Facebook.class);
    private Candidates candidateList = Candidates.getInstance();

    private Multimap<Candidates.CandidateReference, Candidates.CandidateReference> knowsGraph = HashMultimap.create();


    protected Facebook() {
        FBLoader loader = new FBLoader();
        loader.load();
    }

    private static Facebook instance = new Facebook();
    public static Facebook getInstance() {
        return instance;
    }

    @Override
    public List<EventContextNetwork> eventsContaining(Candidates.CandidateReference person, Time interval, Location location) {
        return null;
    }

    @Override
    public List<EventContextNetwork> participants(EventContextNetwork.Event event) {
        return null;
    }

    @Override
    public List<EventContextNetwork> subevents(EventContextNetwork.Event event) {
        return null;
    }

    @Override
    public List<Candidates.CandidateReference> knows(Candidates.CandidateReference person) {
        if ( !knowsGraph.containsKey(person) ) return null;
        return Lists.newArrayList(knowsGraph.get(person));
    }

    @Override
    public List<EventContextNetwork> knowsAtTime(Candidates.CandidateReference person, Time time) {
        return null;
    }


    public class FBLoader extends MongoDB {

        public FBLoader() {
            super(PConstants.DBNAME);
        }

        public void load() {
            //load users
            MongoDB.DBReader reader = startReader("fb_users");
            reader.getAll(new BasicDBObject());

            String name= null, location=null, id=null, email=null;
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                name = obj.getString("name").toLowerCase();
                if (obj.containsField("email"))
                    email = obj.getString("email").toLowerCase();
                if (obj.containsField("location")) {
                    BasicDBObject l = (BasicDBObject) obj.get("location");
                    if (l.containsField("name"))
                        location = l.getString("name");
                }
                id = obj.getString("id");

                //logger.info("user = " + name + " " + id + " " + location );
                if (id.equals(""))
                    logger.info("id");


                List<String> keys = Lists.newArrayList();
                List<String> values = Lists.newArrayList();

                keys.add(Candidates.FB_ID_KEY); values.add(id);
                if (name != null) {
                    keys.add(Candidates.NAME_KEY); values.add(name);
                }
                if (location != null) {
                    keys.add(Candidates.LOCATION_KEY); values.add(location);
                }
                if (email != null) {
                    keys.add(Candidates.EMAIL_KEY); values.add(email);
                }
                candidateList.createEntity(keys, values);

                /*
                cReference = candidateList.search(Candidates.NAME_KEY, name);
                if (cReference.equals(Candidates.UNKNOWN)) cReference = candidateList.search(Candidates.FB_ID_KEY, id);
                if (cReference == Candidates.UNKNOWN && email != null) cReference = candidateList.search(Candidates.EMAIL_KEY, email);

                if (cReference == Candidates.UNKNOWN) cReference = candidateList.createCandidate(Candidates.FB_ID_KEY, id);
                if (name != null) candidateList.add(cReference, Candidates.NAME_KEY, name);
                if (email != null) candidateList.add(cReference, Candidates.EMAIL_KEY, email);
                if (location != null) candidateList.add(cReference, Candidates.LOCATION_KEY, location);

                */
                //re-init to null
                location=null; email=null;
            }



            //relations
            reader = startReader("fb_relationships");
            reader.getAll(new BasicDBObject());

            String pid = null, relationid = null;
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                relationid = obj.getString("relation");
                pid = obj.getString("id");

                //logger.info("relationships = " + pid + " " + relationid);

                List<Candidates.CandidateReference> relRef = candidateList.search(Candidates.FB_ID_KEY, relationid);
                List<Candidates.CandidateReference> selfRef = candidateList.search(Candidates.FB_ID_KEY, pid);
                if (relRef.size() != 1) throw new NullPointerException();
                if (selfRef.size() != 1) throw new NullPointerException();
                knowsGraph.put(relRef.get(0), selfRef.get(0));
                knowsGraph.put(selfRef.get(0), relRef.get(0));
            }

            //events
            reader = startReader("fb_events");
            reader.getAll(new BasicDBObject());
            long start=0, end=0;
            Time interval;
            String title;
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                title = obj.getString("name");
                start = Long.parseLong(obj.getString("start_time"));
                end = Long.parseLong(obj.getString("end_time"));
                interval = Time.createFromInterval(start, end);
                //logger.info("events = " + interval + " " + title);
            }


            close();
        }

    }

}


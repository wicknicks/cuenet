package esl.cuenet.algorithms.firstk.personal.accessor;

import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

import java.util.List;

public class Facebook implements Source {

    private Logger logger = Logger.getLogger(Facebook.class);
    private Candidates candidateList = Candidates.getInstance();


    public Facebook() {
        FBLoader loader = new FBLoader();
        loader.load();
    }

    @Override
    public List<Object> eventsContaining(Object person, Time interval, Location location) {
        return null;
    }

    @Override
    public List<Object> participants(Object event) {
        return null;
    }

    @Override
    public List<Object> subevents(Object event) {
        return null;
    }

    @Override
    public List<Object> knows(Object person) {
        return null;
    }

    @Override
    public List<Object> knowsAtTime(Object person, Time time) {
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

                Candidates.CandidateReference cReference = null;
                cReference = candidateList.search(Candidates.NAME_KEY, name);
                if (cReference == Candidates.UNKNOWN) cReference = candidateList.search(Candidates.FB_ID_KEY, id);
                if (cReference == Candidates.UNKNOWN && email != null) cReference = candidateList.search(Candidates.EMAIL_KEY, email);

                if (cReference == Candidates.UNKNOWN) cReference = candidateList.createCandidate(Candidates.FB_ID_KEY, id);
                if (name != null) candidateList.add(cReference, Candidates.NAME_KEY, name);
                if (email != null) candidateList.add(cReference, Candidates.EMAIL_KEY, email);
                if (location != null) candidateList.add(cReference, Candidates.LOCATION_KEY, location);

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

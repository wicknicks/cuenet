package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

import java.util.List;

public class Conference implements Source {

    private Candidates candidateSet = Candidates.getInstance();
    private Logger logger = Logger.getLogger(Conference.class);

    protected Conference() {
        (new ConferenceLoader()).load();
    }

    private static Conference instance = new Conference();
    public static Conference getInstance() {
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
        return null;
    }

    @Override
    public List<EventContextNetwork> knowsAtTime(Candidates.CandidateReference person, Time time) {
        return null;
    }

    private class ConferenceObject {
        Time interval;
        Location location;
        List<Candidates.CandidateReference> participants = Lists.newArrayList();
        BasicDBObject information = new BasicDBObject();
    }

    public class ConferenceLoader extends MongoDB {

        public ConferenceLoader() {
            super(PConstants.DBNAME);
//            super("jain");
        }

        public List<ConferenceObject> load() {
            MongoDB.DBReader reader = startReader("conferences");
            reader.getAll(new BasicDBObject());

            List<ConferenceObject> conferenceObjs = Lists.newArrayList();
            while (reader.hasNext()) {
                BasicDBObject object = (BasicDBObject) reader.next();
                ConferenceObject confObject = new ConferenceObject();

                long _sdate = object.getLong("start-date");
                long _edate = object.getLong("end-date");
                String _title = object.getString("title");
                String _short = object.getString("short");
                String _url = object.getString("url");

                confObject.interval = Time.createFromInterval(_sdate, _edate);
                confObject.information.put("title", _title);
                confObject.information.put("short", _short);
                confObject.information.put("url", _url);

                if (object.containsField("hashtag"))
                    confObject.information.put("hashtag", object.getString("hashtag"));

                conferenceObjs.add(confObject);

            }


            for (ConferenceObject confObject: conferenceObjs)
                confObject.participants = loadParticipant(confObject.information.getString("url"));

            return conferenceObjs;
        }

        private List<Candidates.CandidateReference> loadParticipant(String url) {
            MongoDB.DBReader reader = startReader("conf_attendees");
            reader.query(new BasicDBObject("url", url));

            List<Candidates.CandidateReference> attendees = Lists.newArrayList();

            List<String> name = Lists.newArrayList(Candidates.NAME_KEY);

            while (reader.hasNext()) {
                BasicDBObject object = (BasicDBObject) reader.next();
                Candidates.CandidateReference attn = candidateSet.createEntity(name, Lists.newArrayList(object.getString("name")));
                attendees.add(attn);
            }

            logger.info(attendees.size() + " attendees loaded for " + url);

            return attendees;
        }
    }
}

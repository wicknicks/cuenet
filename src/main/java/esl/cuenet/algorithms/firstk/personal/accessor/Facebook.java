package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Facebook implements Source {

    private Logger logger = Logger.getLogger(Facebook.class);
    private Candidates candidateList = Candidates.getInstance();

    private Multimap<Candidates.CandidateReference, Candidates.CandidateReference> knowsGraph = HashMultimap.create();
    private List<FBEvent> events = Lists.newArrayList();
    private List<FBPhoto> photos = Lists.newArrayList();


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

        List<EventContextNetwork> nets = null;
        for (FBEvent event: events) {
            if (event.time.contains(interval) && event.participants.contains(person))  {
                if (nets == null) nets = Lists.newArrayList();
                addToNets(nets, event, location);
            }
        }

        if (nets != null && nets.size() > 0) logger.info("Found " + nets.size() + " facebook event.");
        return nets;
    }

    private void addToNets(List<EventContextNetwork> nets, FBEvent event, Location location) {

        if (event.sent) return;

        EventContextNetwork network = new EventContextNetwork();

        EventContextNetwork.ECNRef eventRef = network.createEvent("event", event.time.getStart(), event.time.getEnd(), location.getFullAddress());
        network.initializeSubeventTree(eventRef);

        for (Candidates.CandidateReference candidate: event.participants) {
            EventContextNetwork.ECNRef pRef = network.createPerson(candidate);
            network.createPartiticipationEdge(eventRef, pRef);
        }

        event.sent = true;
        nets.add(network);
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

    @Override
    public void writeInstances(FileWriter instanceFileWriter) throws IOException {
        int instance_count = 0;
        int event_id = 8;

        for (FBPhoto obj: photos) {
            instance_count++;
            instanceFileWriter.write(event_id + " " + instance_count);
            instanceFileWriter.write('\n');
            instanceFileWriter.write(event_id + ", " + obj.participants.toString() + ", " + (obj.time.getStart() + obj.time.getEnd())/2);
            instanceFileWriter.write('\n');
            instanceFileWriter.write("====================================\n");
        }
    }

    class FBEvent {
        Time time;
        BasicDBObject information;
        List<Candidates.CandidateReference> participants;
        boolean sent = false;
    }

    class FBPhoto {
        Time time;
        String id;
        List<Candidates.CandidateReference> participants;
    }

    public class FBLoader extends MongoDB {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

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
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                FBEvent fbEvent = new FBEvent();
                start = (Long.parseLong(obj.getString("start_time")) + 0 * 3600) * 1000;
                end = (Long.parseLong(obj.getString("end_time")) + 0 * 3600) * 1000;

                fbEvent.time = Time.createFromInterval(start, end);
                fbEvent.information = new BasicDBObject();

                fbEvent.information.put("eid", obj.getString("eid"));
                if (obj.containsField("name")) fbEvent.information.put("title", obj.getString("name"));
                if (obj.containsField("location")) fbEvent.information.put("location", obj.getString("location"));
                if (obj.containsField("description")) fbEvent.information.put("description", obj.getString("description"));

                events.add(fbEvent);
            }

            logger.info("Loaded " + events.size() + " events.");

            for (FBEvent e: events) loadAttendees(e);

            //loadPhotos();

            close();
        }

        private void loadPhotos() {

            logger.info("Loading FB Photos");

            DBReader reader = this.startReader("fb_photos");
            BasicDBObject keys = new BasicDBObject();
            keys.put("_id", 0);

            reader.getAll(keys);

            String tagId, tagName, date;

            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();

                if ( !obj.containsField("created_time") ) {
                    logger.error("object doesn't have 'created_time' field: " + obj);
                    continue;
                }

                date = obj.getString("created_time");
                long t = parseFBDate(date).getTime();

                FBPhoto photo = new FBPhoto();
                photo.time = Time.createFromMoment(t);
                photo.participants = Lists.newArrayList();

                if (obj.containsField("tags")) {
                    BasicDBObject _tags = (BasicDBObject) obj.get("tags");
                    if (_tags.containsField("data")) {
                        for (Object o: (BasicDBList) _tags.get("data")) {

                            tagId = null; tagName = null;
                            BasicDBObject tag = (BasicDBObject) o;

                            if (tag.containsField("id")) tagId = tag.getString("id");

                            Candidates.CandidateReference ref = candidateList.searchLimitOne(Candidates.FB_ID_KEY, tagId);
                            if (ref == Candidates.UNKNOWN || ref == null) continue;

                            photo.participants.add(ref);
                        }
                    }
                }
                photos.add(photo);
            }

            logger.info("Loaded " + photos.size() + " Photos");
        }

        private Date parseFBDate(String sDate) {
            if (sDate == null) return new Date(0);

            Date dt;
            try {
                dt = dateFormat.parse(sDate);
            } catch (ParseException e) {
                logger.error("Date couldn't be parsed: "  + sDate);
                logger.error("MESSAGE: " + e.getMessage());
                dt = new Date(0);
            }

            return dt;

        }

        private void loadAttendees(FBEvent event) {
            MongoDB.DBReader reader = startReader("fb_event_attendees");
            reader.query(new BasicDBObject("eid", event.information.get("eid")));
            event.participants = Lists.newArrayList();
            while(reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();
                Candidates.CandidateReference ref = candidateList.searchLimitOne(Candidates.FB_ID_KEY, obj.getString("id"));
                if (ref == null) {
                    logger.info("Ref = null for id " +  obj.getString("name"));
                    continue;
                }
                event.participants.add(ref);
            }

            if (event.participants.size() > 0)
                logger.info("Event has " + event.participants.size() + " attendees");
        }
    }
}


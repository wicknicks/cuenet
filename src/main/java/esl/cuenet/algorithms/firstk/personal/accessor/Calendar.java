package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.algorithms.firstk.personal.Utils.RFC3339DateFormatter;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Calendar implements Source {

    private Candidates candidateSet = Candidates.getInstance();
    private Logger logger = Logger.getLogger(Calendar.class);

    private DateFormat dateTimeFormat = new RFC3339DateFormatter();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static Source instance = new Calendar();

    protected Calendar() {
        (new CalendarLoader()).load();
    }

    public static Source getInstance() {
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

    @Override
    public void writeInstances(FileWriter instanceFile) {
    }

    private class CalendarObject {
        Time time;
        Location location;
        List<Candidates.CandidateReference> participants = Lists.newArrayList();
        BasicDBObject information = new BasicDBObject();
    }

    public class CalendarLoader extends MongoDB {

        public CalendarLoader() {
            super(PConstants.DBNAME);
        }

        public List<CalendarObject> load() {
            MongoDB.DBReader reader = startReader("google_calendar");
            reader.getAll(new BasicDBObject());
            Candidates.CandidateReference userReference =
                    candidateSet.searchLimitOne(Candidates.EMAIL_KEY, PConstants.EMAIL);

            reader.getAll(new BasicDBObject());

            List<CalendarObject> calendarObjs = Lists.newArrayList();
            while (reader.hasNext()) {
                BasicDBObject object = (BasicDBObject) reader.next();
                CalendarObject cal = new CalendarObject();
                cal.participants.add(userReference);

                if (object.containsField("status") && object.getString("status").equals("cancelled")) continue;

                if (object.containsField("summary")) cal.information.put("title", object.getString("summary"));
                if (object.containsField("type")) cal.information.put("type", object.getString("type"));
                if (object.containsField("link")) cal.information.put("link", object.getString("link"));

                if (object.containsField("description")) {
                    String desc = object.getString("description");
                    for (String d: desc.split("\n")) {
                        Candidates.CandidateReference participant = candidateSet.searchLimitOne(Candidates.NAME_KEY, d);
                        if (participant != null) cal.participants.add(participant);
                    }
                }

                long start = -1, end = -1;
                if (object.containsField("start")) start = getTime(object, "start");
                if (object.containsField("end")) end = getTime(object, "end");
                if (start == -1 || end == -1)
                    throw new RuntimeException("Did not find dates" + start + " " + end + " " + object);

                cal.time = Time.createFromInterval(start, end);
                calendarObjs.add(cal);
            }

            logger.info("Loaded " + calendarObjs.size() + " calendar objects.");

            close();
            return calendarObjs;
        }


        private long getTime(BasicDBObject object, String label) {
            BasicDBObject o = (BasicDBObject) object.get(label);

            Date d;

            if ( o.containsField("date") ) {
                try {
                    d =  dateFormat.parse(o.getString("date"));
                    return d.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();

                }
            }

            else {
                try {
                    d = dateTimeFormat.parse(o.getString("dateTime"));
                    return d.getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return -1;

        }



    }
}

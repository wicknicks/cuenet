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

import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Twitter implements Source {

    private Candidates candidateSet = Candidates.getInstance();
    private Logger logger = Logger.getLogger(Twitter.class);

    private Multimap<String, TweetObject> hashtagIndex = HashMultimap.create();

    protected Twitter() {
        (new TwitterLoader()).load();
    }

    private static Twitter instance = new Twitter();
    public static Twitter getInstance() {
        return instance;
    }

    public void lookupHashtag(String query) {
        for (TweetObject tw: hashtagIndex.get(query.toLowerCase()))
            logger.info(candidateSet.get(tw.source).toStringKey(Candidates.NAME_KEY));

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
        if ( !event.getInformation().containsField("hashtag") )
            return null;

        String hashtag = event.getInformation().getString("hashtag");
        List<EventContextNetwork> networks = Lists.newArrayList();



        return networks;
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

    private class TweetObject {
        Time time;
        Location location;
        Candidates.CandidateReference source;
        BasicDBObject information = new BasicDBObject();
    }

    public class TwitterLoader extends MongoDB {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        public TwitterLoader() {
//            super(PConstants.DBNAME);
            super("jain");
        }

        public List<TweetObject> load() {
            MongoDB.DBReader reader = startReader("tweets");
            reader.getAll(new BasicDBObject());

            List<TweetObject> tweets = Lists.newArrayList();
            while (reader.hasNext()) {
                BasicDBObject object = (BasicDBObject) reader.next();
                String text = object.getString("text");

                BasicDBObject user = (BasicDBObject) object.get("user");
                if (user == null) continue;

                String name = user.getString("name");
                if (name == null) continue;

                String _time = object.getString("created_at");
                if (_time == null) continue;

                TweetObject tweet = new TweetObject();
                tweet.time = Time.createFromMoment(getTime(_time));

                tweet.source = candidateSet.createEntity(Lists.newArrayList(Candidates.NAME_KEY),
                        Lists.newArrayList(name));

                for (String t: text.split("\\s+"))
                    if (t.toLowerCase().charAt(0) == '#') hashtagIndex.put(t.toLowerCase(), tweet);

                tweet.information = object;
                tweets.add(tweet);
            }

            return tweets;
        }

        private long getTime(String time) {

            try {
                Date d = format.parse(time);
                return d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return -1;
        }

    }
}

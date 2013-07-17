package esl.cuenet.algorithms.firstk.personal;

import com.google.common.collect.*;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Ontology {

    private ImmutableList<URI> eventList = null;
    private HashMap<String, URI> uriIndex = new HashMap<String, URI>();

    private Multimap<URI, URI> subeventIndex = HashMultimap.create();
    private Multimap<URI, URI> supereventIndex = HashMultimap.create();
    private Multimap<URI, URI> isAIndex = HashMultimap.create();

    public static String PERSON = "Person";

    public Ontology() {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<URI> getAllEvents() {
        return eventList;
    }

    public List<URI> getSubevents(URI event) {
        if ( !subeventIndex.containsKey(event) ) return null;
        return Lists.newArrayList(subeventIndex.get(event));
    }

    public URI getSuperevent(URI event) {
        if (supereventIndex.containsKey(event))
            return supereventIndex.get(event).iterator().next();

        return null;
    }

    private void load() throws IOException {
        String data = FileUtils.readFileToString(
                new File("/home/arjun/Documents/Dropbox/Ontologies/cuenet-main/cuenet-main.js"));
        BasicDBObject object = (BasicDBObject) JSON.parse(data);

        BasicDBList events = (BasicDBList) object.get("events");
        BasicDBList subevent = (BasicDBList) object.get("subevent");
        BasicDBList isA = (BasicDBList) object.get("isA");

        List<URI> tempEventList = Lists.newArrayList();
        for (Object o: events) {
            URI uri = new URI(o.toString());
            uriIndex.put(o.toString(), uri);
            tempEventList.add(uri);
        }

        ImmutableList.Builder<URI> builder = new ImmutableList.Builder<URI>();
        builder.addAll(tempEventList);
        eventList = builder.build();

        for (Object o: subevent) {
            BasicDBObject subeventRelation = (BasicDBObject) o;
            String _super = subeventRelation.getString("super");
            String _sub = subeventRelation.getString("sub");

            subeventIndex.put(uriIndex.get(_super), uriIndex.get(_sub));
            supereventIndex.put(uriIndex.get(_sub), uriIndex.get(_super));
        }

        for (Object o: isA) {
            BasicDBObject isARelation = (BasicDBObject) o;

            String _parent = isARelation.getString("parent");
            String _child = isARelation.getString("child");

            isAIndex.put(uriIndex.get(_child), uriIndex.get(_parent));
        }

    }

    public void printAll() {
        System.out.println(eventList);

        System.out.println(subeventIndex);
        System.out.println(supereventIndex);

        System.out.println(isAIndex);
    }


    public static class URI {
        String uri;

        public URI(String uri) {
            this.uri = uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            URI uri1 = (URI) o;

            if (!uri.equals(uri1.uri)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public String toString() {
            return uri;
        }
    }

}

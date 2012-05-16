package esl.cuenet.query.drivers.mongodb;

import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHelper extends MongoDB {

    public MongoDBHelper() {
        super("test");
    }

    public List<BasicDBObject> query(String collection, BasicDBObject query) {
        DBReader reader = startReader(collection);
        reader.query(query);

        List<BasicDBObject> objects = new ArrayList<BasicDBObject>();

        while(reader.hasNext()) objects.add((BasicDBObject) reader.next());

        return objects;
    }

}

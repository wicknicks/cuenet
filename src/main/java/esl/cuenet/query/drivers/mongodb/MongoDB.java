package esl.cuenet.query.drivers.mongodb;

import com.mongodb.*;

import java.net.UnknownHostException;


public class MongoDB {

    private String dbName = null;
    private boolean isOpen = false;
    private DB db = null;

    private String location = "localhost";

    public MongoDB(String dbName) {
        this.dbName = dbName;
        open(dbName);
    }

    protected void openDB(String location, String dbName) {
        if (isOpen) return;

        try {
            Mongo m = new Mongo(location, 27017);
            db = m.getDB(dbName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        isOpen = true;
    }

    public void open(String databaseName) {
        openDB(location, databaseName);
    }

    public void close() {
        db.getMongo().close();
        isOpen = false;
    }

    public DBWriter startWriter(String collectionName) {
        return new DBWriter(db, collectionName);
    }

    public DBReader startReader(String collectionName) {
        return new DBReader(db, collectionName);
    }

    public String getDbName() {
        return dbName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    protected class DBReader {

        private DBCollection collection = null;
        private DBCursor cursor = null;

        public DBReader(DB db, String collectionName) {
            collection = db.getCollection(collectionName);
        }

        public void getAll() {
            cursor = collection.find();
        }

        public void query (BasicDBObject queryDBObject) {
            cursor = collection.find(queryDBObject);
        }

        public boolean hasNext() {
            return cursor.hasNext();
        }

        public int count() {
            if (cursor == null) throw new RuntimeException("No query issued");
            return cursor.count();
        }


        public DBObject next() {
            return cursor.next();
        }

        public void remove() {

        }
    }

    protected class DBWriter {

        private DBCollection collection = null;

        public DBWriter(DB db, String collectionName) {
            collection = db.getCollection(collectionName);
        }

        public void write(DBObject dbTuple) {
            collection.save(dbTuple);
        }

        public void delete(DBObject dbTuple) {
            collection.remove(dbTuple);
        }

        public void update(DBObject oldTuple, DBObject newTuple) {
            collection.update(oldTuple, newTuple);
        }

        public void dropCollection() {
            collection.drop();
        }
    }
}

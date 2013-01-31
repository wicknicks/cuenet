package esl.cuenet.ranking;

import org.neo4j.graphdb.Relationship;

public interface EntityBase {

    URINode lookup(String key, Object value);

    public final static String TYPE = "type";
    public final static String TEXT = "text";
    public final static String ENTITY = "entity";


    public final static String V_NAME = "name";
    public final static String V_EMAIL = "email";
    public final static String V_FB_ID = "fb_id";

}

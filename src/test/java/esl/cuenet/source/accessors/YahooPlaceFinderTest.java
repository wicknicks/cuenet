package esl.cuenet.source.accessors;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.system.SysLoggerUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

public class YahooPlaceFinderTest {

    private Logger logger = Logger.getLogger(YahooPlaceFinderTest.class);

    static {
        SysLoggerUtils.initLogger();
    }

    public YahooPlaceFinderTest() {
        super();
    }

    @Test
    public void runTest() throws IOException {

        YahooPlaceFinderAPI placeFinder = new YahooPlaceFinderAPI();
        BasicDBObject o = placeFinder.findAddress(30.2669, -97.7428);
        if (o != null) logger.info(o.toString());

        o = placeFinder.findLatLon("Eiffel Tower, Paris, France");
        if (o != null) logger.info(o.toString());

    }


    @Test
    public void urlTest() throws IOException, HttpException, DecoderException {

        URLCodec codec = new URLCodec();
        String rgeo = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D%2237.416275%2C%20-122.025092%22%20and%20gflags%3D%22R%22&appid=UmMtXR7c&format=json";
        System.out.println(codec.decode(rgeo));
        String placeName = null;
        String s = "http://query.yahooapis.com/v1/public/yql?q=" +
                codec.encode("select * from geo.placefinder WHERE text = \"" + placeName + "\"", "ISO-8859-1") +
                "&appid=UmMtXR7c&format=json";

        System.out.println(s);

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(rgeo);

        System.out.println("executing request " + httpget.getURI());

        // Create a response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpclient.execute(httpget, responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);
        System.out.println("----------------------------------------");

        BasicDBObject object = (BasicDBObject) JSON.parse(responseBody);
        BasicDBObject query = (BasicDBObject) object.get("query");
        if (query.getInt("count") == 1) {
            BasicDBObject r = (BasicDBObject) query.get("results");
            System.out.println("obj = " + r);
        }
        else {
            BasicDBObject r = (BasicDBObject) query.get("results");
            BasicDBList list = (BasicDBList) r.get("Result");
            System.out.println("list = " + list.get(0));
        }
    }

}

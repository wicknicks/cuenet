package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.accessor.*;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static EventContextNetwork load () {
        EventContextNetwork network = new EventContextNetwork();

        LocalFilePreprocessor.ExifExtractor extractor = new LocalFilePreprocessor.ExifExtractor();
        LocalFilePreprocessor.Exif exif;
        Time time = null;
        Location location = null;
        try {
            exif = extractor.extractExif(PConstants.IMAGE);
            time = Time.createFromMoment(exif.timestamp);
            location = Location.createFromGPS(exif.GPSLatitude, exif.GPSLongitude);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        EventContextNetwork.ECNRef pc = network.createEvent("photo-capture", exif.timestamp,
                exif.timestamp, location.getFullAddress());
        EventContextNetwork.ECNRef user = network.createPerson(Candidates.NAME_KEY, PConstants.USERNAME);

        network.initializeSubeventTree(pc);
        network.createPartiticipationEdge(pc, user);

        Discoverer discoverer = new Discoverer(network, SourceFactory.getSources(), time, location);
        discoverer.dnm();

        logger.info("Verification Calls = " + Verifier.getInstance().numVerificationCalls());
        
        return network;
    }

    public static void main(String[] args) {
        SysLoggerUtils.initLogger();

        Facebook.getInstance();
        Candidates.getInstance().logistics(false);
        Email.getInstance();

        //Candidates.getInstance().merge();
        Candidates.getInstance().logistics(false);

        EventContextNetwork network = load();
        if (network == null) return;

        network.printTree(true);
    }

}

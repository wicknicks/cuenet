package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import esl.cuenet.algorithms.firstk.personal.accessor.Source;
import esl.cuenet.algorithms.firstk.personal.accessor.SourceFactory;
import esl.system.SysLoggerUtils;

import java.io.IOException;

public class Main {

    public static LocalFilePreprocessor.Exif EXIF;

    public static EventContextNetwork load () {
        EventContextNetwork network = new EventContextNetwork();

        LocalFilePreprocessor.ExifExtractor extractor = new LocalFilePreprocessor.ExifExtractor();
        Time time = null;
        Location location = null;
        try {
            EXIF = extractor.extractExif(PConstants.IMAGE);
            time = Time.createFromMoment(EXIF.timestamp);
            location = Location.createFromGPS(EXIF.GPSLatitude, EXIF.GPSLongitude);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Source[] sources = SourceFactory.getFactory().getSources();

//        EventContextNetwork.ECNRef pc = network.createEvent("photo-capture", EXIF.timestamp,
//                EXIF.timestamp, location.getFullAddress());
//        EventContextNetwork.ECNRef user = network.createPerson(Candidates.NAME_KEY, PConstants.USERNAME);
//
//        network.initializeSubeventTree(pc);
//        network.createPartiticipationEdge(pc, user);
//
//        Discoverer discoverer = new Discoverer(network, sources, time, location);
//        discoverer.dnm();
//        discoverer.terminate();
        
        return network;
    }

    public static void main(String[] args) {
        SysLoggerUtils.initLogger();

        EventContextNetwork network = load();
        if (network == null) return;

        network.printTree(true);
    }

}

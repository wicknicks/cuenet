package esl.cuenet.generative.structs;

import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class SpaceTimeValueGenerators {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(getClass());


    public SpaceTimeValueGenerators(String locationValuesFilename) throws IOException {

        LineIterator iter = FileUtils.lineIterator(new File(locationValuesFilename));
        String bounds = null;
        if (iter.hasNext()) bounds = iter.next();
        else throw new RuntimeException();

        String tmp = null;
        double lat, lon;
        while (iter.hasNext()) {
            tmp = iter.next();
            String[] parts = tmp.split(",");
            lat = Double.parseDouble(parts[1]);
            lon = Double.parseDouble(parts[2]);
        }

    }

}

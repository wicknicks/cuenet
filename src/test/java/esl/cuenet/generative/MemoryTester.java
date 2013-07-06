package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import com.javamex.classmexer.MemoryUtil;


public class MemoryTester {

    static{
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(MemoryTester.class);

    @Test
    public void testMem() throws Exception {
        DataReader dReader = new DataReader();

        String filename = "/data/osm/inst-mid.sim";
        logger.info("String takes up: " + MemoryUtil.deepMemoryUsageOf(filename));

        logger.info("Loading network1... " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);
        logger.info("network1 takes up: " + MemoryUtil.deepMemoryUsageOf(network1));
    }

    @Test
    public void cmdMeasureContextNetworkSize() throws Exception {
        DataReader dReader = new DataReader();
        String simfile = (String) System.getProperties().get("simfile");
        if (simfile != null) {
            logger.info("----------------------------------------------------------------------------");
            logger.info(" L O A D I N G    " + simfile);
            logger.info("----------------------------------------------------------------------------");
            ContextNetwork network = dReader.readInstanceGraphs(simfile);
            logger.info("network size in bytes: " + MemoryUtil.deepMemoryUsageOf(network));
        }
        else
            logger.info("No simfile property found. Test skipped");
    }

}

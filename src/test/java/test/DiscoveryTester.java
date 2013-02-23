package test;

import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;

public class DiscoveryTester extends TestBase {

    private Logger logger = Logger.getLogger(Scratch.class);
    private String directory = "/home/arjun/Dataset/mm13/d7-futurict-boston-2013-jain/";
    private static final String ANNOTATIONS = ".annotations";

    @BeforeClass
    public static void setup() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void testDirectory() {
        String[] photos = (new File(directory)).list(new JPGFileChooser());

        int ix = 0;
        for (String p: photos) {
            String path = FilenameUtils.concat(directory, p);
            if (isAnnotationFileEmpty(path + ANNOTATIONS)) continue;
            logger.info(ix++ + ". " + path);
        }

    }

    public void singleFileTest(String photo) throws Exception {
        File file = new File(FilenameUtils.concat(directory, photo));
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.execute(new LocalFileDataset(file));
    }

    public boolean isAnnotationFileEmpty(String annotationsFilePath) {
        return (FileUtils.sizeOf(new File(annotationsFilePath)) == 0);
    }

    private class JPGFileChooser implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".JPG");
        }
    }

}

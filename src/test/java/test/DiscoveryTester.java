package test;

import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.system.ExperimentsLogger;
import esl.system.SysLoggerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DiscoveryTester extends TestBase {

    private Logger logger = Logger.getLogger(DiscoveryTester.class);
    private String directory = "/home/arjun/Dataset/mm13/d2-turing100-jain/";
    private static final String ANNOTATIONS = ".annotations";


    @BeforeClass
    public static void setup() {
        ExperimentsLogger.getInstance("/home/arjun/Dataset/mm13/jain.log.40");
        SysLoggerUtils.initLogger();
    }

    @AfterClass
    public static void shutdown() {
        ExperimentsLogger.getInstance().close();
    }

    @Test
    public void testDirectory() throws Exception {
        String[] photos = (new File(directory)).list(new JPGFileChooser());
        Arrays.sort(photos);

        String[] annotations;
        for (int i=20; i<photos.length; i++) {
            String photo = photos[i];
            String path = FilenameUtils.concat(directory, photo);
            if (isAnnotationFileEmpty(path + ANNOTATIONS)) continue;
            annotations = getAnnotations(path + ANNOTATIONS);
            if (annotations.length == 0) continue;

            logger.info(i + ". " + path);
            ExperimentsLogger.getInstance().list(i + ". " + path);

            ExperimentsLogger.getInstance().list("Annotations = " + StringUtils.join(annotations, ','));

            long start = System.currentTimeMillis();
            singleFileTest(photo, annotations);
            ExperimentsLogger.getInstance().list("Time Taken = " + (System.currentTimeMillis() - start)/1000);

            //if (photo.equals("DSC06646.JPG")) break;

            ExperimentsLogger.getInstance().list("============");
        }
    }

    @Test
    public void testSingleFile() throws Exception {
        String singlePhotoPath = "DSC01942.JPG"; //photos[0];
        String path = FilenameUtils.concat(directory, singlePhotoPath);
        ExperimentsLogger.getInstance().list(path);
        String[] annotations = getAnnotations(path + ANNOTATIONS);
        if (annotations.length == 0) return;
        ExperimentsLogger.getInstance().list("Annotations = " + StringUtils.join(annotations, ','));
        singleFileTest(singlePhotoPath, annotations);
    }


    public String[] getAnnotations(String path) throws Exception {
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String tmp = null;
        ArrayList<String> annotations = new ArrayList<String>();
        int ix=0, eix = 0;
        while (true) {
            tmp = reader.readLine();
            if (tmp == null) break;
            if (!tmp.contains("\"")) continue;
            ix = tmp.indexOf('"');
            eix = tmp.indexOf('"', ix+1);
            annotations.add(tmp.substring(ix+1, eix));
        }

        String a[] = new String[annotations.size()];
        annotations.toArray(a);
        return a;
    }

    public void singleFileTest(String photo, String[] annotations) throws Exception {
        File file = new File(FilenameUtils.concat(directory, photo));
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.setK(annotations.length);
        firstKDiscoverer.execute(new LocalFileDataset(file, annotations));
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

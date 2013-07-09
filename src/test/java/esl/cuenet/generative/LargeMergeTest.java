package esl.cuenet.generative;

import com.javamex.classmexer.MemoryUtil;
import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LargeMergeTest {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(LargeMergeTest.class);

    public void testLoadSampleMerge(String filename, int _sample_count, double percentage) throws Exception {

        assert _sample_count > 1;

        DataReader dReader = new DataReader();

        logger.info("Loading network1... " + filename);
        ContextNetwork network1 = dReader.readInstanceGraphs(filename);
        //logger.info("Nodes: " + network1.nodeCount() + " ; memory = " + MemoryUtil.deepMemoryUsageOf(network1));

        logger.info("Creating Samples... " + _sample_count);
        //Generate samples
        List<ContextNetwork> samples = NetworkBuildingHelper.sample(network1, _sample_count, percentage);

        for (ContextNetwork sample : samples) {
            if ( !NetworkBuildingHelper.validateSample(network1, sample) )
                logger.info("Invalid Sample");
        }

        ContextNetwork merge = samples.get(0);
        long s;
        for (int i=1; i<samples.size(); i++) {
            s = System.currentTimeMillis();
            logger.info("Merging Sample #" + i);
            merge.merge(samples.get(i));
            logger.info("Post merge node count #" + merge.nodeCount() + " ; time  = " + (System.currentTimeMillis() - s));
            //logger.info("Size of merge graph: " + MemoryUtil.deepMemoryUsageOf(merge));
            if ( !NetworkBuildingHelper.validateSample(network1, merge) )
                logger.info("Merge corrupted merge graph");
        }

        logger.info("Creating Instance Nets... ");
        //Create nets from each instance
        //Merging these gurantee that all nodes in n1 will be in merge
        List<ContextNetwork> instanceNets = NetworkBuildingHelper.createNetworkForEachInstace(network1);

        logger.info("Merging Instance Nets");
        s = System.currentTimeMillis();
        for (ContextNetwork instanceNet : instanceNets)
            merge.merge(instanceNet);
        logger.info("Merges Complete Merges = " + instanceNets.size() + "; Nodes = "  + merge.nodeCount() +
                "; time = " + (System.currentTimeMillis() - s));

        if ( !NetworkBuildingHelper.validateSample(network1, merge) )
            logger.info("Merge corrupted merge graph");

        logger.info("checkorder: " + NetworkBuildingHelper.checkOrderStrict(merge));


        logger.info("Loading network2... ");
        ContextNetwork network2 = dReader.readInstanceGraphs(filename);

        logger.info("Final node counts: " + merge.nodeCount() + " " + network2.nodeCount());
        //logger.info("Size of merge graph: " + MemoryUtil.deepMemoryUsageOf(merge));

        boolean val = merge.compareNetwork(network2);
        logger.info("Equals: " + val);
        Assert.assertEquals(val, true);
    }

    @Test
    public void checkOrderTest() throws Exception {
        DataReader dReader = new DataReader();
        logger.info("Loading network1... ");
        ContextNetwork network1 = dReader.readInstanceGraphs("/data/osm/instance.sim.4");
        logger.info("strict check " + NetworkBuildingHelper.checkOrderStrict(network1));
    }

    @Test
    public void testSmall() throws Exception {
        testLoadSampleMerge("/data/osm/inst-small.sim", 10, 0.1);
    }

    @Test
    public void testMid() throws Exception {
        //testLoadSampleMerge("/data/osm/inst-mid.sim", 5);
        testLoadSampleMerge("/data/osm/inst-mid.sim", 5, 0.1);
    }

    @Test
    public void repeatTestMid() throws Exception {
        for (int i=1; i<3; i++) {
            testLoadSampleMerge("/data/osm/instance.sim.4", 5*i, 0.1);
            logger.info("------------------------------------------");
        }
    }

    @Test
    public void multiDatasetRepeatTestMid() throws Exception {
        String[] filenames = new String[10];
        for (int i=1; i<=10; i++) filenames[i-1] = "/data/osm/10/instance.sim." + i;
        for (String file: filenames) {
            testLoadSampleMerge(file, 50, 0.1);
            logger.info("------------------------------------------ " + file);
        }
    }

    @Test
    public void testLarge() throws Exception {
        testLoadSampleMerge("/data/osm/instance.sim.6", 5, 0.1);
    }

    @Test
    public void testMergeWithIncreasingSampleSize() throws Exception {
        String filename = "/data/osm/6/instance.sim.7";
        for (double p=0.1; p<=0.7; p+=0.1) {
            testLoadSampleMerge(filename, 10, 0.1);
            logger.info("------------------------------------------ sampling at " + p);
        }
    }

    @Test
    public void cmdMergeWithIncreasingDepth() throws Exception {
        DataReader dReader = new DataReader();

        String primesim = System.getProperty("primesim");
        if (primesim == null) logger.info("primesim = null, skipping");

        String mergesim = System.getProperty("mergesim");
        if (mergesim == null) logger.info("mergesim = null, skipping");

//        String mergesim = "/home/arjun/data/cuenet/simplemerge/instance.sim.1";
//        String primesim = "/home/arjun/data/cuenet/simplemerge/prime.sim";

        ContextNetwork prime = dReader.readInstanceGraphs(primesim);
        NetworkBuildingHelper.createTimeIntervals(prime);

        dReader.offset = 100;
        ContextNetwork merge = dReader.readInstanceGraphs(mergesim);
        NetworkBuildingHelper.createTimeIntervals(merge);

        logger.info("prime = " + primesim);
        logger.info("merge = " + mergesim);

        logger.info("Starting merge");
        long s = System.currentTimeMillis();
        prime.merge(merge);
        long e = System.currentTimeMillis();
        logger.info("Time Taken to Merge = " + (e-s));
        logger.info(NetworkBuildingHelper.depth(prime));
    }


    @Test
    public void cmdMultiMerge() throws Exception {
        DataReader dReader = new DataReader();

        String prefix = System.getProperty("prefix");
        if (prefix == null) {
            logger.info("prefix = null, skipping");
            return;
        }

        String start = System.getProperty("start");
        if (start == null) {
            logger.info("start = null, skipping");
            return;
        }

        String end = System.getProperty("end");
        if (end == null) {
            logger.info("end = null, skipping");
            return;
        }

        int _start = Integer.parseInt(start);
        int _end = Integer.parseInt(end);

        ContextNetwork prime = dReader.readInstanceGraphs(prefix + start);
        NetworkBuildingHelper.createTimeIntervals(prime);
        ContextNetwork merge;

        long st, total = 0;


        for (int i=_start + 1, j=0; i<=_end; i++, j++) {
            try {
                merge = dReader.readInstanceGraphs(prefix + i);
                NetworkBuildingHelper.createTimeIntervals(merge);

                st = System.currentTimeMillis();
                prime.merge(merge);
                total += System.currentTimeMillis() - st;

                if (j % 100 == 0) {
                    logger.info("Time till " + j + " merges = " + total);
                    logger.info("NC = " + prime.nodeCount());
                    total = 0;
                }

            } catch (Exception ex) {
                logger.error("Exception while loading " + ex.getMessage() + " at i = " + (prefix+i));
            }
        }

        logger.info("Time till " + (_end - _start) + " merges = " + total);
    }

    @Test
    public void cmdMultiMergeTest() throws Exception {
        DataReader dReader = new DataReader();

        String prefix = System.getProperty("prefix");
        if (prefix == null) {
            logger.info("prefix = null, skipping");
            return;
        }

        String start = System.getProperty("start");
        if (start == null) {
            logger.info("start = null, skipping");
            return;
        }

        String end = System.getProperty("end");
        if (end == null) {
            logger.info("end = null, skipping");
            return;
        }


//        String prefix = "/home/arjun/data/cuenet/multimerge/instance.sim.";
//        String start = "1";
//        String end = "1000";


        int _start = Integer.parseInt(start);
        int _end = Integer.parseInt(end);
        ContextNetwork[] networks = new ContextNetwork[_end - _start + 1];
        String[] names = new String[_end - _start + 1];

        logger.info(_start + " " + prefix + " " + _end);

        long s = System.currentTimeMillis();
        for (int i=_start, j=0; i<=_end;i++, j++) {
            try {
                networks[j] = dReader.readInstanceGraphs(prefix + i);
                names[j] = prefix + i;
                NetworkBuildingHelper.createTimeIntervals(networks[j]);
                int d = NetworkBuildingHelper.depth(networks[j]);
                if (d > 20) logger.info("depth = " + d);
            } catch (Exception ex) {
                logger.error("Exception while loading " + ex.getMessage() + " at i = " + (prefix+i));
            }
        }
        long e = System.currentTimeMillis();
        logger.info("Loaded Networks in " + (e-s));

        s = System.currentTimeMillis();
        for (int i=1; i<networks.length; i++) {
            logger.info("merging " + names[0] + " with " + names[i]);
            try {
                networks[0].merge(networks[i]);
            } catch (Exception ex) {
                s = System.currentTimeMillis();
                logger.error("Exception " + ex.getClass().getCanonicalName() + " " +
                        ex.getMessage() + " " + prefix + (_start + i));
                logger.info("Time taken so far = " + (e-s));
            }
            if (i%100 == 0) {
                e = System.currentTimeMillis();
                logger.info("Time taken for " + i + " merges = " + (e-s));
            }
            int d = NetworkBuildingHelper.depth(networks[0]);
            logger.info(d);
        }
        e = System.currentTimeMillis();
        logger.info("Time taken to merge = " + (e-s));
    }
}


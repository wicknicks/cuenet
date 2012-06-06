package benchmarks;

import esl.cuenet.mapper.parser.ParseException;
import esl.datastructures.graph.relationgraph.RelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphNode;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class GraphBenchmarks {

    private Logger logger = Logger.getLogger(GraphBenchmarks.class);
    private long start = 0;

    public void benchmarkSimpleGraph() throws IOException, ParseException {
        RelationGraph graph = new RelationGraph();
        RelationGraphNode[] typeNodes = new RelationGraphNode[5];
        Random generator = new Random(System.currentTimeMillis());

        recordTick();

        //create 100 type nodes
        for (int i=0; i<typeNodes.length; i++) {
            typeNodes[i] = graph.createNode("t" + i);
        }

        recordTick("Created Type Nodes");

        //create 100 nodes
        for (int i=0; i<5; i++) {
            RelationGraphNode n = graph.createNode("n" + i);
            graph.createEdge("type-of", n, typeNodes[generator.nextInt(typeNodes.length)]);
        }

        recordTick("Created Instance Nodes");

        for (int i=0; i<typeNodes.length; i++) {
            List<RelationGraphNode> l = graph.getNodesOfType(typeNodes[i].name());
            logger.info(l.size());
        }
    }

    private void recordTick() {
        logger.info("Time Elapsed: " + (System.currentTimeMillis() - start));
    }

    private void recordTick(String msg) {
        logger.info("Time Elapsed (" + msg + "): " + (System.currentTimeMillis() - start));
    }

    private void time() throws Exception {
        start = System.currentTimeMillis();
        benchmarkSimpleGraph();
        long end = System.currentTimeMillis();
        logger.info("Total Execution Time: " + (end - start));
    }

    public static void main(String[] args) throws Exception {
        SysLoggerUtils.initLogger();
        (new GraphBenchmarks()).time();
    }
}

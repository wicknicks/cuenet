package esl.cuenet.query.pattern.graph;

import esl.cuenet.query.pattern.parser.ModelPatternParser;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Stack;

public class PatternGraphTraversalTest {

    static {
        SysLoggerUtils.initLogger();

    }

    private Logger logger = Logger.getLogger(PatternGraphTraversalTest.class);

    @Test
    public void traversePGTest() {
        PatternGraphConstructor constructor = new PatternGraphConstructor();
        ModelPatternParser parser = new ModelPatternParser("(a: ( (b: ( (x:(xx -> xxx)) -> y -> (z:((zz:(zzz)))) )) -> (c: (p -> (q: (m -> n -> (o:(oo -> ooo)) )) -> r) ) ) )");

        try {
            parser.parse(constructor);
        } catch (Exception p) {
            System.out.println("EXCEPT: " + p.getMessage());
        } catch (Error r) {
            System.out.println("ERROR: " + r.getMessage());
        }

        PatternGraphNode subEventGraph = constructor.getGraph().getFirst();
        Stack<PatternGraphNode> dfsStack = new Stack<PatternGraphNode>();
        dfsStack.push(subEventGraph);

        while ( !dfsStack.empty() ) {
            PatternGraphNode node = dfsStack.pop();
            logger.info("At: " + node.label());
            if (node.getSubEventPatternGraph() != null) {
                for (int i=node.getSubEventPatternGraph().size()-1; i>=0; i--)
                    dfsStack.push(node.getSubEventPatternGraph().get(i));
            }
        }

        logger.info("");
    }

}

package esl.cuenet.source;

import esl.cuenet.query.QueryOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeMapper implements IMapper {

    private HashMap<String, TreeMapperNode> mapperNodeMap = new HashMap<String, TreeMapperNode>();

    @Override
    public void map(String pathExpression, Adornment adornment,
                    QueryOperator operator, Attribute attribute) {

    }

    @Override
    public void map(String pathExpression, Adornment adornment) {

    }

    @Override
    public void map(String pathExpression, QueryOperator operator) {

    }

    @Override
    public void map(String pathExpression, Attribute attribute) {

    }

    @Override
    public boolean containsPattern(String pathExpression) {
        return false;
    }

    private class TreeMapperNode {

        public String name;
        public Adornment adornment;
        public QueryOperator operator;
        public Attribute attribute;
        public List<TreeMapperNode> children = new ArrayList<TreeMapperNode>();

    }

}

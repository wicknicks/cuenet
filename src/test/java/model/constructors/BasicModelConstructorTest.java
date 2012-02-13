package model.constructors;

import esl.cuenet.model.constructors.BasicModelConstructor;
import org.junit.Test;

public class BasicModelConstructorTest {

    @Test
    public void doTest() {

        BasicModelConstructor construtor = new BasicModelConstructor();
        construtor.constructOntModel();

    }

}

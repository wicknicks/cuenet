package model.constructors;

import esl.cuenet.model.constructors.BasicModelConstructor;
import org.junit.Test;

import java.io.FileNotFoundException;

public class BasicModelConstructorTest {

    @Test
    public void doTest() {

        BasicModelConstructor construtor = new BasicModelConstructor();
        try {
            construtor.constructOntModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

package coyote.commons.network;

import coyote.dx.TransformEngine;
import coyote.loader.log.Log;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;


/**
 *
 */
public class ScpTest extends AbstractEngineTest {

    //@Test
    public void test() {

        // load the configuration from the class path
        TransformEngine engine = loadEngine("scptest");
        assertNotNull(engine);

        try {
            engine.run();
        } catch (Exception e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        }

        try {
            engine.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

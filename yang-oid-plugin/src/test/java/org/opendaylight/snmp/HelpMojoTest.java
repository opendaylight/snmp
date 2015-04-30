package org.opendaylight.snmp;

import org.junit.Test;

public class HelpMojoTest {

    @Test
    public void executeTest() throws Exception {
        HelpMojo helpMojo = new HelpMojo();
        helpMojo.execute();
    }
}

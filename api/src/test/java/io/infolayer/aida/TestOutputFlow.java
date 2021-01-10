package io.infolayer.aida;

import org.junit.Test;

import io.infolayer.aida.exception.OutputFlowException;
import io.infolayer.aida.plugin.OutputFlow;

public class TestOutputFlow {

    @Test
    public void testOutputFlow() throws OutputFlowException {
        OutputFlow flow = OutputFlow.newInstance("target/test");
    }
    
}

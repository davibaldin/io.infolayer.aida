package io.infolayer.aida.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.executor.runnable.UnixProcessRunnablePlugin;
import io.infolayer.aida.plugin.OutputFlow;

@TestInstance(Lifecycle.PER_CLASS)
public class TestStandardPlugins {

    public TestStandardPlugins() {

    }

    @Test
    public void testUnixProccessRunnablePlugin() throws Exception {

        OutputFlow flow = OutputFlow.newInstance("temp");

        UnixProcessRunnablePlugin plugin = new UnixProcessRunnablePlugin();
        plugin.configure(10, null, null);

        PluginCall call = new PluginCall(plugin.getPlugin().getName());
        call.addPluginParam("command", "date");

        plugin.submit(call, null, null, flow);
        plugin.run();

    }
    
}
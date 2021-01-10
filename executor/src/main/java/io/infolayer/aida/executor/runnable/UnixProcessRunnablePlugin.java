package io.infolayer.aida.executor.runnable;

import java.io.File;
import java.lang.reflect.Field;

import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.annotation.PluginParameter;
import io.infolayer.aida.executor.util.ParameterValueResolver;
import io.infolayer.aida.executor.util.TemplateRender;
import io.infolayer.aida.plugin.OutputFlow;

@Plugin(name = "unix.shell", description = "Execute unix shell command.")
public class UnixProcessRunnablePlugin extends AbstractRunnablePlugin {

	@PluginParameter(defaultValue = "sh", description = "Shell interpreter.")
	private String interpreter;

	@PluginParameter(description = "Command to execute. Will be ignored if source is defined.")
	private String command;

	@PluginParameter(description = "Execute a source script from repository.")
	private String source;
	
	public UnixProcessRunnablePlugin() {
		super();
	}

	@Override
	public Object execute() throws Exception {

		Process p = null;

		try {
	
			OutputFlow flow = this.getOutputFlow(this);
		
			File dummy = flow.getFile("dummy");
			this.getEnvironment().put("flowdir", dummy.getParentFile().getCanonicalPath());

			String commandFinal = null;
			if (this.source != null) {
				File sourceFile = new File(this.source);
				File destinationFile = flow.getFile(this.getInstanceID());
				
				TemplateRender.renderFile(sourceFile, destinationFile, this.getParametersValue());
				destinationFile.setExecutable(true, true);
				commandFinal = destinationFile.getAbsolutePath();

			} else {
				commandFinal = ParameterValueResolver.updateText(
						command, flow, this.getEnvironment(), this.getParametersValue(), null);
			}
			
			String[] shell = { interpreter, "-c", commandFinal };
			
			ProcessBuilder pb = new ProcessBuilder(shell);		
			pb.directory(dummy.getParentFile());

			for (String ee : this.getEnvironment().keySet()) {
				pb.environment().put(ee.toUpperCase(), this.getEnvironment().get(ee));
			}

			// Special case: Add Plugin path to the PATH
			if (pb.environment().containsKey("PATH")) {
				pb.environment().put("PATH",
						pb.environment().get("PATH") + File.pathSeparator + this.getEnvironment().get("PLUGIN_PATH")
								+ File.pathSeparator + this.getEnvironment().get("PLUGIN_PATH") + File.separator
								+ "bin");
			}

			this.logDebug("DUMP Environment");
			for (String ee : pb.environment().keySet()) {
				this.logDebug(" ENV " + ee + " = " + pb.environment().get(ee));
			}

			pb.redirectErrorStream(true);
			p = pb.start();

			long pid = getPidOfProcess(p);
			flow.addProperty(this.getInstanceID() + ".pid", pid);
			p.waitFor();
			
			flow.loadContent(p.getInputStream());
			
		} finally {
			
			if (p.isAlive()) {
				p.destroyForcibly();
			}
			p = null;
		}
		
		return null;
	}

	private static synchronized long getPidOfProcess(Process p) {
		long pid = -1;

		try {
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			} else if (p.getClass().getName().equals("java.lang.Win32Process")) {
				Field f = p.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			} else if (p.getClass().getName().equals("java.lang.ProcessImpl")) {
				Field f = p.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			}
		} catch (Exception e) {
			pid = -1;
		}
		return pid;
	}

}
package io.infolayer.aida.executor.runnable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;

import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.annotation.PluginParameter;
import io.infolayer.aida.exception.PluginException;
import io.infolayer.aida.executor.util.TemplateRender;
import io.infolayer.aida.plugin.OutputFlow;

@Plugin(name = "ps.shell", description = "Execute PowerShell command.")
public class PowerShellRunnablePlugin extends AbstractRunnablePlugin {

	@PluginParameter(description = "Command to execute. Will be ignored if source is defined.")
	private String command;

	@PluginParameter(description = "Execute a source script from repository.")
	private String source;

	public PowerShellRunnablePlugin() {
		super();
	}

	@Override
	public Object execute() throws Exception {

		if (this.command == null && this.source == null) {
			throw new PluginException("Cannot execute a Null command. Malformed plugin?");
		}

		OutputFlow flow = this.getOutputFlow(this);
		String commandFinal = null;

		if (this.source != null) {
			File sourceFile = new File(source);
			File destinationFile = flow.getFile(this.getInstanceID());

			TemplateRender.renderFile(sourceFile, destinationFile, this.getParametersValue());
			destinationFile.setExecutable(true, true);
			commandFinal = destinationFile.getAbsolutePath();

		} else {
			commandFinal = TemplateRender.renderText(command, this.getParametersValue());
		}

		PowerShell powerShell = PowerShell.openSession();

		File dummy = flow.getFile("dummy");

		// Increase timeout to give enough time to the script to finish
		Map<String, String> config = new HashMap<String, String>();
		config.put("maxWait", (this.getTimeout() * 1000) + "");
		config.put("tempFolder", dummy.getParentFile().getCanonicalPath());

		powerShell.configuration(config);

		PowerShellResponse response = null;

		if (source != null) {
			response = powerShell.executeScript(commandFinal.toString());
		} else {
			response = powerShell.executeCommand(commandFinal.toString());
		}

		if (response.isError()) {
			throw new PluginException(response.getCommandOutput());
		}

		if (response.isTimeout()) {
			throw new PluginException("Execution cancelled due timeout.");
		}

		flow.loadContent(new ByteArrayInputStream(response.getCommandOutput().getBytes()));

		powerShell.close();

		flow.addEnvironment("plugin.name", this.getPlugin().getName());

		return null;

	}

}
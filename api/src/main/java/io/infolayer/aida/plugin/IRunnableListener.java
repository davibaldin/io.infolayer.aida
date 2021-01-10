package io.infolayer.aida.plugin;

/**
 * Interface to be called during plugin execution lifecycle.
 * @author davi@infolayer.io
 *
 */
public interface IRunnableListener {

	public void listen(String message, int status, String instanceId, String pluginName, Object instance);

}

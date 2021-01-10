package io.infolayer.aida.exception;

public class PluginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -277453160722717191L;
	
	public PluginException(String message) {
		super(message);
	}

	public PluginException(Exception e) {
		super(e);
	}

}

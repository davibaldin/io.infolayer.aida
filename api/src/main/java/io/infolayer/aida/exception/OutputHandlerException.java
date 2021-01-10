package io.infolayer.aida.exception;

public class OutputHandlerException extends Exception {

	public OutputHandlerException(String message) {
		super(message);
	}

	public OutputHandlerException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = -7208930244293811869L;

}

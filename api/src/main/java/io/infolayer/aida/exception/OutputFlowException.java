package io.infolayer.aida.exception;

public class OutputFlowException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7320053541775457496L;
	
	public OutputFlowException(String message) {
		super(message);
	}
	
	public OutputFlowException(Exception e) {
		super(e);
	}

}

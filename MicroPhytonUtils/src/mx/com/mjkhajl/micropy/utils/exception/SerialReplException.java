package mx.com.mjkhajl.micropy.utils.exception;

public class SerialReplException extends Exception {

	private static final long	serialVersionUID	= 1L;
	private final String		module;
	private final String		command;

	public SerialReplException( String command, String module, String message ) {

		super( message );
		this.module = module;
		this.command = command;
	}

	/* @formatter: off */
	@Override
	public String toString() {

		return new StringBuilder( super.toString() )
				.append( "\n\tCommand: " ).append( command )
				.append( "\n\tModule:  " ).append( module )
				.append( "\n\tMessage: " ).append( this.getMessage() )
				.toString();
	}
	/* @formatter: on */
}

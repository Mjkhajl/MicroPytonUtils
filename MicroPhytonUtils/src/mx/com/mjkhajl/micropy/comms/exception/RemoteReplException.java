package mx.com.mjkhajl.micropy.comms.exception;

import java.io.IOException;

public class RemoteReplException extends IOException {

	private static final long	serialVersionUID	= 1L;
	private final String		module;
	private final String		command;

	public RemoteReplException( String command, String module, String message ) {

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

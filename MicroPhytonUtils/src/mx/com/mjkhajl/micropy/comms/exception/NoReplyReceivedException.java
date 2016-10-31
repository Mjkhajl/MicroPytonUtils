package mx.com.mjkhajl.micropy.comms.exception;

public class NoReplyReceivedException extends RemoteReplException {

	private static final long serialVersionUID = 1L;

	public NoReplyReceivedException( String command, String module, String message ) {
		super( command, module, message );
	}
}

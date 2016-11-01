package mx.com.mjkhajl.micropy.comms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.com.mjkhajl.micropy.comms.exception.NoReplyReceivedException;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;

public class SerialReplReader implements Runnable {

	public static final String		END_REPLY_S		= ">>> ";
	public static final String		CONT_COMMAND_S	= "... ";
	public static final String		CR_LF_S			= "\r\n";
	public static final byte[]		CR_LF_B			= CR_LF_S.getBytes();
	private static final Pattern	ERROR_PATTERN	= Pattern.compile( "\\n([A-Z][a-zA-Z0-9]+Error)[:]([^\\r^\\n]*)" );

	enum EndType {
		ENTER_NEW_COMMAND, CONTINUE_COMMAND
	}

	private Connection		conn;
	private StringBuilder	reply;
	private Object			monitor;
	private EndType			endType;

	public SerialReplReader( Connection conn, Object monitor ) {

		this.conn = conn;
		this.monitor = monitor;
		this.reply = new StringBuilder();
		this.endType = EndType.CONTINUE_COMMAND;
	}

	public String getReply() throws NoReplyReceivedException {

		if ( reply.length() == 0 )
			throw new NoReplyReceivedException( null, "No reply was received from REPL?!!", null );

		return reply.toString();
	}

	public EndType getEndType() {
		return endType;
	}

	@Override
	public synchronized void run() {

		try {

			int data;

			while ( ( data = conn.read() ) != -1 ) {

				reply.append( (char) data );

				if ( endStringMatches( reply ) )
					break;
			}

		} catch ( Exception e ) {

			throw new RuntimeException( e );

		} finally {

			synchronized ( monitor ) {

				// notify all monitoring objects waiting...
				monitor.notifyAll();
			}
		}

	}

	private boolean endStringMatches( StringBuilder reply ) {

		// check only if reply is more than 4 chars...
		if ( reply.length() >= 4 ) {

			// get the last 4 chars in the reply
			String tail = reply.substring( reply.length() - 4, reply.length() );

			if ( tail.equals( CONT_COMMAND_S ) ) {

				endType = EndType.CONTINUE_COMMAND;
				return true;
			}
			if ( tail.equals( END_REPLY_S ) ) {

				endType = EndType.ENTER_NEW_COMMAND;
				return true;
			}
		}
		return false;
	}

	private static void checkRemoteErrors( String reply ) throws RemoteReplException {

		// search errors in the reply
		Matcher matcher = ERROR_PATTERN.matcher( reply );

		if ( matcher.find() ) {

			// if found throw exception with received data...
			throw new RemoteReplException( reply, matcher.group( 1 ), matcher.group( 2 ) );
		}
	}

	public static String checkForErrorsAndReturn( String command, String reply ) throws RemoteReplException {

		checkRemoteErrors( reply );

		int crlfIndex = reply.indexOf( CR_LF_S );

		// discard the first line to the end of the CRLF
		int begin = ( crlfIndex != -1 ) ? ( crlfIndex + 2 ) : 0;
		// discard the tail (...\s Continue ) (>>>\s New command )
		int end = ( reply.length() > begin + 4 ) ? reply.length() - 4 : begin;

		reply = reply.substring( begin, end );

		System.out.println( ">" + reply );

		return reply;
	}
}

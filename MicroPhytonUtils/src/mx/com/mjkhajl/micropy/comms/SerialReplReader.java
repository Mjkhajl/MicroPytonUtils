package mx.com.mjkhajl.micropy.comms;

import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.com.mjkhajl.micropy.comms.exception.NoReplyReceivedException;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;

public class SerialReplReader implements Runnable {

	public static final String		END_REPLY_STR	= ">>> ";
	public static final byte[]		END_REPLY		= END_REPLY_STR.getBytes();
	public static final String		CONT_REPLY_STR	= "... ";
	public static final byte[]		CONT_REPLY		= CONT_REPLY_STR.getBytes();
	public static final String		END_COMMAND_STR	= "\r\n";
	public static final byte[]		END_COMMAND		= END_COMMAND_STR.getBytes();
	private static final Pattern	ERROR_PATTERN	= Pattern.compile( "\\n([A-Z][a-zA-Z0-9]+Error)[:]([^\\r^\\n]*)" );

	enum EndType {
		WAIT, CONTINUE
	}

	private InputStream		inStream;
	private StringBuilder	reply;
	private Object			monitor;
	private EndType			endType;

	public SerialReplReader( InputStream inStream, Object monitor ) {

		this.inStream = inStream;
		this.monitor = monitor;
		this.reply = new StringBuilder();
		this.endType = EndType.CONTINUE;
	}

	public String getReply() throws NoReplyReceivedException {

		if ( reply.length() == 0 )
			throw new NoReplyReceivedException( null, "No reply was received from REPL?!!", null );

		return String.valueOf( reply );
	}

	public EndType getEndType() {
		return endType;
	}

	@Override
	public synchronized void run() {

		try {

			int data;
			byte[] tail = new byte[4];

			while ( ( data = inStream.read() ) != -1 ) {

				reply.append( (char) data );

				if ( tailMatchesEnd( tail, data ) )
					break;
			}

		} catch ( Exception e ) {

			throw new RuntimeException( e );

		} finally {

			synchronized ( monitor ) {

				monitor.notifyAll();
			}
		}

	}

	private boolean tailMatchesEnd( byte[] tail, int data ) {

		System.arraycopy( tail, 1, tail, 0, END_REPLY.length - 1 );
		tail[END_REPLY.length - 1] = (byte) data;

		if ( Arrays.equals( tail, END_REPLY ) ) {

			endType = EndType.WAIT;
			return true;
		}
		if ( Arrays.equals( tail, CONT_REPLY ) ) {

			endType = EndType.CONTINUE;
			return true;
		}

		return false;
	}

	public static String checkForErrorsAndReturn( String command, String reply ) throws RemoteReplException {

		Matcher matcher = ERROR_PATTERN.matcher( reply );

		if ( matcher.find() ) {

			throw new RemoteReplException( command, matcher.group( 1 ), matcher.group( 2 ) );
		}

		int indexOfCR = reply.indexOf( END_COMMAND_STR ) + END_COMMAND_STR.length();

		if ( indexOfCR != -1 ) {

			reply = reply.substring( indexOfCR );
		}

		if ( reply.length() <= 4 ) {

			reply = "";

		} else {

			reply = reply.substring( 0, reply.length() - 4 );
		}

		System.out.println( ">" + reply );

		return reply;
	}
}

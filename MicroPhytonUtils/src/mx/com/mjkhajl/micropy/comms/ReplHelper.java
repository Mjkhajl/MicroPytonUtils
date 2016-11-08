package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;
import mx.com.mjkhajl.micropy.utils.Log;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;

public class ReplHelper implements Closeable {

	private static final String		CR_LF_S				= "\r\n";
	private static final Pattern	PATTERN_ERROR		= Pattern.compile( "^([^\n]+)\n([^\n]+\n)*([A-Z][a-zA-Z0-9]+Error)[:](.*)" );
	private static final byte[]		PATTERN_NEXT_B		= "\r\n>>> ".getBytes();
	private static final byte[]		PATTERN_CONTINUE_B	= "\r\n... ".getBytes();
	private static final int        READ_BUFFER_SIZE    = 256;
	private Connection				conn				= null;
	private final int				maxCommandSize;

	/**
	 * Creates a new REPL interface helper to send commands using the received
	 * connection, it connects to the first connection available
	 * 
	 * @param timeout
	 *            timeout to connect
	 * @param maxCommandSize
	 *            max length permitted for command length
	 * @param conn
	 *            connection to use to contact the remote REPL
	 * @throws IOException
	 */
	public ReplHelper( long timeout, int maxCommandSize, Connection conn ) throws IOException {

		this.conn = conn;
		this.maxCommandSize = maxCommandSize;

		conn.connectToFirstAvailable();
	}

	/**
	 * Same as sendCommand, but if an exception is raised during the command
	 * evaluation and/or execution it is ignored.
	 * 
	 * @param command
	 *            command to execute in REPL
	 * @return the reply from the REPL
	 */
	public String sendCommandIgnoreErrors( String command ) {

		String reply = new String();

		try {

			// send the command and ignore any error
			reply = sendCommand( command );

		} catch ( Exception e ) {

			Log.log( e, LogLevel.ERROR );
		}

		return reply;
	}

	/**
	 * Sends the received command through the Connection of this REPL interface
	 * and reads the reply, if command length is greater than maxCommandSize it
	 * is split into smaller chunks of the original command and sent to the REPL
	 * chunk by chunk.
	 * 
	 * @param command
	 *            command to execute in REPL
	 * @return the reply from the REPL
	 * @throws IOException
	 */
	public synchronized String sendCommand( String command ) throws IOException {

		// if the command string is too long the ESP8266 will hung... so we
		// split the string in chunks...
		List<String> chunks = CodeUtils.tokenizeCommand( command, maxCommandSize );
		String reply = new String();

		for ( String chunk : chunks ) {

			Log.log( "C:" + chunk, LogLevel.DEBUG );
			reply = sendCommandInternal( chunk );
			Log.log( "R:" + reply, LogLevel.DEBUG );
		}

		return reply;
	}

	private synchronized String sendCommandInternal( String command ) throws IOException {

		byte[] buffer = new byte[ READ_BUFFER_SIZE ];
		// write the command in the connection...
		conn.write( ( command + CR_LF_S ).getBytes() );
		int length = 0;

		do {
			int data = conn.read();
			if ( data != -1 ) {
				if ( length >= buffer.length ) {
					// grow the buffer if it is full....
					byte[] newBuffer = new byte[buffer.length + READ_BUFFER_SIZE];
					System.arraycopy( buffer, 0, newBuffer, 0, buffer.length );
					buffer = newBuffer;
				}
				buffer[length++] = (byte) data;
			}
		} while ( !replyMatchesEnd( buffer, length ) );

		String reply = new String( buffer );

		Log.log( reply, LogLevel.DEBUG );

		// reply matches error pattern...
		if ( reply.indexOf( "\r\nTraceback (" ) != -1 ) {

			// ...throw exception with received data
			throw parseException( reply );
		}

		int crlfIndex = reply.indexOf( CR_LF_S );
		// discard the first line to the end of the CRLF
		int begin = ( crlfIndex != -1 ) ? ( crlfIndex + 2 ) : 0;

		return reply.substring( begin );
	}

	private boolean replyMatchesEnd( byte[] buffer, int length ) {

		if ( length < 6 )
			return false;

		length = length - 6;
		for ( int i = 0; i < 6; i++ ) {

			if ( buffer[length + i] != PATTERN_NEXT_B[i] && buffer[length + i] != PATTERN_CONTINUE_B[i] )
				return false;
		}

		return true;
	}

	private RemoteReplException parseException( String reply ) {

		Log.log( reply, LogLevel.ERROR );

		Matcher matcher = PATTERN_ERROR.matcher( reply );

		matcher.find();
		return new RemoteReplException( matcher.group( 1 ), matcher.group( 3 ), matcher.group( 4 ) );
	}

	@Override
	public synchronized void close() throws IOException {

		CodeUtils.close( conn );
	}
}

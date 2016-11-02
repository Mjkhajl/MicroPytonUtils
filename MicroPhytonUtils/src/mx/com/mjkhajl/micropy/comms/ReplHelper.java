package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class ReplHelper implements Closeable {

	private static final String		CR_LF_S				= "\r\n";
	private static final byte[]		CR_LF_B				= CR_LF_S.getBytes();
	private static final Pattern	PATTERN_ERROR		= Pattern.compile( "^([^\n]+)\n([^\n]+\n)*([A-Z][a-zA-Z0-9]+Error)[:](.*)" );
	private static final Pattern	PATTERN_END_RPLY	= Pattern.compile( "([>]{3})|([\\.]{3})\\s$" );

	private Connection				conn				= null;
	private final int				maxCommandSize;

	public ReplHelper( long timeout, int maxCommandSize, Connection conn ) throws IOException {

		this.conn = conn;
		this.maxCommandSize = maxCommandSize;

		conn.connectToFirstAvailable();
	}

	public synchronized String sendCommand( String command ) throws IOException {

		// if the command string is too long the ESP8266 will hung... so we
		// split the string in chunks...
		List<String> chunks = CodeUtils.tokenizeCommand( command, maxCommandSize );
		String reply = new String();

		for ( String chunk : chunks ) {

			System.out.println( ">>>" + chunk );
			reply = sendCommandInternal( chunk );
			System.out.println( ">" + reply );
		}

		return reply;
	}

	private synchronized String sendCommandInternal( String command ) throws IOException {

		StringBuilder reply = new StringBuilder();

		// write the command in the connection...
		conn.write( command.getBytes() );
		conn.write( CR_LF_B );

		do {

			// read data and append to reply until...
			reply.append( (char) conn.read() );

		} while ( !PATTERN_END_RPLY.matcher( reply ).find() );

		Matcher matcher = PATTERN_ERROR.matcher( reply );

		// reply matches error pattern...
		if ( matcher.find() ) {

			// ...throw exception with received data
			throw new RemoteReplException( matcher.group( 1 ), matcher.group( 3 ), matcher.group( 4 ) );
		}

		int crlfIndex = reply.indexOf( CR_LF_S );
		// discard the first line to the end of the CRLF
		int begin = ( crlfIndex != -1 ) ? ( crlfIndex + 2 ) : 0;
		// discard the tail (...\s Continue ) (>>>\s New command )
		int end = ( reply.length() > begin + 4 ) ? reply.length() - 4 : begin;

		return reply.substring( begin, end );
	}

	public String sendCommandIgnoreErrors( String command ) {

		String reply = new String();

		try {

			// send the command and ignore any error
			reply = sendCommand( command );

		} catch ( Exception e ) {

			e.printStackTrace();
		}

		return reply;
	}

	@Override
	public synchronized void close() throws IOException {

		CodeUtils.close( conn );
	}
}

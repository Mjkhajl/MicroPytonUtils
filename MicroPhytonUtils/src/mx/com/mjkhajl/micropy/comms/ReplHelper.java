package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class ReplHelper implements Closeable {

	private Connection	conn	= null;
	private final long	timeout;
	private final int	maxCommandSize;

	public ReplHelper( long timeout, int maxCommandSize, Connection conn ) throws IOException {

		this.conn = conn;
		this.maxCommandSize = maxCommandSize;
		this.timeout = timeout;
	}

	public synchronized String sendCommand( String command ) throws IOException {

		System.out.println( ">>>" );

		// if the command string is too long the ESP8266 will hung... so we
		// split the string in chunks...
		List<String> chunks = CodeUtils.tokenizeCommand( command, maxCommandSize );
		String reply = new String();

		for ( String chunk : chunks ) {

			System.out.println( chunk );
			reply = sendCommandChunk( chunk );
		}

		return reply;
	}

	private synchronized String sendCommandChunk( String command ) throws IOException {

		SerialReplReader reader = new SerialReplReader( conn, this );

		try {

			// start the reader before so it can read while command is
			// written...
			new Thread( reader ).start();

			conn.write( command.getBytes() );
			conn.write( SerialReplReader.CR_LF_B );

			// wait for reader to notify or time to run out
			this.wait( timeout );

		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}

		return SerialReplReader.checkForErrorsAndReturn( command, reader.getReply() );
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

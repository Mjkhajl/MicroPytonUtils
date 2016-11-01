package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import mx.com.mjkhajl.micropy.comms.exception.NoReplyReceivedException;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

@SuppressWarnings( "unchecked" )
public class SerialReplHelper implements Closeable {

	public static final String		CONT_REPLY_STR	= "... ";
	public static final byte[]		CONT_REPLY		= CONT_REPLY_STR.getBytes();
	public static final String		END_COMMAND_STR	= "\r\n";
	public static final byte[]		END_COMMAND		= END_COMMAND_STR.getBytes();
	public static final String		END_REPLY_STR	= ">>> ";
	public static final byte[]		END_REPLY		= END_REPLY_STR.getBytes();
	private static final Pattern	ERROR_PATTERN	= Pattern.compile( "\\n([A-Z][a-zA-Z0-9]+Error)[:]([^\\r^\\n]*)" );
	private SerialPort				port			= null;
	private boolean					connected		= false;
	private InputStream				inStream		= null;
	private OutputStream			outStream		= null;
	private final int				freq, dataBits, stopBits, parity, maxCommandSize;
	private final long				timeout;

	public SerialReplHelper( long timeout, int freq, int dataBits, int stopBits, int parity, int maxCommandSize ) {

		this.freq = freq;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.timeout = timeout;
		this.maxCommandSize = maxCommandSize;
	}

	public synchronized String connectToFirstAvailable()
			throws PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {

		if ( connected )
			throw new IllegalStateException( "Already connected!!" );

		Enumeration<CommPortIdentifier> portIds = CommPortIdentifier.getPortIdentifiers();

		while ( portIds.hasMoreElements() ) {

			CommPortIdentifier portId = portIds.nextElement();

			if ( portId.getPortType() == CommPortIdentifier.PORT_SERIAL ) {

				port = (SerialPort) portId.open( "SerialAppToCOMM", (int) timeout );
				port.setSerialPortParams( freq, dataBits, stopBits, parity );

				outStream = port.getOutputStream();
				inStream = port.getInputStream();

				connected = true;
				return portId.getName();
			}
		}

		throw new IOException( "No serial port available!!" );
	}

	public synchronized String sendCommand( String command ) throws IOException {

		System.out.println( ">>>" );
		
		List<String> chunks = CodeUtils.tokenizeCommand( command, maxCommandSize );
		String reply = "";
		
		for ( String chunk : chunks ) {
			
			System.out.println( chunk );
			reply = sendCommandChunk( chunk );
		}
		
		return reply;
	}

	private synchronized String sendCommandChunk( String command ) throws IOException {

		SerialReader reader = new SerialReader( inStream, this );

		outStream.write( command.getBytes() );
		outStream.write( END_COMMAND );
		outStream.flush();

		try {
			// read inStream for reply
			new Thread( reader ).start();

			// wait for reader to notify or time to run out
			this.wait( timeout );

		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}

		return checkForErrorsAndReturn( command, reader.getReply() );
	}

	public String sendCommandIgnoreErrors( String command ) {

		String result = "";

		try {

			result = sendCommand( command );

		} catch ( NoReplyReceivedException e ) {
		} catch ( Throwable e ) {

			e.printStackTrace();
		}

		return result;
	}

	private String checkForErrorsAndReturn( String command, String reply ) throws RemoteReplException {

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

	@Override
	public synchronized void close() throws IOException {

		if ( connected ) {

			CodeUtils.close( outStream, inStream );

			port.close();
			connected = false;
		}
	}

	public static class SerialReader implements Runnable {

		enum EndType {
			WAIT, CONTINUE
		}

		private InputStream		inStream;
		private StringBuilder	reply;
		private Object			monitor;
		private EndType			endType;

		public SerialReader( InputStream inStream, Object monitor ) {

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
	}
}

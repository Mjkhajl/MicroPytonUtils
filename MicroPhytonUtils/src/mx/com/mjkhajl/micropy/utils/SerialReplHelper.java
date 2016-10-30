package mx.com.mjkhajl.micropy.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import mx.com.mjkhajl.micropy.utils.exception.SerialReplException;

@SuppressWarnings( "unchecked" )
public class SerialReplHelper implements Closeable {

	public static final byte[]		ENDCOMMAND		= "\r\n".getBytes();
	private static final byte[]		REPL_END_BYTES	= { 62, 62, 62, 32 };
	private static final Pattern	ERROR_PATTERN	= Pattern.compile( "\\n([A-Z][a-zA-Z0-9]+Error)[:]([^\\r^\\n]*)" );
	private SerialPort				port			= null;
	private boolean					connected		= false;
	private InputStream				inStream		= null;
	private OutputStream			outStream		= null;
	private final int				freq, dataBits, stopBits, parity;
	private final long				timeout, lazyTime;

	public SerialReplHelper( long timeout, int freq, int dataBits, int stopBits, int parity, long lazyTime ) {

		this.freq = freq;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.timeout = timeout;
		this.lazyTime = lazyTime;
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

		throw new RuntimeException( "No serial port available!!" );
	}

	public synchronized String sendCommand( String command ) throws IOException, InterruptedException, SerialReplException {

		System.out.println( ">" + command );

		if ( !connected )
			throw new IllegalStateException( "Not Connected!" );

		outStream.write( command.getBytes() );
		outStream.write( ENDCOMMAND );
		outStream.flush();

		SerialReader reader = new SerialReader( inStream, this );

		// read inStream for reply
		new Thread( reader ).start();

		// wait until notified by reader...
		this.wait( timeout );

		// gimme a break I'm just a lazy NodeMCU board...
		Thread.sleep( lazyTime );

		return checkForErrorsAndReturn( command, reader.getReply() );
	}

	public String sendCommandIgnoreErrors( String command ) {

		String result = "";

		try {

			result = sendCommand( command );

		} catch ( Throwable e ) {

			e.printStackTrace();
		}

		return result;
	}

	private String checkForErrorsAndReturn( String command, String reply ) throws SerialReplException {

		Matcher matcher = ERROR_PATTERN.matcher( reply );

		if ( matcher.find() )
			throw new SerialReplException( command, matcher.group( 1 ), matcher.group( 2 ) );

		reply = reply.substring( command.length() + 2 );

		if ( reply.length() <= 4 ) {
			reply = "";
		} else {
			reply = reply.substring( 0, reply.length() - 6 );
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
		private InputStream		inStream;
		private StringBuilder	reply;
		private Object			monitor;

		public SerialReader( InputStream inStream, Object monitor ) {

			this.inStream = inStream;
			this.monitor = monitor;
			this.reply = new StringBuilder();
		}

		public String getReply() {

			if ( reply.length() == 0 )
				throw new IllegalStateException( "No reply was received from REPL?!!" );

			return String.valueOf( reply );
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

			System.arraycopy( tail, 1, tail, 0, REPL_END_BYTES.length - 1 );
			tail[REPL_END_BYTES.length - 1] = (byte) data;

			return Arrays.equals( tail, REPL_END_BYTES );
		}
	}
}

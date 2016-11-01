package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import mx.com.mjkhajl.micropy.comms.exception.NoReplyReceivedException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

@SuppressWarnings( "unchecked" )
public class SerialReplHelper implements Closeable {

	private SerialPort		port		= null;
	private boolean			connected	= false;
	private InputStream		inStream	= null;
	private OutputStream	outStream	= null;
	private final int		freq, dataBits, stopBits, parity, maxCommandSize;
	private final long		timeout;

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

		SerialReplReader reader = new SerialReplReader( inStream, this );

		outStream.write( command.getBytes() );
		outStream.write( SerialReplReader.END_COMMAND );
		outStream.flush();

		try {
			// read inStream for reply
			new Thread( reader ).start();

			// wait for reader to notify or time to run out
			this.wait( timeout );

		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}

		return SerialReplReader.checkForErrorsAndReturn( command, reader.getReply() );
	}

	public String sendCommandIgnoreErrors( String command ) {

		try {

			return sendCommand( command );

		} catch ( NoReplyReceivedException e ) {
		} catch ( Throwable e ) {

			e.printStackTrace();
		}

		return "";
	}

	@Override
	public synchronized void close() throws IOException {

		if ( connected ) {

			CodeUtils.close( outStream, inStream );

			port.close();
			connected = false;
		}
	}
}

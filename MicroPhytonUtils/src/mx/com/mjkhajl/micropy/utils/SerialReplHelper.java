package mx.com.mjkhajl.micropy.utils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

@SuppressWarnings("unchecked")
public class SerialReplHelper implements Closeable{

	public static final byte[] ENDCOMMAND = "\r\n".getBytes();
	private static final byte[] REPL_END_BYTES = { 62, 62, 62, 32 };
	private SerialPort port = null;
	private boolean connected = false;
	private InputStream inStream = null;
	private OutputStream outStream = null;
	private final int freq, dataBits, stopBits, parity, timeout;

	public SerialReplHelper(int timeout, int freq, int dataBits, int stopBits, int parity) {

		this.freq = freq;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		this.timeout = timeout;
	}

	public synchronized String connectToFirstAvailable()
			throws PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {

		if (connected)
			throw new IllegalStateException("Already connected!!");

		Enumeration<CommPortIdentifier> portIds = CommPortIdentifier.getPortIdentifiers();

		while (portIds.hasMoreElements()) {

			CommPortIdentifier portId = portIds.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

				port = (SerialPort) portId.open("SerialAppToCOMM", 500);
				port.setSerialPortParams(freq, dataBits, stopBits, parity);

				outStream = port.getOutputStream();
				inStream = port.getInputStream();

				connected = true;
				return portId.getName();
			}
		}

		throw new RuntimeException("No serial port available!!");
	}

	public synchronized String sendCommand(String command) throws IOException, InterruptedException {
		
		if (!connected)
			throw new IllegalStateException("Not Connected!");

		outStream.write(command.getBytes());
		outStream.write(ENDCOMMAND);
		outStream.flush();

		SerialReader reader = new SerialReader(inStream);
		
		// read inStream for reply
		new Thread( reader ).start();

		// wait until notified by reader...
		synchronized( reader ){
			
			reader.wait(timeout);
		}

		System.out.println( reader.getReply() );
		
		// give it a rest...
		Thread.sleep( 300 );
		
		return reader.getReply();
	}

	@Override
	public synchronized void close() {

		if (connected) {

			try {

				outStream.close();
				inStream.close();
				port.close();
				connected = false;

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public static class SerialReader implements Runnable {
		private InputStream inStream;
		private String reply;

		public SerialReader( InputStream inStream ) {
			
			this.inStream = inStream;
		}

		public String getReply() {
			
			return reply.toString();
		}

		@Override
		public synchronized void run() {

			try {
				
				StringBuilder reply = new StringBuilder();
				int data;
				byte[] tail = new byte[4];
				
				while ( ( data = inStream.read() ) != -1 ) {

					reply.append( (char) data );

					if ( tailMatchesEnd( tail, data ) ) break;
				}
				
				this.reply = reply.toString();
				
			} catch (Exception e) {

				throw new RuntimeException(e);
				
			} finally {
			
				this.notifyAll();
			}
			
		}
		
		private boolean tailMatchesEnd( byte[] tail, int data ){
			System.arraycopy(tail, 1, tail, 0, REPL_END_BYTES.length - 1);
			tail[REPL_END_BYTES.length-1] = (byte) data;
			
			return Arrays.equals( tail, REPL_END_BYTES);
		}
	}
	
	public static void main(String[] args) throws Throwable {

		SerialReplHelper helper = new SerialReplHelper( 1, 115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		System.out.println("connected to " + helper.connectToFirstAvailable());

		System.out.println( helper.sendCommand("import os") );

		System.out.println( helper.sendCommand("os.listdir()") );

		helper.close();

		System.out.println("end");
	}
}

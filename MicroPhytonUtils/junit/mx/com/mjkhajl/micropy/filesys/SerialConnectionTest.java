package mx.com.mjkhajl.micropy.filesys;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.SerialCommConnection;
import mx.com.mjkhajl.micropy.utils.Log;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;

public class SerialConnectionTest {

	private SerialCommConnection conn;

	@Before
	public void setUp() {
		final int bpsSpeed = 115200;
		final int dataBits = 8;
		final int stopBits = 1;
		final int parity = 0; // none see @javax.comm.SerialPort
		final int timeout = 5000;

		conn = new SerialCommConnection( bpsSpeed, dataBits, stopBits, parity, timeout );
		Log.GL_LOG_LEVEL = LogLevel.DEBUG;
	}

	@Test
	public void testSpeed() throws Exception {

		// Calendar cal = Calendar.getInstance();
		Log.log( "start" );
		conn.connectToFirstAvailable();
		Log.log( "start reader" );
		new Thread( new ReadWorker() ).start();
		Log.log( "---send---" );
		conn.write( "a\r\n".getBytes() );
		/*
		 * int dat = 0; while ( ( dat = conn.read() ) != -1 ) { Log.log( (char)
		 * dat ); }
		 */
		Thread.sleep( 30 );
		Log.log( "end" );
	}

	class ReadWorker implements Runnable {

		@Override
		public void run() {

			try {
				int data;
				while ( true ) {
					data = conn.read();
					if ( data != -1 )
						Log.log( (char) data );
				}
			} catch ( IOException e ) {

				Log.log( e );
			}
		}
	}
}

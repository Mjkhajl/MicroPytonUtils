package mx.com.mjkhajl.micropy.filesys;

import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.SerialCommConnection;
import mx.com.mjkhajl.micropy.utils.Log;

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
	}

	@Test
	public void testSpeed() throws Exception {
		Log.log( "start" );
		conn.connectToFirstAvailable();
		Log.log( "send" );
		conn.write( "a\r\n".getBytes() );
		Log.log( "rec" );
		int dat = 0;
		while ( ( dat = conn.read() ) != -1 ) {
			Log.log( (char) dat );
		}
		Log.log( "end" );
	}
}

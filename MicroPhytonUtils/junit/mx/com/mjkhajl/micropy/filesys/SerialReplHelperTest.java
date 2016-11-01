package mx.com.mjkhajl.micropy.filesys;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.Connection;
import mx.com.mjkhajl.micropy.comms.ReplHelper;
import mx.com.mjkhajl.micropy.comms.SerialCommConnection;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class SerialReplHelperTest {

	private ReplHelper repl;

	@Before
	public void setUp() throws Throwable {

		final int bpsSpeed = 115200;
		final int dataBits = 8;
		final int stopBits = 1;
		final int parity = 0; // none see @javax.comm.SerialPort
		final int timeout = 5000;
		final int maxReplLineSize = 300;

		Connection conn = new SerialCommConnection( bpsSpeed, dataBits, stopBits, parity, timeout );

		conn.connectToFirstAvailable();

		repl = new ReplHelper( timeout, maxReplLineSize, conn );
	}

	@Test
	public void serialRepl() throws Throwable {

		repl.sendCommand( "import os" );

		repl.sendCommand( "os.listdir()" );
	}

	@Test( expected = RemoteReplException.class )
	public void errorResult() throws Throwable {

		repl.sendCommand( "asnos señor de tu rebaño" );
	}

	@Test
	public void sendBigCommand() throws Throwable {

		repl.sendCommand(
				"b = bytes([116,95,115,46,115,101,110,100,40,98,121,116,101,115,40,67,79,78,84,69,78,84,46,102,111,114,109,97,116,40,99,111,117,110,116,101,114,41,44,32,34,97,115,99,105,105,34,41,41,13,10,32,32,32,32,32,32,32,32,32,32,32,32,99,108,105,101,110,116,95,115,46,99,108,111,115,99,108,105,101,110,116,95,115,46,99,108,111,115,99,108,105,101,110,116,95,115,46,99,108,111,115,99,108,105,101,110,116,95,115,46,99,108,111,115,99,108,105,101,110,116,95,115,46,99,108,111,115,99,108,105,101,110,116,95,115,46,99,108,111,115,101,40,41,13,10,32,32,32,32,32,32,32,32,32,32,32,32,112,97,114])" );

		repl.sendCommand( "b" );

		repl.sendCommand( "del b" );
		repl.sendCommand( "gc.collect()" );
	}

	@After
	public void after() throws Throwable {

		CodeUtils.close( repl );
	}

}

package mx.com.mjkhajl.micropy.filesys;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class SerialReplHelperTest {

	private SerialReplHelper repl;

	@Before
	public void setUp() throws Throwable {
		
		/* @formatter:off 
		 *  TIMEOUT: 200, 
		 *  SPEED: 115200 bps, 
		 *  DATA BITS: 8, 
		 *  STOP BITS: 1, 
		 *  PARITY: NONE (0)
		 *  MAX_COMMAND_SIZE: 300
		 * @formatter:on */
		repl = new SerialReplHelper( 2000, 115200, 8, 1, 0, 300 );

		System.out.println( "connected to " + repl.connectToFirstAvailable() );
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

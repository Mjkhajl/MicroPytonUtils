package mx.com.mjkhajl.micropy.filesys;

import javax.comm.SerialPort;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.comms.exception.SerialReplException;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class SerialReplHelperTest {

	private SerialReplHelper repl;

	@Before
	public void setUp() throws Throwable {

		repl = new SerialReplHelper( 1000, 115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, 0 );

		System.out.println( "connected to " + repl.connectToFirstAvailable() );
	}

	@Test
	public void serialRepl() throws Throwable {

		repl.sendCommand( "import os" );

		repl.sendCommand( "os.listdir()" );
	}

	@Test( expected = SerialReplException.class )
	public void errorResult() throws Throwable {

		repl.sendCommand( "asnos señor de tu rebaño" );
	}

	@After
	public void after() throws Throwable {

		CodeUtils.close( repl );
	}

}

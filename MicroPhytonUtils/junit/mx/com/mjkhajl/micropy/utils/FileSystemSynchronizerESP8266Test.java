package mx.com.mjkhajl.micropy.utils;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileSystemSynchronizerESP8266Test {
	
	FileSystemSynchronizerESP8266 sync;
	
	private static final File TEST_DIR_ROOT = new File("C:/Users/Luis Miguel/git/MicroPytonUtils/MicroPhytonUtils/web-server");
	
	@Before
	public void setUp() throws Throwable {
		
		sync = new FileSystemSynchronizerESP8266();
	}
	
	@Test
	public void synchronizeFs() throws Exception{
		
		sync.synchronizeFs( TEST_DIR_ROOT, "/web-server");
	}
	
	@Test
	public void writeFile() throws Exception{
		
		sync.writeFile( new File(TEST_DIR_ROOT,"web-server.py"), "/web-server/web-server.py");
	}
	
	@After
	public void end() throws Throwable {
		
		sync.close();
	}
	
}

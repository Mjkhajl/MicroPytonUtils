package mx.com.mjkhajl.micropy.filesys;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.filesys.impl.ESP8266FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.impl.FileSystemSynchronizerImpl;
import mx.com.mjkhajl.micropy.filesys.impl.LocalFileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class FileSystemSynchronizerESP8266Test {

	FileSystemSynchronizer		sync;
	LocalFileSystemInterface	localFs;
	ESP8266FileSystemInterface	remoteFs;
	
	private static final File	TEST_DIR_RO0T	= new File( "C:/Users/Luis Miguel/git/MicroPytonUtils/MicroPhytonUtils" );
	private static final File	TEST_DIR_SYNC	= new File( TEST_DIR_RO0T, "webserver" );

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
		SerialReplHelper repl = new SerialReplHelper( 5000, 115200, 8, 1, 0, 300 );

		remoteFs = new ESP8266FileSystemInterface( repl, 256 );
		localFs = new LocalFileSystemInterface();

		sync = new FileSystemSynchronizerImpl( localFs, remoteFs );
	}

	@Test
	public void synchronizeDir() throws Exception {

		FileItem src = new FileItem( TEST_DIR_SYNC.getCanonicalPath(), Nature.LOCAL );
		FileItem dest = new FileItem( "/webserver", Nature.REMOTE );

		sync.synchronizeDir( src, dest );
	}

	@Test
	public void uploadFile() throws Exception {

		FileItem src = new FileItem( new File( TEST_DIR_SYNC, "server.py" ).getCanonicalPath(), FileItem.Nature.LOCAL );
		FileItem dest = new FileItem( "/webserver/server.py", FileItem.Nature.REMOTE );

		sync.copyFile( src, dest );
	}

	@Test
	public void downloadFile() throws Exception {

		FileItem src = new FileItem( "/webserver/server.py", FileItem.Nature.REMOTE );
		FileItem dest = new FileItem( new File( new File( TEST_DIR_RO0T, "down"), "server.py" ).getCanonicalPath(), FileItem.Nature.LOCAL );

		sync.copyFile( src, dest );
	}

	@Test
	public void equals() throws Exception {

		FileItem remote = new FileItem( "/webserver/server.py", FileItem.Nature.REMOTE );
		FileItem local = new FileItem( new File( TEST_DIR_SYNC, "server.py" ).getCanonicalPath(), FileItem.Nature.LOCAL );

		if ( !sync.equals( remote, local ) )
			throw new IllegalStateException( "files are not equal..." );
	}

	@After
	public void end() throws Throwable {

		CodeUtils.close( sync );
	}

}

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

	private static final File	TEST_DIR_ROOT	= new File( "C:/Users/Luis Miguel/git/MicroPytonUtils/MicroPhytonUtils/web-server" );

	@Before
	public void setUp() throws Throwable {

		/* @formatter:off
		 * 
		 *  TIMEOUT: 200, 
		 *  SPEED: 115200 BPS, 
		 *  DATA BITS: 8, 
		 *  STOP BITS: 1, 
		 *  PARITY: NONE (0),
		 *  LAZYTIME: 10
		 *  
		 * @formatter:on	
		 */
		SerialReplHelper repl = new SerialReplHelper( 2000, 115200, 8, 1, 0, 50 );

		remoteFs = new ESP8266FileSystemInterface( repl, 96 );
		localFs = new LocalFileSystemInterface();

		sync = new FileSystemSynchronizerImpl( localFs, remoteFs );
	}

	@Test
	public void synchronizeFs() throws Exception {

		FileItem src = new FileItem( TEST_DIR_ROOT.getCanonicalPath(), Nature.LOCAL );
		FileItem dest = new FileItem( "/web-server", Nature.REMOTE );

		sync.synchronizeDir( src, dest );
	}

	@Test
	public void uploadFile() throws Exception {

		FileItem src = new FileItem( new File( TEST_DIR_ROOT, "web-server.py" ).getCanonicalPath(), FileItem.Nature.LOCAL );
		FileItem dest = new FileItem( "/web-server/web-server.py", FileItem.Nature.REMOTE );

		sync.copyFile( src, dest );
	}

	@Test
	public void downloadFile() throws Exception {

		FileItem src = new FileItem( "/web-server/web-server.py", FileItem.Nature.REMOTE );
		FileItem dest = new FileItem( new File( TEST_DIR_ROOT, "web-server-down.py" ).getCanonicalPath(), FileItem.Nature.LOCAL );

		sync.copyFile( src, dest );
	}

	@Test
	public void equals() throws Exception {

		FileItem remote = new FileItem( "/web-server/img.jpg", FileItem.Nature.REMOTE );
		FileItem local = new FileItem( new File( TEST_DIR_ROOT, "img.jpg" ).getCanonicalPath(), FileItem.Nature.LOCAL );

		// upload file
		sync.copyFile( local, remote );

		if ( !sync.equals( remote, local ) )
			throw new IllegalStateException( "files are not equal..." );
	}

	@After
	public void end() throws Throwable {

		CodeUtils.close( sync );
	}

}

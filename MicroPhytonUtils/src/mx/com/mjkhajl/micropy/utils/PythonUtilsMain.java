package mx.com.mjkhajl.micropy.utils;

import java.io.IOException;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.filesys.FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.impl.ESP8266FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.impl.FileSystemSynchronizerImpl;
import mx.com.mjkhajl.micropy.filesys.impl.LocalFileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandLineUtils;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandlineMethod;

public class PythonUtilsMain {

	private FileSystemSynchronizer sync;

	public static void main( String[] args ) throws Exception {

		PythonUtilsMain main = new PythonUtilsMain();

		if ( args.length > 0 ) {

			CommandLineUtils.executeCommand( main, args );
			return;
		}

		throw new IllegalArgumentException( "No arguments received, use help for details on how to use this tool" );
	}

	@CommandlineMethod( description = "Synchronizes a local dir into a remote dir in the ESP8266, ESP should be connected via USB", usage = " synchronize <localdir> <remotedir>", argNames = { "localdir", "remotedir" }, argDescriptions = {
			"local dir in the file system that will be used as source, can be a relative path", "remote dir in the ESP8266 that will be updated according to the local dir provided, should be an absolute path" } )
	public void synchronize( String args[] ) {

		if ( args.length >= 3 ) {

			try {

				/* @formatter:off 
				 *  TIMEOUT: 200, 
				 *  SPEED: 115200 bps, 
				 *  DATA BITS: 8, 
				 *  STOP BITS: 1, 
				 *  PARITY: NONE (0)
				 *  MAX_COMMAND_SIZE: 300
				 * @formatter:on */
				SerialReplHelper repl = new SerialReplHelper( 5000, 115200, 8, 1, 0, 300 );

				FileSystemInterface remoteFs = new ESP8266FileSystemInterface( repl, 256 );
				FileSystemInterface localFs = new LocalFileSystemInterface();

				sync = new FileSystemSynchronizerImpl( localFs, remoteFs );

				FileItem srcFile = new FileItem( args[1], Nature.LOCAL );
				FileItem desFile = new FileItem( args[2], Nature.REMOTE );

				sync.synchronizeDir( srcFile, desFile );

				System.out.println( "Synchronization success!!!" );

				return;
			} catch ( IOException e ) {

				e.printStackTrace();
			} catch ( Exception e ) {

				e.printStackTrace();
			}
		}

		CommandLineUtils.printCommandHelp( this, args[0], System.out );

		throw new IllegalArgumentException( "usage: -sync <src-local-path> <dest-remote-path>" );
	}

	@CommandlineMethod( description = "Prints this help", usage = " help " )
	public void help( String args[] ) {

		CommandLineUtils.printObjectHelp( this, System.out );
	}
}

package mx.com.mjkhajl.micropy.utils;

import java.io.IOException;

import mx.com.mjkhajl.micropy.comms.ReplHelper;
import mx.com.mjkhajl.micropy.comms.SerialCommConnection;
import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.impl.ESP8266FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.impl.FileSystemSynchronizerImpl;
import mx.com.mjkhajl.micropy.filesys.impl.LocalFileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandLineUtils;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandlineMethod;

public class PythonUtilsMain {

	public static void main( String[] args ) throws Exception {

		PythonUtilsMain main = new PythonUtilsMain();

		if ( args.length > 0 ) {

			CommandLineUtils.executeCommand( main, args );
			return;
		}

		throw new IllegalArgumentException( "No arguments received, use help for details on how to use this tool" );
	}

	/* @formatter:off */
	@CommandlineMethod( 
			description = "Synchronizes a local dir into a remote dir in the ESP8266, ESP should be connected via USB", 
			usage       = " synchronize <localdir> <remotedir>", 
			argNames    = { 
					"localdir", 
					"remotedir" }, 
			argDescriptions = {
					"local dir in the file system that will be used as source, can be a relative path", 
					"remote dir in the ESP8266 that will be updated according to the local dir provided, should be an absolute path" } )
	/* @formatter:on */
	public void synchronize( String args[] ) {

		if ( args.length >= 3 ) {

			FileSystemSynchronizer sync = null;

			try {

				sync = buildSynchronizer();

				sync.synchronizeDir( new FileItem( args[1], Nature.LOCAL ), new FileItem( args[2], Nature.REMOTE ) );

				System.out.println( "Synchronization success!!!" );

				return;
			} catch ( Exception e ) {

				e.printStackTrace();
			} finally {

				CodeUtils.close( sync );
			}
		}

		CommandLineUtils.printCommandHelp( this, args[0], System.out );

		throw new IllegalArgumentException( "usage: -sync <src-local-path> <dest-remote-path>" );
	}

	private FileSystemSynchronizer buildSynchronizer() throws IOException, Exception {

		final int bpsSpeed = 115200;
		final int dataBits = 8;
		final int stopBits = 1;
		final int parity = 0; // none see @javax.comm.SerialPort
		final int timeout = 5000;
		final int maxReplLineSize = 300;
		final int maxFileChunk = 256;

		return new FileSystemSynchronizerImpl(
				new LocalFileSystemInterface(),
				new ESP8266FileSystemInterface(
						new ReplHelper(
								timeout,
								maxReplLineSize,
								new SerialCommConnection( bpsSpeed, dataBits, stopBits, parity, timeout ) ),
						maxFileChunk ) );
	}

	@CommandlineMethod( description = "Prints this help", usage = " help " )
	public void help( String args[] ) {

		CommandLineUtils.printObjectHelp( this, System.out );
	}
}

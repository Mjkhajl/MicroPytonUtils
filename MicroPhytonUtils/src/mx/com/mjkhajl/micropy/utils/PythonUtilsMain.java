package mx.com.mjkhajl.micropy.utils;

import java.io.IOException;

import mx.com.mjkhajl.micropy.comms.Connection;
import mx.com.mjkhajl.micropy.comms.ReplHelper;
import mx.com.mjkhajl.micropy.comms.ReplJavaCommandConsole;
import mx.com.mjkhajl.micropy.comms.SerialCommConnection;
import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.impl.ESP8266FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.impl.FileSystemSynchronizerImpl;
import mx.com.mjkhajl.micropy.filesys.impl.LocalFileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandLineUtils;
import mx.com.mjkhajl.micropy.utils.cmddoc.CommandlineMethod;

public class PythonUtilsMain {

	public static void main( String[] args ) throws Exception {

		Log.setLogLevelFromArgs( args );

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

				sync = buildSynchronizer( buildRepl( buildConection() ) );

				sync.synchronizeDir( new FileItem( args[1], Nature.LOCAL ), new FileItem( args[2], Nature.REMOTE ) );

				Log.log( "Synchronization success!!!", LogLevel.INFO );

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

	/* @formatter:off */
	@CommandlineMethod( 
			description = 
				"Starts a REPL console with special java commands to synchronizes a local dir into a remote dir in the ESP8266, " +
				"ESP should be connected via USB; Special command 'java sync' to execute file synchronization inside the console", 
			usage       = " startSyncConsole <localdir> <remotedir>", 
			argNames    = { 
					"localdir", 
					"remotedir" }, 
			argDescriptions = {
					"local dir in the file system that will be used as source, can be a relative path", 
					"remote dir in the ESP8266 that will be updated according to the local dir provided, should be an absolute path" } )
	/* @formatter:on */
	public void startSyncConsole( String args[] ) {

		if ( args.length >= 3 ) {

			FileSystemSynchronizer sync = null;
			Connection conn = null;

			try {

				conn = buildConection();
				sync = buildSynchronizer( buildRepl( conn ) );

				ReplJavaCommandConsole console = new ReplJavaCommandConsole( conn, sync );

				console.start( new FileItem( args[1], Nature.LOCAL ), new FileItem( args[2], Nature.REMOTE ) );
				return;
			} catch ( Exception e ) {

				e.printStackTrace();
			} finally {

				CodeUtils.close( sync, conn );
			}
		}

		CommandLineUtils.printCommandHelp( this, args[0], System.out );

		throw new IllegalArgumentException( "usage: -sync <src-local-path> <dest-remote-path>" );
	}

	private SerialCommConnection buildConection() throws IOException, Exception {

		final int bpsSpeed = 115200;
		final int dataBits = 8;
		final int stopBits = 1;
		final int parity = 0; // none see @javax.comm.SerialPort
		final int timeout = 5000;

		return new SerialCommConnection( bpsSpeed, dataBits, stopBits, parity, timeout );
	}

	private ReplHelper buildRepl( Connection conn ) throws IOException, Exception {

		final int maxReplLineSize = 300;

		return new ReplHelper( maxReplLineSize, conn );
	}

	private FileSystemSynchronizer buildSynchronizer( ReplHelper repl ) throws IOException, Exception {

		final int maxFileChunk = 256;

		return new FileSystemSynchronizerImpl( new LocalFileSystemInterface(), new ESP8266FileSystemInterface( repl, maxFileChunk ) );
	}

	@CommandlineMethod( description = "Prints this help", usage = " help " )
	public void help( String args[] ) {

		CommandLineUtils.printObjectHelp( this, System.out );
	}
}

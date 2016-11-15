package mx.com.mjkhajl.micropy.comms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.CodeUtils;
import mx.com.mjkhajl.micropy.utils.Log;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;

public class ReplJavaCommandConsole {

	private final Connection				conn;
	private final FileSystemSynchronizer	sync;
	private final ReplHelper				repl;
	private boolean							readerEnabled	= false;

	public ReplJavaCommandConsole( FileSystemSynchronizer sync, ReplHelper repl, Connection conn ) {

		this.conn = conn;
		this.sync = sync;
		this.repl = repl;
	}

	public void start( FileItem src, FileItem dest ) throws IOException {

		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
		ReadWorker replReader = new ReadWorker();
		String line;

		Log.log( "Welcome to micropython!!!" );

		new Thread( replReader ).start();

		readerEnabled = true;
		
		while ( !"exit".equals( line = reader.readLine() ) ) {

			try {
				switch ( line ) {

					case "java sync":
						readerEnabled = false;
						sync.synchronizeDir( src, dest );
						System.out.print( ">>>" );
						readerEnabled = true;
						continue;
					case "log level":
						readerEnabled = false;
						System.out.println( "set level?" );
						Log.setLogLevelFromArgs( new String[] { reader.readLine() } );
						System.out.print( ">>>" );
						readerEnabled = true;
						continue;
					case "esc":
						conn.write( new byte[] { 03 } );
						continue;
					default:
						conn.write( ( line + "\r\n" ).getBytes() );
				}
			} catch ( Exception e ) {

				Log.log( e, LogLevel.ERROR );
				readerEnabled = true;
			}
		}

		Log.log( "console closed...", LogLevel.INFO );
	}

	public void runScriptFile( File file ) {

		System.out.println( "running script: " + file.getAbsolutePath() );

		readerEnabled = false;
		BufferedReader reader = null;
		List<String> errors = new LinkedList<String>();
		try {

			reader = new BufferedReader( new FileReader( file ) );
			String line;

			while ( ( line = reader.readLine() ) != null ) {

				if ( !line.isEmpty() ) {
					System.out.println( line );
					try {
						System.out.println( repl.sendCommand( line ) );
					} catch ( Exception e ) {
						errors.add( String.valueOf( e ) );
						e.printStackTrace();
					}
				}
			}

		} catch ( Exception e ) {

			System.out.println( e );
		} finally {

			CodeUtils.close( reader );
		}

		if ( errors.isEmpty() ) {

			System.out.println( "Script executed succesfully!!" );
		} else {

			System.out.println( "Script executed with following errors:" );

			for ( String error : errors ) {

				System.out.println( "\t" + error );
			}
		}
		
		readerEnabled = true;
	}

	class ReadWorker implements Runnable {

		@Override
		public void run() {

			try {

				int dat = 0;
				while ( true ) {

					if ( readerEnabled && ( dat = conn.read() ) != -1 ) {

						System.out.print( (char) dat );
					}
				}
			} catch ( Exception e ) {

				Log.log( e, LogLevel.ERROR );
			}
		}
	}
}

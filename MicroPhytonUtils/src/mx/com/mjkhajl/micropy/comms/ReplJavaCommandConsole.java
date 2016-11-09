package mx.com.mjkhajl.micropy.comms;

import java.io.IOException;
import java.util.Scanner;

import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.Log;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;

public class ReplJavaCommandConsole {

	private final Connection				conn;
	private final FileSystemSynchronizer	sync;

	public ReplJavaCommandConsole( Connection conn, FileSystemSynchronizer sync ) {

		this.conn = conn;
		this.sync = sync;
	}

	public void start( FileItem src, FileItem dest ) throws IOException {

		Scanner scanner = new Scanner( System.in );
		ReadWorker replReader = new ReadWorker();
		String line;

		new Thread( replReader ).start();

		while ( !"exit".equals( line = scanner.nextLine() ) ) {

			try {
				switch ( line ) {

					case "java sync":
						replReader.setWaiting( true );
						sync.synchronizeDir( src, dest );
						System.out.print( ">>>" );
						replReader.setWaiting( false );
						continue;
					case "log level":
						System.out.println( "set level?" );
						Log.setLogLevelFromArgs( new String[] { scanner.nextLine() } );
						System.out.print( ">>>" );
						continue;
					case "esc":
						conn.write( new byte[] { 03 } );
						continue;
					default:
						conn.write( ( line + "\r\n" ).getBytes() );
				}
			} catch ( Exception e ) {

				Log.log( e, LogLevel.ERROR );
			}
		}

		Log.log( "console closed...", LogLevel.INFO );
	}

	class ReadWorker implements Runnable {

		private boolean waiting = false;

		@Override
		public void run() {

			try {

				int dat = 0;
				while ( true ) {

					if ( !waiting && ( dat = conn.read() ) != -1 ) {

						System.out.print( (char) dat );
					} else {

						Thread.sleep( 20 );
					}
				}
			} catch ( Exception e ) {

				Log.log( e, LogLevel.ERROR );
			}
		}

		public void setWaiting( boolean waiting ) {
			this.waiting = waiting;
		}
	}
}

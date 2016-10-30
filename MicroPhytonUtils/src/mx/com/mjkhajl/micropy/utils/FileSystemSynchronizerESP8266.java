package mx.com.mjkhajl.micropy.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FileSystemSynchronizerESP8266 extends FileSystemSynchronizerAbstract {

	private final static int		DIR_MODE	= 16384;
	private final int				fileChunkSize;

	private static SerialReplHelper	repl;

	public FileSystemSynchronizerESP8266( long timeout, int speed, int databits, int stopbits, int parity, long rest, int fileChunkSize ) throws Throwable {
		super();

		this.fileChunkSize = fileChunkSize;

		repl = new SerialReplHelper( timeout, speed, databits, stopbits, parity, rest );

		repl.connectToFirstAvailable();
		repl.sendCommand( "import os" );
	}

	@Override
	public List<String> listDir( String path ) throws Exception {

		String commandRes = repl.sendCommand( "os.listdir('" + path + "')" );

		return CodeUtils.extractItemsFromString( commandRes, String.class );
	}

	@Override
	public boolean isDir( String path ) throws Exception {

		String commandRes = repl.sendCommand( "os.stat('" + path + "')" );

		List<Integer> stats = CodeUtils.extractItemsFromString( commandRes, Integer.class );

		return stats.get( 0 ) == DIR_MODE;
	}

	@Override
	public byte[] readFile( String path ) throws Exception {

		String commandRes;
		ByteArrayOutputStream baoStream = null;

		try {

			repl.sendCommand( "file = open('" + path + "', 'rb' )" );
			baoStream = new ByteArrayOutputStream();

			int total = 0;
			
			do {

				total += fileChunkSize;
				
				commandRes = repl.sendCommand( "file.read(" + fileChunkSize + ")" );
				commandRes = CodeUtils.unescapePythonString( commandRes.substring( 2, commandRes.length() - 1 ) );

				System.out.println( "[" + total + "]real: " + commandRes );

				baoStream.write( commandRes.getBytes() );

			} while ( !commandRes.trim().isEmpty() );

			baoStream.flush();

			return baoStream.toByteArray();

		} finally {

			// free objects and collect garbage...
			repl.sendCommandIgnoreErrors( "file.close()" );
			repl.sendCommandIgnoreErrors( "del file" );
			repl.sendCommandIgnoreErrors( "gc.collect()" );

			CodeUtils.close( baoStream );
		}
	}

	@Override
	public void writeDir( File srcDir, String destPath ) throws Exception {

	}

	@Override
	public void writeFile( File srcFile, String destPath ) throws Exception {

		FileInputStream finStream = null;

		try {
			finStream = new FileInputStream( srcFile );

			// open the dest file in 8266
			repl.sendCommand( "file = open('" + destPath + "', 'wb' )" );

			byte[] buffer = new byte[fileChunkSize];
			int readBytes = -1;
			int total = 0;

			while ( ( readBytes = finStream.read( buffer ) ) != -1 ) {

				byte[] chunk = Arrays.copyOf( buffer, readBytes );

				total += fileChunkSize;
				
				System.out.println( "[" + total + "] chunk" + new String( chunk ) );

				repl.sendCommand( "file.write( bytes(" + CodeUtils.byteArrayToString( chunk ) + ") )" );
			}

			repl.sendCommand( "file.flush()" );

		} finally {

			CodeUtils.close( finStream );

			// free objects and collect garbage...
			repl.sendCommandIgnoreErrors( "file.close()" );
			repl.sendCommandIgnoreErrors( "del file" );
			repl.sendCommandIgnoreErrors( "gc.collect()" );
		}
	}

	@Override
	public void close() throws IOException {

		repl.close();
	}
}

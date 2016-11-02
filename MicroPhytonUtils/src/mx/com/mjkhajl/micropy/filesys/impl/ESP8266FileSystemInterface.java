package mx.com.mjkhajl.micropy.filesys.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import mx.com.mjkhajl.micropy.comms.ReplHelper;
import mx.com.mjkhajl.micropy.comms.exception.RemoteReplException;
import mx.com.mjkhajl.micropy.filesys.FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.stream.ESP8266FileInputStream;
import mx.com.mjkhajl.micropy.filesys.stream.ESP8266FileOutputStream;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.CodeUtils;
import mx.com.mjkhajl.micropy.utils.FileItemUtils;

public class ESP8266FileSystemInterface implements FileSystemInterface {

	private final static int	DIR_MODE	= 16384;
	private final int			fileChunkSize;

	private ReplHelper			repl;

	public ESP8266FileSystemInterface( ReplHelper repl, int fileChunkSize ) throws IOException, Exception {
		super();

		this.fileChunkSize = fileChunkSize;

		this.repl = repl;

		repl.sendCommand( "import os" );
	}

	@Override
	public List<String> listDir( FileItem dir ) throws IOException {

		String commandRes = repl.sendCommand( "os.listdir('" + getFullPath( dir ) + "')" );

		return CodeUtils.extractItemsFromString( commandRes, String.class );
	}

	@Override
	public boolean isDir( FileItem file ) throws IOException {

		String commandRes = repl.sendCommand( "os.stat('" + getFullPath( file ) + "')" );

		List<Integer> stats = CodeUtils.extractItemsFromString( commandRes, Integer.class );

		return stats.get( 0 ) == DIR_MODE;
	}

	@Override
	public InputStream openFileRead( FileItem file ) throws IOException {

		return new ESP8266FileInputStream( file, repl, fileChunkSize );
	}

	@Override
	public OutputStream openFileWrite( FileItem file ) throws IOException {

		return new ESP8266FileOutputStream( file, repl, fileChunkSize );
	}

	@Override
	public boolean exists( FileItem file ) throws IOException {

		try {

			isDir( file );

		} catch ( RemoteReplException e ) {

			if ( e.getMessage().endsWith( "ENOENT" ) )
				return false;
		}

		return true;
	}

	@Override
	public void close() throws IOException {

		CodeUtils.close( repl );
	}

	@Override
	public boolean mkdir( FileItem dir ) throws IOException {

		repl.sendCommand( "os.mkdir( '" + getFullPath( dir ) + "' )" );

		return true;
	}

	private String getFullPath( FileItem file ) {

		return FileItemUtils.getFullPath( file );
	}

	@Override
	public boolean rmdir( FileItem dir ) throws IOException {

		repl.sendCommand( "os.rmdir( '" + getFullPath( dir ) + "' )" );

		return true;
	}

	@Override
	public boolean delete( FileItem file ) throws IOException {

		repl.sendCommand( "os.remove( '" + getFullPath( file ) + "' )" );

		return true;
	}

}

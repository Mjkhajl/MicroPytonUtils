package mx.com.mjkhajl.micropy.filesys.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.filesys.FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.CodeUtils;
import mx.com.mjkhajl.micropy.utils.FileItemUtils;

public class ESP8266FileSystemInterface implements FileSystemInterface {

	private final static int	DIR_MODE	= 16384;
	private final int			fileChunkSize;

	private SerialReplHelper	repl;

	public ESP8266FileSystemInterface( SerialReplHelper repl, int fileChunkSize ) throws IOException, Exception {
		super();

		this.fileChunkSize = fileChunkSize;

		this.repl = repl;

		repl.connectToFirstAvailable();
		repl.sendCommand( "import os" );
	}

	@Override
	public List<String> listDir( FileItem dir ) throws IOException {

		String commandRes = repl.sendCommand( "os.listdir('" + FileItemUtils.getFullPath( dir ) + "')" );

		return CodeUtils.extractItemsFromString( commandRes, String.class );
	}

	@Override
	public boolean isDir( FileItem file ) throws IOException {

		String commandRes = repl.sendCommand( "os.stat('" + FileItemUtils.getFullPath( file ) + "')" );

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
	public void close() throws IOException {

		CodeUtils.close( repl );
	}

}

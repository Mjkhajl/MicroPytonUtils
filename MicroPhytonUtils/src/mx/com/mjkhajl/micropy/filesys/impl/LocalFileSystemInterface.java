package mx.com.mjkhajl.micropy.filesys.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import mx.com.mjkhajl.micropy.filesys.FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.FileItemUtils;
import mx.com.mjkhajl.micropy.utils.Log;
import mx.com.mjkhajl.micropy.utils.Log.LogLevel;

public class LocalFileSystemInterface implements FileSystemInterface {

	@Override
	public List<String> listDir( FileItem dir ) throws IOException {

		return Arrays.asList( getFile( dir ).list() );
	}

	@Override
	public boolean isDir( FileItem file ) throws IOException {

		return getFile( file ).isDirectory();
	}

	@Override
	public InputStream openFileRead( FileItem file ) throws IOException {

		File lfile = getFile( file );
		Log.log( "open[R]:" + lfile.getCanonicalPath(), LogLevel.INFO );
		return new FileInputStream( lfile );
	}

	@Override
	public OutputStream openFileWrite( FileItem file ) throws IOException {

		File lfile = getFile( file );
		Log.log( "open[W]: " + lfile.getCanonicalPath(), LogLevel.INFO );
		return new FileOutputStream( lfile );
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean exists( FileItem file ) throws IOException {

		return getFile( file ).exists();
	}

	private File getFile( FileItem file ) {

		return new File( FileItemUtils.getFullPath( file ) );
	}

	@Override
	public boolean mkdir( FileItem dir ) throws IOException {

		return getFile( dir ).mkdirs();
	}

	@Override
	public boolean rmdir( FileItem dir ) throws IOException {

		return getFile( dir ).delete();
	}

	@Override
	public boolean delete( FileItem file ) throws IOException {

		return getFile( file ).delete();
	}
}

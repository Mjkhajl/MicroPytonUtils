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

public class LocalFileSystemInterface implements FileSystemInterface {

	@Override
	public List<String> listDir( FileItem dir ) throws IOException {

		return Arrays.asList( new File( FileItemUtils.getFullPath( dir ) ).list() );
	}

	@Override
	public boolean isDir( FileItem file ) throws IOException {

		return new File( FileItemUtils.getFullPath( file ) ).isDirectory();
	}

	@Override
	public InputStream openFileRead( FileItem file ) throws IOException {

		System.out.println( "open[R]:" + FileItemUtils.getFullPath( file ) );
		return new FileInputStream( FileItemUtils.getFullPath( file ) );
	}

	@Override
	public OutputStream openFileWrite( FileItem file ) throws IOException {

		System.out.println( "open[W]: " + FileItemUtils.getFullPath( file ) );
		return new FileOutputStream( FileItemUtils.getFullPath( file ) );
	}

	@Override
	public void close() throws IOException {
	}
}

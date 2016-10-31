package mx.com.mjkhajl.micropy.filesys.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mx.com.mjkhajl.micropy.filesys.FileSystemInterface;
import mx.com.mjkhajl.micropy.filesys.FileSystemSynchronizer;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Type;
import mx.com.mjkhajl.micropy.utils.CodeUtils;

public class FileSystemSynchronizerImpl implements FileSystemSynchronizer {

	private FileSystemInterface	localFs;
	private FileSystemInterface	remoteFs;

	public FileSystemSynchronizerImpl( FileSystemInterface localFs, FileSystemInterface remoteFs ) {
		super();
		this.localFs = localFs;
		this.remoteFs = remoteFs;
	}

	@Override
	public void synchronizeFs( FileItem src, FileItem dest ) throws Exception {

		process( src );
		process( dest );

		src.printTree( System.out );
		dest.printTree( System.out );
	}

	private FileSystemInterface getFSInterface( FileItem item ) {

		if ( item.getNature() == Nature.LOCAL ) {

			return localFs;
		}
		return remoteFs;
	}

	private void process( FileItem item ) throws Exception {

		if ( getFSInterface( item ).isDir( item ) ) {

			item.setType( Type.DIR );
			processDir( item );
			return;
		}

		item.setType( Type.FILE );
	}

	private void processDir( FileItem dirItem ) throws Exception {

		for ( String fileName : getFSInterface( dirItem ).listDir( dirItem ) ) {

			process( new FileItem( dirItem, fileName ) );
		}
	}

	@Override
	public void close() throws IOException {

		CodeUtils.close( localFs, remoteFs );
	}

	@Override
	public void copyFile( FileItem src, FileItem dest ) throws Exception {

		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			
			inStream = getFSInterface( src ).openFileRead( src );
			outStream = getFSInterface( dest ).openFileWrite( dest );
			int data = -1;
			
			while ( ( data = inStream.read() ) != -1 ) {

				outStream.write( data );
			}
			
			outStream.flush();

		} finally {

			CodeUtils.close( outStream, inStream );
		}
	}

}
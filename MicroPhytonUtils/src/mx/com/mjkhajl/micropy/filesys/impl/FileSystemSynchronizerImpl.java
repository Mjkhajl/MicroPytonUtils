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
import mx.com.mjkhajl.micropy.utils.FileItemUtils;

public class FileSystemSynchronizerImpl implements FileSystemSynchronizer {

	private FileSystemInterface	localFs;
	private FileSystemInterface	remoteFs;

	public FileSystemSynchronizerImpl( FileSystemInterface localFs, FileSystemInterface remoteFs ) {

		super();
		this.localFs = localFs;
		this.remoteFs = remoteFs;
	}

	@Override
	public void synchronizeDir( FileItem srcDir, FileItem desDir ) throws IOException {

		FileSystemInterface srcFs = getFSInterface( srcDir );
		FileSystemInterface desFs = getFSInterface( desDir );

		if ( !exists( desDir ) ) {

			desFs.mkdir( desDir );
		}

		for ( String fileName : srcFs.listDir( srcDir ) ) {

			FileItem srcFile = new FileItem( srcDir, fileName );
			FileItem desFile = new FileItem( desDir, fileName );

			if ( srcFs.isDir( srcFile ) ) {

				srcFile.setType( Type.DIR );
				desFile.setType( Type.DIR );
				synchronizeDir( srcFile, desFile );
			} else {

				srcFile.setType( Type.FILE );
				desFile.setType( Type.FILE );
				synchronizeFile( srcFile, desFile );
			}
		}
	}

	private void synchronizeFile( FileItem srcFile, FileItem destFile ) throws IOException {

		if( !exists( destFile) || !equals( srcFile, destFile ) ){
			
			copyFile( srcFile, destFile );
			return;
		}
		
		System.out.println( "Files are equal..." + FileItemUtils.getFullPath( destFile ) );
	}
	
	private boolean exists( FileItem file ) throws IOException{
		
		return getFSInterface( file ).exists( file );
	}

	private FileSystemInterface getFSInterface( FileItem item ) {

		if ( item.getNature() == Nature.LOCAL ) {

			return localFs;
		}
		return remoteFs;
	}

	private void process( FileItem item ) throws IOException {

		if ( getFSInterface( item ).isDir( item ) ) {

			item.setType( Type.DIR );
			processDir( item );
			return;
		}

		item.setType( Type.FILE );
	}

	private void processDir( FileItem dirItem ) throws IOException {

		for ( String fileName : getFSInterface( dirItem ).listDir( dirItem ) ) {

			process( new FileItem( dirItem, fileName ) );
		}
	}

	@Override
	public void close() throws IOException {

		CodeUtils.close( localFs, remoteFs );
	}

	@Override
	public void copyFile( FileItem src, FileItem dest ) throws IOException {

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

	@Override
	public boolean equals( FileItem aFile, FileItem bFile ) throws IOException {

		InputStream astream = null, bstream = null;
		int adata, bdata;

		try {

			astream = getFSInterface( aFile ).openFileRead( aFile );
			bstream = getFSInterface( bFile ).openFileRead( bFile );

			do {

				adata = astream.read();
				bdata = bstream.read();

				if ( adata != bdata )
					return false;

			} while ( adata != -1 && bdata != -1 );

			return true;

		} finally {

			CodeUtils.close( astream, bstream );
		}
	}

}
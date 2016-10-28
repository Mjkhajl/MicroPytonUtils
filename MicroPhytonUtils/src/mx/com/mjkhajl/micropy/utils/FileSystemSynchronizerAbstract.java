package mx.com.mjkhajl.micropy.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class FileSystemSynchronizerAbstract implements FileSystemSynchronizer {

	private enum Nature {
		LOCAL, REMOTE
	}

	protected Set<String>	srcEntries;
	protected Set<String>	destEntries;

	@Override
	public void synchronizeFs( File srcDir, String dest ) throws Exception {

		this.srcEntries = new TreeSet<String>();
		this.destEntries = new TreeSet<String>();

		process( srcDir.getCanonicalPath(), Nature.LOCAL );
		process( dest, Nature.REMOTE );

		System.out.println( "SRC:" );

		for ( String string : srcEntries ) {
			System.out.println( string );
		}

		System.out.println( "DEST: " );

		for ( String string : destEntries ) {
			System.out.println( string );
		}
	}

	private void process( String entry, Nature nature ) throws Exception {

		addEntry( entry, nature );

		if ( isDir( entry, nature ) ) {

			processDir( entry, nature );
		}
	}

	private void processDir( String dir, Nature nature ) throws Exception {

		for ( String fileName : listDir( dir, nature ) ) {

			process( dir + '/' + fileName, nature );
		}
	}

	private List<String> listDir( String dir, Nature nature ) throws Exception {

		if ( nature == Nature.REMOTE ) {

			return listDir( dir );
		}

		return Arrays.asList( new File( dir ).list() );
	}

	private void addEntry( String entry, Nature nature ) {

		if ( nature == Nature.REMOTE ) {

			destEntries.add( entry );
			return;
		}

		srcEntries.add( entry );
	}

	private boolean isDir( String dir, Nature nature ) throws Exception {

		if ( nature == Nature.REMOTE ) {

			return isDir( dir );
		}
		return new File( dir ).isDirectory();
	}
}
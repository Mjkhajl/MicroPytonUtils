package mx.com.mjkhajl.micropy.utils;

import java.io.Closeable;
import java.io.File;
import java.util.List;

public interface FileSystemSynchronizer extends Closeable {

	void synchronizeFs( File srcDir, String dest ) throws Exception;

	void writeDir( File srcDir, String destPath ) throws Exception;

	void writeFile( File srcFile, String destPath ) throws Exception;

	List<String> listDir( String path ) throws Exception;

	byte[] readFile( String Path ) throws Exception;

	boolean isDir( String path ) throws Exception;
}
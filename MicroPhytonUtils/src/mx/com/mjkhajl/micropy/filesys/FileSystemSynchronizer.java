package mx.com.mjkhajl.micropy.filesys;

import java.io.Closeable;
import java.io.IOException;

import mx.com.mjkhajl.micropy.filesys.vo.FileItem;

public interface FileSystemSynchronizer extends Closeable {

	void synchronizeDir( FileItem src, FileItem dest ) throws IOException;

	void copyFile( FileItem src, FileItem dest ) throws IOException;

	boolean equals( FileItem src, FileItem dest ) throws IOException;
}
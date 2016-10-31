package mx.com.mjkhajl.micropy.filesys;

import java.io.Closeable;

import mx.com.mjkhajl.micropy.filesys.vo.FileItem;

public interface FileSystemSynchronizer extends Closeable {

	void synchronizeFs( FileItem src, FileItem dest ) throws Exception;

	void copyFile( FileItem src, FileItem dest ) throws Exception;

}
package mx.com.mjkhajl.micropy.filesys;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import mx.com.mjkhajl.micropy.filesys.vo.FileItem;

public interface FileSystemInterface extends Closeable {

	List<String> listDir( FileItem dir ) throws IOException;

	InputStream openFileRead( FileItem file ) throws IOException;

	OutputStream openFileWrite( FileItem file ) throws IOException;

	boolean isDir( FileItem file ) throws IOException;
	
	boolean exists( FileItem file ) throws IOException;
	
	boolean mkdir( FileItem dir ) throws IOException;
}

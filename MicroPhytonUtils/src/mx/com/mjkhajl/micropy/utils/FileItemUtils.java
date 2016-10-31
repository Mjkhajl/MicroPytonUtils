package mx.com.mjkhajl.micropy.utils;

import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem.Nature;

public class FileItemUtils {

	private static final char	REMOTE_SEPARATOR	= '/';
	private static final char	LOCAL_SEPARATOR		= '\\';

	public static final String getFullPath( FileItem file ) {

		StringBuilder path = new StringBuilder();

		if ( file.getParent() != null ) {

			path.append( getFullPath( file.getParent() ) )
				.append( getSeparator( file ) );
		}

		return path.append( file.getFileName() ).toString();
	}

	public static char getSeparator( FileItem file ) {

		if ( file.getNature() == Nature.LOCAL )
			return LOCAL_SEPARATOR;
		return REMOTE_SEPARATOR;
	}
}

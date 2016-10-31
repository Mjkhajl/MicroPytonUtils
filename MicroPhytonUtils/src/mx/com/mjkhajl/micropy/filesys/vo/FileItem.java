package mx.com.mjkhajl.micropy.filesys.vo;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import mx.com.mjkhajl.micropy.utils.FileItemUtils;

public class FileItem implements Comparable<FileItem> {

	public enum Nature {
		LOCAL, REMOTE
	}

	private String					fileName;

	private Map<String, FileItem>	children;

	private Nature					nature;

	private FileItem				parent;

	public FileItem( String fileName, Nature nature ) {

		this.nature = nature;
		this.children = new HashMap<String, FileItem>();
		this.fileName = fileName;
	}

	public FileItem( FileItem parent, String fileName ) {
		this( fileName, parent.nature );
		this.parent = parent;

		parent.children.put( fileName, this );
	}

	@Override
	public int compareTo( FileItem o ) {

		return FileItemUtils.getFullPath( this ).compareTo( FileItemUtils.getFullPath( o ) );
	}

	/* @formatter:off */
	@Override
	public String toString() {
		
		return new StringBuilder()
				.append( nature.name().charAt( 0 ) )
				.append( "-" )
				.append( (children.isEmpty())?'F':'D' )
				.append( "-" )
				.append( FileItemUtils.getFullPath( this ) )
				.toString();
	}
	/* @formatter:on */

	public void printTree( PrintStream print ) {

		print.println( this );

		for ( FileItem fileItem : children.values() ) {

			fileItem.printTree( print );
		}
	}

	public String getFileName() {
		return fileName;
	}

	public Map<String, FileItem> getChildren() {
		return children;
	}

	public Nature getNature() {
		return nature;
	}

	public FileItem getParent() {
		return parent;
	}
}

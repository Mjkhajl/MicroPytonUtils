package mx.com.mjkhajl.micropy.filesys.impl;

import java.io.IOException;
import java.io.InputStream;

import mx.com.mjkhajl.micropy.comms.SerialReplHelper;
import mx.com.mjkhajl.micropy.filesys.vo.FileItem;
import mx.com.mjkhajl.micropy.utils.CodeUtils;
import mx.com.mjkhajl.micropy.utils.FileItemUtils;

public class ESP8266FileInputStream extends InputStream {

	private int					index;
	private SerialReplHelper	repl;
	private int[]				buffer;

	public ESP8266FileInputStream( FileItem file, SerialReplHelper repl, int buffSize ) throws IOException {

		this.index = -1;
		this.repl = repl;
		this.buffer = new int[buffSize];

		repl.sendCommand( "import array" );
		repl.sendCommandAsync( "def tointarray( barray ):" );
		repl.sendCommandAsync( "iarray = array.array('i',[])" );
		repl.sendCommandAsync( "for c in barray: iarray.append(c)" );
		repl.sendCommandAsync( "return iarray" );
		repl.sendCommandAsync( "" );
		repl.sendCommandAsync( "" );
		repl.sendCommandAsync( "" );

		// we discard the pending
		repl.getResponseAsync();

		// open the file in 8266
		repl.sendCommand( "file = open('" + FileItemUtils.getFullPath( file ) + "', 'rb' )" );
	}

	@Override
	public int read() throws IOException {

		if ( index == -1 || index >= buffer.length ) {

			String response = repl.sendCommand( "tointarray( file.read(" + buffer.length + ") )" );

			buffer = CodeUtils.extractItemsFromString( response, Integer.class ).stream().mapToInt( Integer::intValue ).toArray();

			index = ( buffer.length == 0 ) ? -1 : 0;
		}
		if ( index == -1 ) {

			return index;
		}

		return buffer[index++];
	}

	@Override
	public void close() throws IOException {

		// free objects and collect garbage...
		repl.sendCommandIgnoreErrors( "file.close()" );
		repl.sendCommandIgnoreErrors( "del file" );
		repl.sendCommandIgnoreErrors( "del tointarray" );
		repl.sendCommandIgnoreErrors( "gc.collect()" );
		super.close();
	}

}

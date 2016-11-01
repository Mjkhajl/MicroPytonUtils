package mx.com.mjkhajl.micropy.comms;

import java.io.Closeable;
import java.io.IOException;

public interface Connection extends Closeable {

	/**
	 * Connects to the specified conn
	 * 
	 * @param connId
	 *            identifier of the connection
	 * @throws IOException
	 *             if the specified connection does not exist or is not
	 *             available
	 */
	public void connectTo( String connId ) throws IOException;

	/**
	 * Connects to the first available autolooked up connection
	 * 
	 * @throws IOException
	 *             if no connection is available
	 */
	public void connectToFirstAvailable() throws IOException;

	/**
	 * Writes the byte array, blocks this connection
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void write( byte[] data ) throws IOException;

	/**
	 * reads one byte from the connection, blocks this connection
	 * 
	 * @return
	 * @throws IOException
	 */
	public int read() throws IOException;
	
	/**
	 * @return true if this connection is established
	 */
	public boolean isConnected();
}

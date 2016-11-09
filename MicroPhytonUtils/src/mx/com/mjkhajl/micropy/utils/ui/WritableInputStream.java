package mx.com.mjkhajl.micropy.utils.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WritableInputStream extends InputStream {

	private ByteArrayInputStream baiStream = null;

	@Override
	public synchronized int read() throws IOException {
		if ( baiStream == null ) {

			try {

				wait();
			} catch ( InterruptedException e ) {

				e.printStackTrace();
			}
		}
		int data = baiStream.read();
		if ( data == -1 )
			baiStream = null;
		return data;
	}

	public synchronized void write( String string ) {

		baiStream = new ByteArrayInputStream( string.getBytes() );
		notify();
	}
}

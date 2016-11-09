package mx.com.mjkhajl.micropy.utils.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class JTextAreaOutputStream extends OutputStream {

	private final JTextArea			textArea;
	private final PrintStream		printStream;
	private int						limit;
	private ByteArrayOutputStream	baoStream	= new ByteArrayOutputStream();

	public JTextAreaOutputStream( JTextArea textArea, PrintStream printStream ) {

		this.textArea = textArea;
		this.printStream = printStream;
	}

	@Override
	public void write( int b ) throws IOException {

		baoStream.write( b );
		printStream.write( b );
		if ( b == '\n' ) {
			internalFlush();
		}
	}

	private void internalFlush() throws IOException {
		baoStream.flush();
		textArea.append( baoStream.toString() );
		limit = textArea.getDocument().getLength();
		textArea.setCaretPosition( limit );
		baoStream.reset();
	}

	@Override
	public void close() throws IOException {
		super.close();
		baoStream.close();
	}

	public int getLimit() {
		return limit;
	}
}

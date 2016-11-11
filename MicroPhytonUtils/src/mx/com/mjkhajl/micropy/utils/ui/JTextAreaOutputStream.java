package mx.com.mjkhajl.micropy.utils.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class JTextAreaOutputStream extends OutputStream {

	private final JTextArea		textArea;
	private final PrintStream	printStream;
	private int					limit;

	public JTextAreaOutputStream( JTextArea textArea, PrintStream printStream ) {

		this.textArea = textArea;
		this.printStream = printStream;
	}

	@Override
	public void write( int b ) throws IOException {

		printStream.write( b );
		textArea.append( String.valueOf( (char) b ) );
		limit = textArea.getDocument().getLength();
		textArea.setCaretPosition( limit );
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	public int getLimit() {
		return limit;
	}
}

package mx.com.mjkhajl.micropy.utils.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UnsupportedLookAndFeelException;

public class JFrameConsoleWindow extends JFrame implements KeyListener, MouseListener {

	private static final long			serialVersionUID	= 1L;
	private WritableInputStream			inStream;
	private JTextAreaOutputStream		outStream;
	private final JTextArea				textArea;
	private final LinkedList<String>	history				= new LinkedList<>();
	private int							historyIndex		= -1;
	private static final PrintStream	OUT					= System.out;

	public JFrameConsoleWindow() throws UnsupportedLookAndFeelException {

		textArea = new JTextArea();
		textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
		textArea.setBackground( new Color( 38, 38, 38 ) );
		textArea.setForeground( new Color( 230, 230, 240 ) );
		textArea.setCaretColor( new Color( 230, 230, 240 ) );
		textArea.addKeyListener( this );
		this.addMouseListener( this );

		inStream = new WritableInputStream();
		outStream = new JTextAreaOutputStream( textArea, OUT );

		PrintStream outPstream = new PrintStream( outStream );
		
		System.setOut( new PrintStream( outStream ) );
		System.setIn( inStream );

		setDefaultCloseOperation( EXIT_ON_CLOSE );
		getContentPane().add( new JScrollPane( textArea ) );
		pack();
		setVisible( true );
		setSize( 800, 600 );
	}

	@Override
	public void keyTyped( KeyEvent e ) {

		switch ( e.getKeyChar() ) {

			case KeyEvent.VK_ENTER:
				String content = textArea.getText();
				String command = content.substring( outStream.getLimit() );
				inStream.write( command );
				addHistory( command );

		}
	}

	private void addHistory( String command ) {

		command = command.replaceAll( "[\r\n]", "" );

		OUT.println( "Command: '" + command + "'" );
		history.addFirst( command );
		historyIndex = -1;
	}

	private String getNextHistory() {

		historyIndex++;

		if ( historyIndex >= history.size() ) {

			historyIndex = history.size() - 1;
		}

		OUT.println( "next idx: " + historyIndex + " history: " + history.size() );

		if ( historyIndex >= 0 ) {
			return history.get( historyIndex );
		}
		return null;
	}

	private String getPrevHistory() {
		historyIndex--;

		OUT.println( "prev idx: " + historyIndex + " history: " + history.size() );
		if ( historyIndex >= 0 ) {

			return history.get( historyIndex );
		}
		historyIndex = -1;
		return null;
	}

	private void replaceCommand( String command ) {

		if ( command != null ) {

			textArea.setText( textArea.getText().substring( 0, outStream.getLimit() ) + command );
		}
	}

	@Override
	public void keyPressed( KeyEvent e ) {

		switch ( e.getKeyCode() ) {

			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
			case KeyEvent.VK_BACK_SPACE:
				if ( textArea.getCaretPosition() > outStream.getLimit() ) {
					break;
				}
				textArea.setCaretPosition( outStream.getLimit() );
				e.consume();
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				replaceCommand( getPrevHistory() );
				e.consume();
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				replaceCommand( getNextHistory() );
				e.consume();
				break;
			default:
				if ( textArea.getCaretPosition() < outStream.getLimit() ) {
					textArea.setCaretPosition( textArea.getText().length() );
				}
		}
	}

	@Override
	public void keyReleased( KeyEvent e ) {
	}

	@Override
	public void mouseClicked( MouseEvent e ) {
	}

	@Override
	public void mousePressed( MouseEvent e ) {
	}

	@Override
	public void mouseReleased( MouseEvent e ) {
		textArea.setCaretPosition( textArea.getDocument().getLength() );
	}

	@Override
	public void mouseEntered( MouseEvent e ) {
	}

	@Override
	public void mouseExited( MouseEvent e ) {
	}
}

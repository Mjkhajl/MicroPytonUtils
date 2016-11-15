package mx.com.mjkhajl.micropy.utils.ui;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mx.com.mjkhajl.micropy.comms.ReplJavaCommandConsole;

public class JFrameConsoleWindow extends JFrame implements KeyListener, ActionListener {

	private static final long				serialVersionUID	= 1L;
	private WritableInputStream				inStream;
	private JTextAreaOutputStream			outStream;
	private final JTextArea					textArea;
	private final LinkedList<String>		history				= new LinkedList<>();
	private int								historyIndex		= -1;
	private static final PrintStream		OUT					= System.out;
	private final FileDialog				fileDialog;
	private final JMenuItem					runMenu, setMenu;
	private File							scriptFile;
	private final ReplJavaCommandConsole	console;

	public JFrameConsoleWindow( ReplJavaCommandConsole console ) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

		this.console = console;

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		fileDialog = new FileDialog( this, "Select a file", FileDialog.LOAD );
		fileDialog.setDirectory( new File( "." ).getCanonicalPath() );

		JMenuBar menuBar = new JMenuBar();
		JMenu scriptMenu = new JMenu( "Script" );

		runMenu = new JMenuItem( "run!!" );
		setMenu = new JMenuItem( "set..." );
		runMenu.setEnabled( false );
		runMenu.addActionListener( this );
		setMenu.addActionListener( this );
		runMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, ActionEvent.ALT_MASK ) );
		setMenu.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.ALT_MASK ) );

		scriptMenu.add( runMenu );
		scriptMenu.add( setMenu );
		menuBar.add( scriptMenu );

		textArea = new JTextArea();
		textArea.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) );
		textArea.setBackground( new Color( 38, 38, 38 ) );
		textArea.setForeground( new Color( 230, 230, 240 ) );
		textArea.setCaretColor( new Color( 230, 230, 240 ) );
		textArea.addKeyListener( this );

		inStream = new WritableInputStream();
		outStream = new JTextAreaOutputStream( textArea, OUT );

		System.setOut( new PrintStream( new PrintStream( outStream ) ) );
		System.setIn( inStream );

		setJMenuBar( menuBar );
		setDefaultCloseOperation( EXIT_ON_CLOSE );
		getContentPane().add( new JScrollPane( textArea ) );
		pack();
		setVisible( true );
		setSize( 800, 600 );
	}

	private void addHistory( String command ) {

		command = command.replaceAll( "[\r\n]", "" );

		if ( !command.isEmpty() ) {

			OUT.println( "Command: '" + command + "'" );
			history.addFirst( command );
			historyIndex = -1;
		}
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
	public void actionPerformed( ActionEvent e ) {

		OUT.println( e );

		switch ( e.getActionCommand() ) {

			case "run!!":
				console.runScriptFile( scriptFile );
				textArea.requestFocus();
				break;
			case "set...":
				fileDialog.setVisible( true );
				scriptFile = fileDialog.getFiles()[0];
				runMenu.setEnabled( true );
				break;
		}
	}

	@Override
	public void keyTyped( KeyEvent e ) {

		switch ( e.getKeyChar() ) {

			case KeyEvent.VK_ENTER:
				String content = textArea.getText();
				String command = content.substring( outStream.getLimit() );
				inStream.write( command );
				addHistory( command );
				break;
			case KeyEvent.VK_BEGIN:
				textArea.setCaretPosition( outStream.getLimit() );
				break;
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
			case KeyEvent.VK_BEGIN:
			case KeyEvent.VK_HOME:
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
			case KeyEvent.VK_ENTER:
				textArea.setCaretPosition( textArea.getText().length() );
			default:
				if ( textArea.getCaretPosition() < outStream.getLimit() ) {
					textArea.setCaretPosition( textArea.getText().length() );
				}
		}
	}

	@Override
	public void keyReleased( KeyEvent e ) {
	}
}

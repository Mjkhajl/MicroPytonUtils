package mx.com.mjkhajl.micropy.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.comm.SerialPort;

public class F8266FileSystemSynchronizer extends FileSystemSynchronizerAbstract {

	private static final int COMM_TIMEOUT = 100;
	private static final int BPS_SPEED = 115200;
	private static final int DIR_MODE = 16384;

	private static SerialReplHelper repl = new SerialReplHelper(COMM_TIMEOUT, BPS_SPEED, SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

	public F8266FileSystemSynchronizer() throws Throwable {
		super();

		repl.connectToFirstAvailable();
		
		repl.sendCommand( "import os" );
	}

	@Override
	public List<String> listDir(String path) throws Exception {
		
		String commandRes = repl.sendCommand( "os.listdir('" + path + "')" );
		
		return matchArrayElements( commandRes, String.class );
	}
	
	@Override
	public boolean isDir(String path) throws Exception{
		
		String commandRes = repl.sendCommand( "os.stat('" + path + "')" );
		
		List<Integer> stats = matchArrayElements(commandRes, Integer.class );
		
		return stats.get( 0 ) == DIR_MODE;
	}
	
	private <T>List<T> matchArrayElements( String test, Class<T> clazz){
		
		
		Pattern pattern = getArrayPattern( clazz );
		
		Matcher matcher = pattern.matcher( test );
		
		List<T> result =new ArrayList<T>();
		
		while( matcher.find() ){
			
			result.add( stringTo( matcher.group(1), clazz ) );
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T stringTo( String value, Class<T> clazz ){
		
		if( clazz == String.class ){
			
			return (T)value;
		}
		
		if( clazz == Integer.class ){
			
			return (T)Integer.valueOf( value );
		}
		
		throw new RuntimeException( "Not implemented for " + clazz );
	}
	
	private <T> Pattern getArrayPattern( Class<T> clazz ){
		
		String regEx="";
		
		if( clazz == String.class ){
			
			regEx = "[']([^'^\\n^\\r^/^\\\\]+)['],?\\s?";
		}
		else if( clazz == Integer.class ){
			
			regEx = "(\\d+),? ?";
		}
		else{
			
			throw new RuntimeException( "Not implemented for " + clazz );
		}
		
		return Pattern.compile(regEx);
	}

	@Override
	public byte[] readFile(String Path) {

		return null;
	}

	@Override
	public void writeDir(File srcDir, String destPath) throws Exception {
		
		
	}

	@Override
	public void writeFile(File srcFile, String destPath) throws Exception {
		
		FileInputStream finStream = null;
		
		try {
			finStream = new FileInputStream( srcFile );
			
			repl.sendCommand( "fileBytes = bytearray([])");
			
			byte[] buffer = new byte[100];
			int readBytes = -1;
			
			while( ( readBytes = finStream.read( buffer ) ) != -1 ){
				
				repl.sendCommand( "fileBytes += bytes(" + Arrays.toString( Arrays.copyOf( buffer, readBytes ) ) + ")");
			}

			// open the dest file in 8266
			repl.sendCommand( "file = open('" + destPath + "', 'wb' )" );
			repl.sendCommand( "file.write( fileBytes )" );
			
		} finally{
			
			finStream.close();
			
			// free objects and collect garbage...
			repl.sendCommand( "file.close()"  );
			repl.sendCommand( "del file" );
			repl.sendCommand( "del fileBytes" );
			repl.sendCommand( "gc.collect()" );
		}
	}

	@Override
	public void close() throws IOException {
		
		repl.close();
	}
}

package mx.com.mjkhajl.micropy.utils;

import java.io.Closeable;
import java.io.IOException;

public class CodeUtils {

	public static <T extends Closeable> void close(T closeable ){
		
		if( closeable != null ){
			
			try {
				
				closeable.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
}

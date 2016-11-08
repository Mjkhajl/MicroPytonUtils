package mx.com.mjkhajl.micropy.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static final SimpleDateFormat	LOG_DATE_FORMAT	= new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SS" );
	public static LogLevel					GL_LOG_LEVEL	= LogLevel.INFO;

	public static enum LogLevel {
		ERROR, INFO, DEBUG
	}

	public static void setLogLevelFromArgs( String[] args ) {

		for ( String arg : args ) {

			switch ( arg ) {

				case "DEBUG":
					GL_LOG_LEVEL = LogLevel.DEBUG;
					break;
				case "ERROR":
					GL_LOG_LEVEL = LogLevel.ERROR;
					break;
				default:
					GL_LOG_LEVEL = LogLevel.INFO;
			}
		}
		System.out.println( "log level: " + GL_LOG_LEVEL.name() );
	}

	public static <T> void log( T o ) {

		log( o, GL_LOG_LEVEL );
	}

	public static <T> void log( T o, LogLevel level ) {

		if ( GL_LOG_LEVEL.ordinal() >= level.ordinal() ) {

			Object message = o;
			
			if ( o instanceof Throwable ) {

				message = new StringWriter();
				( (Throwable) o ).printStackTrace( new PrintWriter( (StringWriter) o ) );
			}

			System.out.println( LOG_DATE_FORMAT.format( new Date() ) + "[" + level + "]" + message );
		}
	}
}

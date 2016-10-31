package mx.com.mjkhajl.micropy.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeUtils {

	private static final Pattern	PATTERN_ARRAY_ITEMS_STRING	= Pattern.compile( "[']([^']+)['],?\\s?" );
	private static final Pattern	PATTERN_ARRAY_ITEMS_INTEGER	= Pattern.compile( "(\\d+),? ?" );
	private static final Pattern	PATTERN_PYTHON_ESCAPECODES	= Pattern.compile( "[\\\\](x[0-9a-f]{2}|[abfnrtvs\\\\'\"\\n])" );

	@SafeVarargs
	public static <T extends Closeable> void close( T... closeables ) {

		for ( T closeable : closeables ) {

			if ( closeable != null ) {

				try {

					closeable.close();

				} catch ( IOException e ) {

					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T stringTo( String value, Class<T> clazz ) {

		if ( clazz == String.class ) {

			return (T) value;
		}
		if ( clazz == Integer.class ) {

			return (T) Integer.valueOf( value );
		}

		throw new RuntimeException( "Not implemented for " + clazz );
	}

	public static <T> Pattern getArrayPattern( Class<T> clazz ) {

		if ( clazz == String.class ) {

			return PATTERN_ARRAY_ITEMS_STRING;
		}
		if ( clazz == Integer.class ) {

			return PATTERN_ARRAY_ITEMS_INTEGER;
		}

		throw new RuntimeException( "Not implemented for " + clazz );
	}

	public static <T> List<T> extractItemsFromString( String test, Class<T> clazz ) {

		Matcher matcher = getArrayPattern( clazz ).matcher( test );

		List<T> result = new ArrayList<T>();

		while ( matcher.find() ) {

			result.add( stringTo( matcher.group( 1 ), clazz ) );
		}

		return result;
	}

	public static String unescapePythonString( final String escaped ) {

		Matcher matcher = PATTERN_PYTHON_ESCAPECODES.matcher( escaped );
		StringBuilder result = new StringBuilder();
		int lastEnd = 0;

		while ( matcher.find() ) {

			result.append( escaped.substring( lastEnd, matcher.start() ) );
			result.append( unescapePythonCode( matcher.group( 1 ) ) );

			lastEnd = matcher.end();
		}

		result.append( escaped.substring( lastEnd ) );

		return result.toString();
	}

	public static char unescapePythonCode( String escapeCode ) {

		if ( escapeCode.charAt( 0 ) == 'x' ) {

			return pythonUnicodeToChar( escapeCode.substring( 1 ) );
		}

		return pythonEscapeToChar( escapeCode.charAt( 0 ) );
	}

	/* @formatter:off */
	public static Character pythonEscapeToChar( char code ) {

		switch ( code ) {
			case 'a':  return '\u0007';
			case 'b':  return '\b';
			case 'f':  return '\u000c';
			case 'n':  return '\n';
			case 'r':  return '\r';
			case 't':  return '\t';
			case 'v':  return '\u000b';
			case 's':  return ' ';
			case '\'': return '\'';
			case '"':  return '"';
			case '\\':  return '\\';
		}
		
		throw new IllegalArgumentException( "No escape sequence for: " + code );
	}
	/* @formatter:on */

	public static char pythonUnicodeToChar( String hexValue ) {

		return (char) Integer.valueOf( hexValue, 16 ).intValue();
	}

	public static String byteArrayToString( byte[] buffer, int start, int end ) {

		StringBuilder sb = new StringBuilder( "[" );

		for ( int i = start; i < end; i++ ) {

			if ( i > start )
				sb.append( ',' );

			sb.append( Byte.toUnsignedInt( buffer[i] ) );
		}

		sb.append( "]" );

		return sb.toString();
	}

}

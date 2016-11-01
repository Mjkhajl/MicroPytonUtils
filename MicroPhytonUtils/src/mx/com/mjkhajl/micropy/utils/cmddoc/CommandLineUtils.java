package mx.com.mjkhajl.micropy.utils.cmddoc;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import mx.com.mjkhajl.micropy.utils.PythonUtilsMain;

public class CommandLineUtils {

	public static void executeCommand( Object main, String[] args ) {

		try {

			Method method = PythonUtilsMain.class.getMethod( args[0], args.getClass() );

			if ( validMethod( method ) ) {

				method.invoke( main, (Object) args );
			}

			return;

		} catch ( InvocationTargetException e ) {

			if( e.getTargetException() != null ){
				
				e.getTargetException().printStackTrace();
			}
			else{
				
				e.printStackTrace();
			}
		} catch ( Exception e ) {
			
			e.printStackTrace();
		}

		throw new IllegalArgumentException( "Unsupported command: " + args[0] );
	}

	private static boolean validMethod( Method method ) {

		int mods = method.getModifiers();

		boolean isPublicNonStatic = Modifier.isPublic( mods ) && !Modifier.isStatic( mods );
		boolean annotated = method.getDeclaredAnnotation( CommandlineMethod.class ) != null;

		return isPublicNonStatic && annotated;
	}

	public static void printObjectHelp( Object main, PrintStream print ) {

		Class<?> mainClass = main.getClass();

		print.println( mainClass.getCanonicalName() + " available commands: " );

		Method[] methods = mainClass.getMethods();

		for ( Method method : methods ) {

			printCommandHelp( mainClass, method, print );
		}
	}

	public static void printCommandHelp( Object main, String command, PrintStream print ) {

		try {

			Class<?> mainClass = main.getClass();

			Method method = mainClass.getMethod( command, String[].class );

			printCommandHelp( mainClass, method, print );

		} catch ( Exception e ) {

			throw new RuntimeException( e );
		}
	}

	private static void printCommandHelp( Class<?> mainClass, Method method, PrintStream print ) {

		if ( validMethod( method ) ) {

			CommandlineMethod annotedDoc = method.getDeclaredAnnotation( CommandlineMethod.class );

			StringBuilder doc = new StringBuilder()
					.append( "\n\tCommand: " ).append( method.getName() )
					.append( "\n\t\tUsage: " ).append( mainClass.getCanonicalName() ).append( annotedDoc.usage() )
					.append( "\n\t\tArguments: " ).append( annotedDoc.argNames().length );

			String[] argNames = annotedDoc.argNames();
			String[] argDesc = annotedDoc.argDescriptions();

			for ( int i = 0; i < argNames.length; i++ ) {

				doc.append( "\n\t\t\t" ).append( argNames[i] ).append( ":\t" ).append( argDesc[i] );
			}

			print.println( doc );
		}
	}
}

package mx.com.mjkhajl.micropy.utils.cmddoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface CommandlineMethod {
	String usage() default "";

	String description() default "";

	String[] argNames() default {};

	String[] argDescriptions() default {};
}

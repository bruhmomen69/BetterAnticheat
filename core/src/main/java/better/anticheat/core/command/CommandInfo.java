package better.anticheat.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows commands to be easily marked with all required information without working with any redundant
 * constructors. This allows all important information for loading to be available upon creation and allows for it to be
 * easily seen by other developers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {

    String name();

    String config();

    Class<? extends Command> parent() default Command.class;
}

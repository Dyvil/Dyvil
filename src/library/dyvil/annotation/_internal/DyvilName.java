package dyvil.annotation._internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @DyvilName("BytecodeName")
// ^ currently type-aliased in the Lang Header as 'BytecodeName'
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface DyvilName
{
	String value();
}

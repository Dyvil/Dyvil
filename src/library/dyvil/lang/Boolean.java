package dyvil.lang;

import dyvil.lang.literal.BooleanConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@BooleanConvertible
public class Boolean
{
	protected static final Boolean	TRUE	= new Boolean(true);
	protected static final Boolean	FALSE	= new Boolean(false);
	
	protected boolean value;
	
	public static Boolean apply(boolean value)
	{
		return value ? TRUE : FALSE;
	}
	
	public static @infix boolean unapply(Boolean v)
	{
		return v == null ? false : v.value;
	}
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	@Intrinsic({ INSTANCE })
	public boolean booleanValue()
	{
		return this.value;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, BINV })
	public @prefix Boolean $bang()
	{
		return apply(!this.value);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND, IFNE })
	public Boolean $amp(boolean v)
	{
		return apply(v && this.value);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR, IFNE })
	public Boolean $bar(boolean v)
	{
		return apply(v || this.value);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR, IFNE })
	public Boolean $up(boolean v)
	{
		return apply(v != this.value);
	}
	
	@Intrinsic({ INSTANCE, BINV, ARGUMENTS, IOR, IFEQ })
	public Boolean $eq$eq$gt(boolean v)
	{
		return apply(v || !this.value);
	}
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public Boolean $lt$eq$gt(boolean v)
	{
		return apply(v == this.value);
	}
	
	// Object methods
	
	public static @infix @inline String toString(boolean value)
	{
		return value ? "true" : "false";
	}
	
	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}
	
	@Override
	public int hashCode()
	{
		return this.value ? 1231 : 1237;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Boolean))
		{
			return false;
		}
		Boolean other = (Boolean) obj;
		return this.value == other.booleanValue();
	}
}

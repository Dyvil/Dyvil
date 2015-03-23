package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.prefix;

public class Boolean implements Boolean$
{
	protected static final Boolean	TRUE	= new Boolean(true);
	protected static final Boolean	FALSE	= new Boolean(false);
	
	protected boolean				value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public static Boolean create(boolean value)
	{
		return value ? TRUE : FALSE;
	}
	
	@Override
	@Intrinsic({ INSTANCE })
	public boolean booleanValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, BINV })
	public @prefix Boolean $bang()
	{
		return create(!this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPNE })
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IAND, IFNE })
	public Boolean $amp(boolean v)
	{
		return create(v && this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IOR, IFNE })
	public Boolean $bar(boolean v)
	{
		return create(v || this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IXOR, IFNE })
	public Boolean $up(boolean v)
	{
		return create(v != this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, BINV, ARGUMENTS, IOR, IFEQ })
	public Boolean $eq$eq$greater(boolean v)
	{
		return create(v || !this.value);
	}
	
	@Override
	@Intrinsic({ INSTANCE, ARGUMENTS, IF_ICMPEQ })
	public Boolean $less$eq$greater(boolean v)
	{
		return create(v == this.value);
	}
	
	// Object methods
	
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
		if (obj == null || !(obj instanceof Number))
		{
			return false;
		}
		Boolean other = (Boolean) obj;
		return this.value == other.booleanValue();
	}
}

package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.BooleanConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@BooleanConvertible
public class Boolean implements Comparable<Boolean>, Serializable
{
	private static final long serialVersionUID = -4115545030218876277L;
	
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
	
	@Intrinsic({ LOAD_0 })
	public boolean booleanValue()
	{
		return this.value;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, BNOT })
	public @prefix Boolean $bang()
	{
		return apply(!this.value);
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IF_ICMPEQ })
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IF_ICMPNE })
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND, IFNE })
	public Boolean $amp(boolean v)
	{
		return apply(v && this.value);
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR, IFNE })
	public Boolean $bar(boolean v)
	{
		return apply(v || this.value);
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR, IFNE })
	public Boolean $up(boolean v)
	{
		return apply(v != this.value);
	}
	
	@Intrinsic({ LOAD_0, BNOT, LOAD_1, IOR, IFEQ })
	public Boolean $eq$eq$gt(boolean v)
	{
		return apply(v || !this.value);
	}
	
	@Intrinsic({ LOAD_0, LOAD_1, IF_ICMPEQ })
	public Boolean $lt$eq$gt(boolean v)
	{
		return apply(v == this.value);
	}
	
	public static @infix int compareTo(boolean b1, boolean b2)
	{
		return b1 == b2 ? 0 : b1 ? 1 : -1;
	}
	
	@Override
	public int compareTo(Boolean o)
	{
		return compareTo(this.value, o.value);
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
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return this.value ? TRUE : FALSE;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return this.value ? TRUE : FALSE;
	}
}

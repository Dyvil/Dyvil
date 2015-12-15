package dyvil.lang;

import dyvil.annotation.Intrinsic;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.lang.literal.BooleanConvertible;
import dyvil.reflect.Modifiers;

import java.io.Serializable;

import static dyvil.reflect.Opcodes.*;

@BooleanConvertible
public class Boolean implements Comparable<Boolean>, Serializable
{
	private static final long serialVersionUID = -4115545030218876277L;
	
	protected static final Boolean TRUE  = new Boolean(true);
	protected static final Boolean FALSE = new Boolean(false);
	
	protected boolean value;
	
	public static Boolean apply(boolean value)
	{
		return value ? TRUE : FALSE;
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean unapply(Boolean v)
	{
		return v != null && v.value;
	}
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	// @formatter:off
	
	public boolean booleanValue() { return this.value; }
	
	@Intrinsic({ LOAD_0, BNOT })
	@DyvilModifiers(Modifiers.PREFIX) public static boolean $bang(boolean v) { return !v; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq(boolean v1, boolean v2) { return v1 == v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bang$eq(boolean v1, boolean v2) { return v1 != v2; }
	
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $amp(boolean v1, boolean v2) { return (v1 && v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $bar(boolean v1, boolean v2) { return (v1 || v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $up(boolean v1, boolean v2) { return (v1 ^ v2); }
	
	@Intrinsic({ LOAD_0, BNOT, LOAD_1, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $eq$eq$gt(boolean v1, boolean v2) { return (!v1 || v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, BNOT, IOR })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq$eq(boolean v1, boolean v2) { return (v1 || !v2); }
	
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	@DyvilModifiers(Modifiers.INFIX) public static boolean $lt$eq$gt(boolean v1, boolean v2) { return (v1 == v2); }
	
	@DyvilModifiers(Modifiers.INFIX) public static int compareTo(boolean b1, boolean b2) { return b1 == b2 ? 0 : b1 ? 1 : -1; }

	@DyvilModifiers(Modifiers.PREFIX)
	public Boolean $bang() { return Boolean.apply(!this.value); }
	
	public Boolean $eq$eq(Boolean v) { return Boolean.apply(this.value == v.value); }
	
	public Boolean $bang$eq(Boolean v) { return Boolean.apply(this.value != v.value); }
	
	public Boolean $amp(Boolean v) { return Boolean.apply(this.value && v.value); }
	
	public Boolean $bar(Boolean v) { return Boolean.apply(this.value || v.value); }
	
	public Boolean $up(Boolean v) { return Boolean.apply(this.value ^ v.value); }
	
	public Boolean $eq$eq$gt(Boolean v) { return Boolean.apply(!this.value || v.value); }
	
	public Boolean $lt$eq$eq(Boolean v) { return Boolean.apply(this.value || !v.value); }
	
	public Boolean $lt$eq$gt(Boolean v) { return Boolean.apply(this.value == v.value); }
	
	@Override
	public int compareTo(Boolean o) { return compareTo(this.value, o.value); }
	
	// @formatter:on
	
	// Object methods
	
	@DyvilModifiers(Modifiers.INFIX)
	public static String toString(boolean value)
	{
		return value ? "true" : "false";
	}
	
	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}
	
	@DyvilModifiers(Modifiers.INFIX)
	public static int $hash$hash(boolean v)
	{
		return v ? 1231 : 1237;
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

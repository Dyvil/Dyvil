package dyvil.lang;

import static dyvil.reflect.Opcodes.*;
import dyvil.lang.annotation.Bytecode;

public class Boolean
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
	
	@Bytecode(postfixOpcode = IBIN)
	public Boolean $bang()
	{
		return create(!this.value);
	}
	
	@Bytecode(postfixOpcode = IF_ICMPNE)
	public boolean $eq$eq(boolean v)
	{
		return this.value == v;
	}
	
	@Bytecode(postfixOpcode = IF_ICMPEQ)
	public boolean $bang$eq(boolean v)
	{
		return this.value != v;
	}
	
	@Bytecode(postfixOpcode = IOR)
	public Boolean $bar(boolean v)
	{
		return create(this.value || v);
	}
	
	@Bytecode(postfixOpcode = IAND)
	public Boolean $amp(boolean v)
	{
		return create(this.value && v);
	}
	
	@Bytecode(postfixOpcode = IXOR)
	public Boolean $up(boolean v)
	{
		return create(this.value ^ v);
	}
}

package dyvil.lang;

import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.prefix;
import static dyvil.reflect.Opcodes.*;

public abstract class Boolean
{
	protected boolean	value;
	
	protected Boolean(boolean value)
	{
		this.value = value;
	}
	
	public abstract Boolean $eq(boolean v);
	
	@Bytecode(postfixOpcode = IBIN)
	public @prefix Boolean $bang()
	{
		return this.$eq(!this.value);
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
		return this.$eq(this.value || v);
	}
	
	@Bytecode(postfixOpcode = IAND)
	public Boolean $amp(boolean v)
	{
		return this.$eq(this.value && v);
	}
	
	@Bytecode(postfixOpcode = IXOR)
	public Boolean $up(boolean v)
	{
		return this.$eq(this.value ^ v);
	}
}

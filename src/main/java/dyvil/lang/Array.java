package dyvil.lang;

import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.sealed;
import dyvil.reflect.Opcodes;

public abstract @sealed class Array<T>
{
	@Bytecode(postfixOpcode = Opcodes.AALOAD)
	public abstract T apply(int i);
	
	@Bytecode(postfixOpcode = Opcodes.AASTORE)
	public abstract void update(int i, T v);
	
	@Bytecode(postfixOpcode = Opcodes.IALOAD)
	public abstract int apply_int(int i);
	
	@Bytecode(postfixOpcode = Opcodes.IASTORE)
	public abstract void update_int(int i, int v);
	
	@Bytecode(postfixOpcode = Opcodes.LALOAD)
	public abstract long apply_long(int i);
	
	@Bytecode(postfixOpcode = Opcodes.LASTORE)
	public abstract void update_long(int i, long v);
	
	@Bytecode(postfixOpcode = Opcodes.FALOAD)
	public abstract float apply_float(int i);
	
	@Bytecode(postfixOpcode = Opcodes.FASTORE)
	public abstract void update_float(int i, float v);
	
	@Bytecode(postfixOpcode = Opcodes.DALOAD)
	public abstract double apply_double(int i);
	
	@Bytecode(postfixOpcode = Opcodes.DASTORE)
	public abstract void update_double(int i, double v);
}

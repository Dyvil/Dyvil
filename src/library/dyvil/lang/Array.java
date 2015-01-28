package dyvil.lang;

import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.sealed;
import dyvil.reflect.Opcodes;

public @sealed interface Array<T>
{
	@Bytecode(postfixOpcode = Opcodes.ARRAYLENGTH)
	public int length();
	
	@Bytecode(postfixOpcode = Opcodes.ARRAYLENGTH)
	public int size();
	
	@Bytecode(postfixOpcode = Opcodes.AALOAD)
	public T apply(int i);
	
	@Bytecode(postfixOpcode = Opcodes.AASTORE)
	public void update(int i, T v);
	
	@Bytecode(postfixOpcode = Opcodes.IALOAD)
	public int apply_int(int i);
	
	@Bytecode(postfixOpcode = Opcodes.IASTORE)
	public void update_int(int i, int v);
	
	@Bytecode(postfixOpcode = Opcodes.LALOAD)
	public long apply_long(int i);
	
	@Bytecode(postfixOpcode = Opcodes.LASTORE)
	public void update_long(int i, long v);
	
	@Bytecode(postfixOpcode = Opcodes.FALOAD)
	public float apply_float(int i);
	
	@Bytecode(postfixOpcode = Opcodes.FASTORE)
	public void update_float(int i, float v);
	
	@Bytecode(postfixOpcode = Opcodes.DALOAD)
	public double apply_double(int i);
	
	@Bytecode(postfixOpcode = Opcodes.DASTORE)
	public void update_double(int i, double v);
}

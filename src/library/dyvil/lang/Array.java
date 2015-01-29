package dyvil.lang;

import dyvil.lang.annotation.Bytecode;
import dyvil.lang.annotation.sealed;
import static dyvil.reflect.Opcodes.*;

public @sealed interface Array<T>
{
	@Bytecode(postfixOpcode = ARRAYLENGTH)
	public int length();
	
	@Bytecode(postfixOpcode = ARRAYLENGTH)
	public int size();
	
	@Bytecode(postfixOpcode = AALOAD)
	public T apply(int i);
	
	@Bytecode(postfixOpcode = AASTORE)
	public void update(int i, T v);
	
	@Bytecode(postfixOpcode = IALOAD)
	public int apply_int(int i);
	
	@Bytecode(postfixOpcode = IASTORE)
	public void update_int(int i, int v);
	
	@Bytecode(postfixOpcode = LALOAD)
	public long apply_long(int i);
	
	@Bytecode(postfixOpcode = LASTORE)
	public void update_long(int i, long v);
	
	@Bytecode(postfixOpcode = FALOAD)
	public float apply_float(int i);
	
	@Bytecode(postfixOpcode = FASTORE)
	public void update_float(int i, float v);
	
	@Bytecode(postfixOpcode = DALOAD)
	public double apply_double(int i);
	
	@Bytecode(postfixOpcode = DASTORE)
	public void update_double(int i, double v);
}

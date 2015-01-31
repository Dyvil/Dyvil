package dyvil.lang;

import dyvil.lang.annotation.Intrinsic;
import dyvil.lang.annotation.sealed;
import static dyvil.reflect.Opcodes.*;

public @sealed interface Array<T>
{
	@Intrinsic({ INSTANCE, ARRAYLENGTH })
	public int length();
	
	@Intrinsic({ INSTANCE, ARRAYLENGTH })
	public int size();
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AALOAD })
	public T apply(int i);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, AASTORE })
	public void update(int i, T v);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IALOAD })
	public int apply_int(int i);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, IASTORE })
	public void update_int(int i, int v);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LALOAD })
	public long apply_long(int i);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, LASTORE })
	public void update_long(int i, long v);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FALOAD })
	public float apply_float(int i);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, FASTORE })
	public void update_float(int i, float v);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DALOAD })
	public double apply_double(int i);
	
	@Intrinsic({ INSTANCE, ARGUMENTS, DASTORE })
	public void update_double(int i, double v);
}

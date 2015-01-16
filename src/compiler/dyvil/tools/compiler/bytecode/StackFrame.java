package dyvil.tools.compiler.bytecode;

import java.util.List;
import java.util.Stack;

import jdk.internal.org.objectweb.asm.Opcodes;

public class StackFrame
{
	public int		type;
	public int		localsCount;
	public Object[]	locals;
	public int		stackCount;
	public Object[]	stack;
	
	public StackFrame(int type, int localsCount, Object[] locals, int stackCount, Object[] stack)
	{
		this.type = type;
		this.localsCount = localsCount;
		this.locals = locals;
		this.stackCount = stackCount;
		this.stack = stack;
	}
	
	public static StackFrame create(List locals, Stack stack)
	{
		return new StackFrame(Opcodes.F_NEW, locals.size(), locals.toArray(), stack.size(), stack.toArray());
	}
}

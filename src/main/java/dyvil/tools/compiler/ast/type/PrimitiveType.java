package dyvil.tools.compiler.ast.type;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;

public class PrimitiveType extends Type
{
	public PrimitiveType(String name, String wrapper)
	{
		super(name);
		this.qualifiedName = wrapper;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	protected boolean isAssignableFrom(Type that)
	{
		return false;
	}
	
	@Override
	public Type resolve(IContext context)
	{
		return this;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buf)
	{
		if (this == Type.VOID)
		{
			buf.append("V");
		}
		else if (this == Type.BOOL)
		{
			buf.append("Z");
		}
		else if (this == Type.BYTE)
		{
			buf.append("B");
		}
		else if (this == Type.SHORT)
		{
			buf.append("S");
		}
		else if (this == Type.CHAR)
		{
			buf.append("C");
		}
		else if (this == Type.INT)
		{
			buf.append("I");
		}
		else if (this == Type.LONG)
		{
			buf.append("J");
		}
		else if (this == Type.FLOAT)
		{
			buf.append("F");
		}
		else if (this == Type.DOUBLE)
		{
			buf.append("D");
		}
	}
	
	@Override
	public int getLoadOpcode()
	{
		if (this == LONG)
		{
			return Opcodes.LLOAD;
		}
		else if (this == FLOAT)
		{
			return Opcodes.FLOAD;
		}
		else if (this == DOUBLE)
		{
			return Opcodes.DLOAD;
		}
		return Opcodes.ILOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		if (this == LONG)
		{
			return Opcodes.LSTORE;
		}
		else if (this == FLOAT)
		{
			return Opcodes.FSTORE;
		}
		else if (this == DOUBLE)
		{
			return Opcodes.DSTORE;
		}
		return Opcodes.ISTORE;
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean equals(Type type)
	{
		return super.equals(type) || this.qualifiedName.equals(type.qualifiedName);
	}
}

package dyvil.tools.compiler.ast.type;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;

public class PrimitiveType extends Type
{
	public int	typecode;
	
	public PrimitiveType(String name, String wrapper, int typecode)
	{
		super(name);
		this.qualifiedName = wrapper;
		this.typecode = typecode;
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
	public String getInternalName()
	{
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
			return "Z";
		case Opcodes.T_BYTE:
			return "B";
		case Opcodes.T_SHORT:
			return "S";
		case Opcodes.T_CHAR:
			return "C";
		case Opcodes.T_INT:
			return "I";
		case Opcodes.T_LONG:
			return "J";
		case Opcodes.T_FLOAT:
			return "F";
		case Opcodes.T_DOUBLE:
			return "D";
		default:
			return "V";
		}
	}
	
	@Override
	public void appendExtendedName(StringBuilder buf)
	{
		buf.append(this.getInternalName());
	}
	
	@Override
	public Object getFrameType()
	{
		if (this == Type.INT)
		{
			return Opcodes.INTEGER;
		}
		else if (this == Type.LONG)
		{
			return Opcodes.LONG;
		}
		else if (this == Type.FLOAT)
		{
			return Opcodes.FLOAT;
		}
		else if (this == Type.DOUBLE)
		{
			return Opcodes.DOUBLE;
		}
		return Opcodes.NULL;
	}
	
	@Override
	public int getLoadOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ALOAD;
		}
		else if (this == LONG)
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
	public int getArrayLoadOpcode()
	{
		if (this == LONG)
		{
			return Opcodes.LALOAD;
		}
		else if (this == FLOAT)
		{
			return Opcodes.FALOAD;
		}
		else if (this == DOUBLE)
		{
			return Opcodes.DALOAD;
		}
		else if (this == BYTE)
		{
			return Opcodes.BALOAD;
		}
		else if (this == SHORT)
		{
			return Opcodes.SALOAD;
		}
		else if (this == CHAR)
		{
			return Opcodes.CALOAD;
		}
		return Opcodes.IALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ASTORE;
		}
		else if (this == LONG)
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
	public int getArrayStoreOpcode()
	{
		if (this == LONG)
		{
			return Opcodes.LASTORE;
		}
		else if (this == FLOAT)
		{
			return Opcodes.FASTORE;
		}
		else if (this == DOUBLE)
		{
			return Opcodes.DASTORE;
		}
		else if (this == BYTE)
		{
			return Opcodes.BASTORE;
		}
		else if (this == SHORT)
		{
			return Opcodes.SASTORE;
		}
		else if (this == CHAR)
		{
			return Opcodes.CASTORE;
		}
		return Opcodes.IASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ARETURN;
		}
		else if (this == INT)
		{
			return Opcodes.IRETURN;
		}
		else if (this == LONG)
		{
			return Opcodes.LRETURN;
		}
		else if (this == FLOAT)
		{
			return Opcodes.FRETURN;
		}
		else if (this == DOUBLE)
		{
			return Opcodes.DRETURN;
		}
		return Opcodes.RETURN;
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean classEquals(Type type)
	{
		return super.classEquals(type) || this.qualifiedName.equals(type.qualifiedName);
	}
	
	@Override
	public PrimitiveType clone()
	{
		PrimitiveType t = new PrimitiveType(this.name, this.qualifiedName, this.typecode);
		t.theClass = this.theClass;
		t.arrayDimensions = this.arrayDimensions;
		return t;
	}
}

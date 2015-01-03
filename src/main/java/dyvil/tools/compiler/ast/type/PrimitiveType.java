package dyvil.tools.compiler.ast.type;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
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
	public boolean isAssignableFrom(IType that)
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
		if (this.arrayDimensions > 0)
		{
			return this.getExtendedName();
		}
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			return Opcodes.INTEGER;
		case Opcodes.T_LONG:
			return Opcodes.LONG;
		case Opcodes.T_FLOAT:
			return Opcodes.FLOAT;
		case Opcodes.T_DOUBLE:
			return Opcodes.DOUBLE;
		default:
			return Opcodes.NULL;
		}
	}
	
	@Override
	public int getLoadOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ALOAD;
		}
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			return Opcodes.ILOAD;
		case Opcodes.T_LONG:
			return Opcodes.LLOAD;
		case Opcodes.T_FLOAT:
			return Opcodes.FLOAD;
		case Opcodes.T_DOUBLE:
			return Opcodes.DLOAD;
		default:
			return 0;
		}
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
			return Opcodes.BALOAD;
		case Opcodes.T_SHORT:
			return Opcodes.SALOAD;
		case Opcodes.T_CHAR:
			return Opcodes.CALOAD;
		case Opcodes.T_INT:
			return Opcodes.IALOAD;
		case Opcodes.T_LONG:
			return Opcodes.LALOAD;
		case Opcodes.T_FLOAT:
			return Opcodes.FALOAD;
		case Opcodes.T_DOUBLE:
			return Opcodes.DALOAD;
		default:
			return 0;
		}
	}
	
	@Override
	public int getStoreOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ASTORE;
		}
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			return Opcodes.ISTORE;
		case Opcodes.T_LONG:
			return Opcodes.LSTORE;
		case Opcodes.T_FLOAT:
			return Opcodes.FSTORE;
		case Opcodes.T_DOUBLE:
			return Opcodes.DSTORE;
		default:
			return 0;
		}
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
			return Opcodes.BASTORE;
		case Opcodes.T_SHORT:
			return Opcodes.SASTORE;
		case Opcodes.T_CHAR:
			return Opcodes.CASTORE;
		case Opcodes.T_INT:
			return Opcodes.IASTORE;
		case Opcodes.T_LONG:
			return Opcodes.LASTORE;
		case Opcodes.T_FLOAT:
			return Opcodes.FASTORE;
		case Opcodes.T_DOUBLE:
			return Opcodes.DASTORE;
		default:
			return 0;
		}
	}
	
	@Override
	public int getReturnOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.ARETURN;
		}
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			return Opcodes.IRETURN;
		case Opcodes.T_LONG:
			return Opcodes.LRETURN;
		case Opcodes.T_FLOAT:
			return Opcodes.FRETURN;
		case Opcodes.T_DOUBLE:
			return Opcodes.DRETURN;
		default:
			return Opcodes.RETURN;
		}
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return super.classEquals(type) || type.isName(this.qualifiedName);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (this.arrayDimensions > 0)
		{
			return ARRAY.resolveField(context, name);
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType... argumentTypes)
	{
		if (this.theClass == null)
		{
			return null;
		}
		
		if (this.arrayDimensions > 0)
		{
			MethodMatch match = ARRAY.resolveMethod(context, name + "_" + this.name, argumentTypes);
			if (match != null)
			{
				return match;
			}
		}
		
		return super.resolveMethod(context, name, argumentTypes);
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

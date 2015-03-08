package dyvil.tools.compiler.ast.type;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.BoxedValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public final class PrimitiveType extends Type
{
	public int		typecode;
	public IMethod	boxMethod;
	public IMethod	unboxMethod;
	
	public PrimitiveType(String name, String wrapper, int typecode)
	{
		super(name);
		this.qualifiedName = wrapper;
		this.typecode = typecode;
	}
	
	public static PrimitiveType fromTypecode(int typecode)
	{
		switch (typecode)
		{
		case Opcodes.T_BOOLEAN:
			return BOOLEAN;
		case Opcodes.T_BYTE:
			return BYTE;
		case Opcodes.T_SHORT:
			return SHORT;
		case Opcodes.T_CHAR:
			return CHAR;
		case Opcodes.T_INT:
			return INT;
		case Opcodes.T_LONG:
			return LONG;
		case Opcodes.T_FLOAT:
			return FLOAT;
		case Opcodes.T_DOUBLE:
			return DOUBLE;
		default:
			return VOID;
		}
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public IValue box(IValue value)
	{
		return new BoxedValue(value, this.boxMethod);
	}
	
	@Override
	public IValue unbox(IValue value)
	{
		return new BoxedValue(value, this.unboxMethod);
	}
	
	@Override
	public IType getElementType()
	{
		int newDims = this.arrayDimensions - 1;
		if (newDims == 0)
		{
			return fromTypecode(this.typecode);
		}
		PrimitiveType t = new PrimitiveType(this.name, this.qualifiedName, this.typecode);
		t.theClass = this.theClass;
		t.arrayDimensions = newDims;
		return t;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public boolean isSuperTypeOf2(IType that)
	{
		return this.theClass == that.getTheClass();
	}
	
	@Override
	public IType resolve(List<Marker> markers, IContext context)
	{
		if (this.theClass == null)
		{
			IType t = resolvePrimitive(this.name);
			if (t != null)
			{
				this.theClass = t.getTheClass();
			}
		}
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
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buf.append('[');
		}
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
			return null;
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
	public void writeDefaultValue(MethodWriter writer)
	{
		if (this.arrayDimensions > 0)
		{
			writer.writeInsn(Opcodes.ACONST_NULL);
			return;
		}
		switch (this.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			writer.writeLDC(0);
			break;
		case Opcodes.T_LONG:
			writer.writeLDC(0L);
			break;
		case Opcodes.T_FLOAT:
			writer.writeLDC(0F);
			break;
		case Opcodes.T_DOUBLE:
			writer.writeLDC(0D);
			break;
		}
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (this == type)
		{
			return true;
		}
		if (type.isName(this.qualifiedName))
		{
			return true;
		}
		return super.classEquals(type);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.arrayDimensions > 0)
		{
			return ARRAY.resolveField(name);
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			MethodMatch match = ARRAY.resolveMethod(instance, name, arguments);
			if (match != null)
			{
				return match;
			}
		}
		
		return this.theClass == null ? null : this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			ARRAY.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
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

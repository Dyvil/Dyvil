package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class PrimitiveType extends Type
{
	public int		typecode;
	public IMethod	boxMethod;
	public IMethod	unboxMethod;
	
	public PrimitiveType(Name name, int typecode)
	{
		super(name);
		this.typecode = typecode;
	}
	
	public static PrimitiveType fromTypecode(int typecode)
	{
		switch (typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return Types.BOOLEAN;
		case ClassFormat.T_BYTE:
			return Types.BYTE;
		case ClassFormat.T_SHORT:
			return Types.SHORT;
		case ClassFormat.T_CHAR:
			return Types.CHAR;
		case ClassFormat.T_INT:
			return Types.INT;
		case ClassFormat.T_LONG:
			return Types.LONG;
		case ClassFormat.T_FLOAT:
			return Types.FLOAT;
		case ClassFormat.T_DOUBLE:
			return Types.DOUBLE;
		default:
			return Types.VOID;
		}
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.arrayDimensions == 0;
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
		PrimitiveType t = new PrimitiveType(this.name, this.typecode);
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
	public IType resolve(MarkerList markers, IContext context)
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
		case ClassFormat.T_BOOLEAN:
			return "Z";
		case ClassFormat.T_BYTE:
			return "B";
		case ClassFormat.T_SHORT:
			return "S";
		case ClassFormat.T_CHAR:
			return "C";
		case ClassFormat.T_INT:
			return "I";
		case ClassFormat.T_LONG:
			return "J";
		case ClassFormat.T_FLOAT:
			return "F";
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
			return ClassFormat.BOOLEAN;
		case ClassFormat.T_BYTE:
			return ClassFormat.BYTE;
		case ClassFormat.T_SHORT:
			return ClassFormat.SHORT;
		case ClassFormat.T_CHAR:
			return ClassFormat.CHAR;
		case ClassFormat.T_INT:
			return ClassFormat.INT;
		case ClassFormat.T_LONG:
			return ClassFormat.LONG;
		case ClassFormat.T_FLOAT:
			return ClassFormat.FLOAT;
		case ClassFormat.T_DOUBLE:
			return ClassFormat.DOUBLE;
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.ILOAD;
		case ClassFormat.T_LONG:
			return Opcodes.LLOAD;
		case ClassFormat.T_FLOAT:
			return Opcodes.FLOAD;
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
			return Opcodes.BALOAD;
		case ClassFormat.T_SHORT:
			return Opcodes.SALOAD;
		case ClassFormat.T_CHAR:
			return Opcodes.CALOAD;
		case ClassFormat.T_INT:
			return Opcodes.IALOAD;
		case ClassFormat.T_LONG:
			return Opcodes.LALOAD;
		case ClassFormat.T_FLOAT:
			return Opcodes.FALOAD;
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.ISTORE;
		case ClassFormat.T_LONG:
			return Opcodes.LSTORE;
		case ClassFormat.T_FLOAT:
			return Opcodes.FSTORE;
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
			return Opcodes.BASTORE;
		case ClassFormat.T_SHORT:
			return Opcodes.SASTORE;
		case ClassFormat.T_CHAR:
			return Opcodes.CASTORE;
		case ClassFormat.T_INT:
			return Opcodes.IASTORE;
		case ClassFormat.T_LONG:
			return Opcodes.LASTORE;
		case ClassFormat.T_FLOAT:
			return Opcodes.FASTORE;
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.IRETURN;
		case ClassFormat.T_LONG:
			return Opcodes.LRETURN;
		case ClassFormat.T_FLOAT:
			return Opcodes.FRETURN;
		case ClassFormat.T_DOUBLE:
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
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			writer.writeLDC(0);
			break;
		case ClassFormat.T_LONG:
			writer.writeLDC(0L);
			break;
		case ClassFormat.T_FLOAT:
			writer.writeLDC(0F);
			break;
		case ClassFormat.T_DOUBLE:
			writer.writeLDC(0D);
			break;
		}
	}
	
	@Override
	public IConstantValue getDefaultValue()
	{
		if (this.arrayDimensions > 0)
		{
			return NullValue.getNull();
		}
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return BooleanValue.TRUE;
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return IntValue.getNull();
		case ClassFormat.T_LONG:
			return LongValue.getNull();
		case ClassFormat.T_FLOAT:
			return FloatValue.getNull();
		case ClassFormat.T_DOUBLE:
			return DoubleValue.getNull();
		}
		return null;
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (this == type)
		{
			return true;
		}
		if (type.getName() == this.name)
		{
			return true;
		}
		return super.classEquals(type);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (this.arrayDimensions > 0)
		{
			return Types.ARRAY.resolveField(name);
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			Types.ARRAY.getMethodMatches(list, instance, name, arguments);
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
		PrimitiveType t = new PrimitiveType(this.name, this.typecode);
		t.theClass = this.theClass;
		t.arrayDimensions = this.arrayDimensions;
		return t;
	}
}

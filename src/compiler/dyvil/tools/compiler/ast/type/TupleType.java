package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class TupleType implements IObjectType, ITypeList
{
	public static final IClass[] tupleClasses = new IClass[22];
	public static final String[] descriptors  = new String[22];
	
	protected IType[] types;
	protected int     typeCount;
	
	public TupleType()
	{
		this.types = new IType[2];
	}
	
	public TupleType(int size)
	{
		this.types = new IType[size];
	}
	
	public TupleType(IType[] types, int typeCount)
	{
		this.types = types;
		this.typeCount = typeCount;
	}
	
	// ITypeList Overrides
	
	public static IClass getTupleClass(int count)
	{
		IClass iclass = tupleClasses[count];
		if (iclass != null)
		{
			return iclass;
		}
		
		iclass = Package.dyvilTuple.resolveClass("Tuple" + count);
		tupleClasses[count] = iclass;
		return iclass;
	}
	
	public static String getConstructorDescriptor(int typeCount)
	{
		String s = descriptors[typeCount];
		if (s != null)
		{
			return s;
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < typeCount; i++)
		{
			buffer.append("Ljava/lang/Object;");
		}
		buffer.append(")V");
		
		return descriptors[typeCount] = buffer.toString();
	}
	
	public static boolean isSuperType(IType type, ITyped[] typedArray, int count)
	{
		IClass iclass = getTupleClass(count);
		if (!iclass.isSubTypeOf(type))
		{
			return false;
		}
		
		for (int i = 0; i < count; i++)
		{
			ITypeParameter typeVar = iclass.getTypeParameter(i);
			IType type1 = type.resolveTypeSafely(typeVar);
			if (!typedArray[i].isType(type1))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int typeTag()
	{
		return TUPLE;
	}
	
	@Override
	public boolean isGenericType()
	{
		return true;
	}
	
	@Override
	public IClass getTheClass()
	{
		return getTupleClass(this.typeCount);
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public int typeCount()
	{
		return this.typeCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.types[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.typeCount++;
		if (this.typeCount > this.types.length)
		{
			IType[] temp = new IType[this.typeCount];
			System.arraycopy(this.types, 0, temp, 0, index);
			this.types = temp;
		}
		this.types[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.types[index];
	}
	
	// IType Overrides
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		IClass iclass = getTupleClass(this.typeCount);
		if (!iclass.isSubTypeOf(type))
		{
			return false;
		}
		
		for (int i = 0; i < this.typeCount; i++)
		{
			ITypeParameter typeVar = iclass.getTypeParameter(i);
			IType type1 = type.resolveTypeSafely(typeVar);
			
			// Covariance
			if (!this.types[i].isSuperTypeOf(type1))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public IType getSuperType()
	{
		return Types.OBJECT;
	}
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		int index = typeParameter.getIndex();
		
		IClass iclass = this.getTheClass();
		if (iclass.getTypeParameter(index) != typeParameter)
		{
			return iclass.resolveType(typeParameter, this);
		}
		
		if (index >= this.typeCount)
		{
			return null;
		}
		return this.types[index];
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			if (this.types[i].hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		TupleType tt = new TupleType(this.typeCount);
		tt.typeCount = this.typeCount;
		for (int i = 0; i < this.typeCount; i++)
		{
			tt.types[i] = this.types[i].getConcreteType(context);
		}
		return tt;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		IClass iclass = getTupleClass(this.typeCount);
		for (int i = 0; i < this.typeCount; i++)
		{
			ITypeParameter typeVar = iclass.getTypeParameter(i);
			IType concreteType = concrete.resolveTypeSafely(typeVar);
			if (concreteType != null)
			{
				this.types[i].inferTypes(concreteType, typeContext);
			}
		}
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		if (this.typeCount == 0)
		{
			return Types.VOID;
		}
		if (this.typeCount == 1)
		{
			return this.types[0].resolveType(markers, context);
		}
		
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i] = this.types[i].resolveType(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].resolve(markers, context);
		}
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		if (position == TypePosition.CLASS)
		{
			markers.add(Markers.semantic(this.types[0].getPosition(), "type.class.tuple"));
		}
		
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].checkType(markers, context, TypePosition.GENERIC_ARGUMENT);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].cleanup(context, compilableList);
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.getTheClass().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		this.getTheClass().getConstructorMatches(list, arguments);
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public String getSignature()
	{
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append('L').append(this.getInternalName());
		buf.append('<');
		for (IType t : this.types)
		{
			t.appendSignature(buf);
		}
		buf.append('>').append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.typeCount);
		writer.writeNewArray("dyvilx/lang/model/type/Type", 1);
		for (int i = 0; i < this.typeCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			writer.writeLDC(i);
			this.types[i].writeTypeExpression(writer);
			writer.writeInsn(Opcodes.AASTORE);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/TupleType", "apply",
		                       "([Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/TupleType;", false);
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}
		
		int index = typePath.getStepArgument(step);
		this.types[index] = IType.withAnnotation(this.types[index], annotation, typePath, step + 1, steps);
	}
	
	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].writeAnnotations(visitor, typeRef, typePath + i + ';');
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.typeCount);
		for (int i = 0; i < this.typeCount; i++)
		{
			IType.writeType(this.types[i], out);
		}
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		int len = this.typeCount = in.readByte();
		if (len > this.types.length)
		{
			this.types = new IType[len];
		}
		for (int i = 0; i < len; i++)
		{
			this.types[i] = IType.readType(in);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(");
		if (this.typeCount > 0)
		{
			builder.append(this.types[0]);
			for (int i = 1; i < this.typeCount; i++)
			{
				builder.append(", ").append(this.types[i]);
			}
		}
		return builder.append(")").toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.typeCount == 0)
		{
			if (Formatting.getBoolean("tuple.empty.space_between"))
			{
				buffer.append("( )");
			}
			else
			{
				buffer.append("()");
			}
			return;
		}

		buffer.append('(');
		if (Formatting.getBoolean("tuple.open_paren.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(prefix, this.types, this.typeCount, Formatting.getSeparator("tuple.separator", ','), buffer);

		if (Formatting.getBoolean("tuple.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
	
	@Override
	public IType clone()
	{
		TupleType tt = new TupleType(this.typeCount);
		tt.typeCount = this.typeCount;
		System.arraycopy(this.types, 0, tt.types, 0, this.typeCount);
		return tt;
	}
}

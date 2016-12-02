package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class TupleType implements IObjectType, ITypeList
{
	public static final int MAX_ARITY = 10;

	public static final IClass[] tupleClasses = new IClass[MAX_ARITY];
	public static final String[] descriptors  = new String[MAX_ARITY];

	protected IType[] types;
	protected int     typeCount;

	protected ICodePosition position;

	public TupleType()
	{
		this.types = new IType[2];
	}

	public TupleType(int size)
	{
		this.types = new IType[size];
	}

	public TupleType(IType... types)
	{
		this.types = types;
		this.typeCount = types.length;
	}

	public TupleType(IType[] types, int typeCount)
	{
		this.types = types;
		this.typeCount = typeCount;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	// ITypeList Overrides

	public static IClass getTupleClass(int count)
	{
		IClass iclass = tupleClasses[count];
		if (iclass != null)
		{
			return iclass;
		}

		iclass = Package.dyvilTuple.resolveClass(Names.Tuple).resolveClass(Name.fromQualified("Of" + count));
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
		if (!iclass.isSubClassOf(type))
		{
			return false;
		}

		for (int i = 0; i < count; i++)
		{
			ITypeParameter typeVar = iclass.getTypeParameter(i);
			IType type1 = Types.resolveTypeSafely(type, typeVar);
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
		return Names.Tuple;
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
		if (!iclass.isSubClassOf(type))
		{
			return false;
		}

		for (int i = 0; i < this.typeCount; i++)
		{
			final ITypeParameter typeVar = iclass.getTypeParameter(i);
			final IType otherElementType = Types.resolveTypeSafely(type, typeVar);

			// Tuple Element Types are Covariant
			if (!Types.isSuperType(this.types[i], otherElementType))
			{
				return false;
			}
		}
		return true;
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
		final IType[] types = GenericType.getConcreteTypes(this.types, this.typeCount, context);
		if (types == this.types)
		{
			// Nothing changed, no need to create a new instance
			return this;
		}
		return new TupleType(types, this.typeCount);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		IClass iclass = getTupleClass(this.typeCount);
		for (int i = 0; i < this.typeCount; i++)
		{
			ITypeParameter typeVar = iclass.getTypeParameter(i);
			IType concreteType = Types.resolveTypeSafely(concrete, typeVar);
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
	public void checkType(MarkerList markers, IContext context, int position)
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		getTupleClass(this.typeCount).getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		getTupleClass(this.typeCount).getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('L').append(this.getInternalName());

		if (type != NAME_DESCRIPTOR)
		{
			final int parType = type == NAME_FULL ? NAME_FULL : NAME_SIGNATURE_GENERIC_ARG;

			buffer.append('<');
			for (int i = 0; i < this.typeCount; i++)
			{
				this.types[i].appendDescriptor(buffer, parType);
			}
			buffer.append('>');
		}

		buffer.append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(this.typeCount);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvilx/lang/model/type/Type");
		for (int i = 0; i < this.typeCount; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.types[i].writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/TupleType", "apply",
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

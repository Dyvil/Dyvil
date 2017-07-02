package dyvil.tools.compiler.ast.type.compound;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.generic.ResolvedGenericType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TupleType extends ResolvedGenericType
{
	public static final int MAX_ARITY = 20 + 1;

	public static final IClass[] tupleClasses = new IClass[MAX_ARITY];
	public static final String[] descriptors  = new String[MAX_ARITY];

	public TupleType()
	{
		super(null);
	}

	public TupleType(int size)
	{
		super(null, null, new TypeList(size));
	}

	public TupleType(IType... types)
	{
		super(null, null, types);
	}

	public TupleType(TypeList types)
	{
		super(null, null, types);
	}

	// TypeList Overrides

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

	@Override
	public int typeTag()
	{
		return TUPLE;
	}

	@Override
	public Name getName()
	{
		return Names.Tuple;
	}

	@Override
	public IClass getTheClass()
	{
		return getTupleClass(this.arguments.size());
	}

	// IType Overrides

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		final int size = this.arguments.size();
		if (size == 0)
		{
			return Types.VOID;
		}
		if (size == 1)
		{
			return this.arguments.get(0).resolveType(markers, context);
		}

		this.arguments.resolveTypes(markers, context);
		this.theClass = getTupleClass(size);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if (position == TypePosition.CLASS)
		{
			markers.add(Markers.semanticError(this.getPosition(), "type.class.tuple"));
		}

		this.arguments.checkTypes(markers, context, IType.TypePosition.genericArgument(position));
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		final int size = this.arguments.size();
		writer.visitLdcInsn(size);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvil/reflect/types/Type");
		for (int i = 0; i < size; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.arguments.get(i).writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/TupleType", "apply",
		                       "([Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/TupleType;", false);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.arguments.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.arguments.read(in);
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		return new TupleType(arguments);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder("(");
		final int size = this.arguments.size();
		if (size > 0)
		{
			builder.append(this.arguments.get(0));
			for (int i = 1; i < size; i++)
			{
				builder.append(", ").append(this.arguments.get(i));
			}
		}
		return builder.append(")").toString();
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.arguments.toString(indent, buffer, '(', ')');
	}
}

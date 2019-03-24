package dyvilx.tools.compiler.ast.type.compound;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.LambdaExpr;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.generic.GenericType;
import dyvilx.tools.compiler.ast.type.generic.ResolvedGenericType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class FunctionType extends ResolvedGenericType
{
	private static final IClass[] functionClasses = new IClass[TupleType.MAX_ARITY];

	// Metadata
	protected boolean extension;

	public FunctionType()
	{
		super(null);
	}

	public FunctionType(@NonNull TypeList arguments)
	{
		super(null, null, arguments);
	}

	public FunctionType(@NonNull SourcePosition position, IType... arguments)
	{
		super(position, null, arguments);
	}

	public FunctionType(@NonNull SourcePosition position, TypeList arguments)
	{
		super(position, null, arguments);
	}

	public static IClass getLambdaClass(int typeCount)
	{
		IClass iclass = functionClasses[typeCount];
		if (iclass != null)
		{
			return iclass;
		}

		iclass = Package.dyvilFunction.resolveClass(Names.Function).resolveClass(Name.fromQualified("Of" + typeCount));
		functionClasses[typeCount] = iclass;
		return iclass;
	}

	@Override
	public int typeTag()
	{
		return LAMBDA;
	}

	@Override
	public Name getName()
	{
		return Names.Function;
	}

	public void setExtension(boolean extension)
	{
		this.extension = extension;
	}

	public boolean isExtension()
	{
		return this.extension;
	}

	@Override
	public IClass getTheClass()
	{
		return getLambdaClass(this.arguments.size() - 1);
	}

	// IType Overrides

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return this.arguments.size() == 1 && Types.isSuperType(this.arguments.get(0), type);
	}

	@Override
	public IValue convertFrom(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isConvertibleFrom(type))
		{
			return null;
		}

		final IValue typedReturnValue = value.withType(this.arguments.get(0), typeContext, markers, context);
		if (typedReturnValue != null)
		{
			return this.wrapLambda(typedReturnValue);
		}
		return null;
	}

	public LambdaExpr wrapLambda(IValue value)
	{
		IType returnType = value.getType();

		final LambdaExpr lambdaExpr = new LambdaExpr(value.getPosition(), null, 0);
		lambdaExpr.setImplicitParameters(true);
		lambdaExpr.setMethod(this.getFunctionalMethod());
		lambdaExpr.setValue(value);
		lambdaExpr.inferReturnType(this, returnType);
		return lambdaExpr;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (Types.isSuperType(this, concrete))
		{
			super.inferTypes(concrete, typeContext);
		}
		else if (this.isConvertibleFrom(concrete))
		{
			this.arguments.get(0).inferTypes(concrete, typeContext);
		}
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
		this.theClass = this.getTheClass();
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if (position == TypePosition.CLASS)
		{
			markers.add(Markers.semanticError(this.position, "type.lambda.class"));
		}

		this.arguments.checkTypes(markers, context, TypePosition.genericArgument(position));
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		final int size = this.arguments.size();
		final int returnIndex = size - 1;
		this.arguments.get(returnIndex).writeTypeExpression(writer);

		writer.visitLdcInsn(returnIndex);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvil/reflect/types/Type");
		for (int i = 0; i < returnIndex; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.arguments.get(i).writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/FunctionType", "apply",
		                       "(Ldyvil/reflect/types/Type;[Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/FunctionType;",
		                       false);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.arguments.write(out);
		out.writeBoolean(this.extension);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.arguments.read(in);
		this.extension = in.readBoolean();
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		final FunctionType functionType = new FunctionType(this.position, arguments);
		functionType.position = this.position;
		functionType.extension = this.extension;
		return functionType;
	}

	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');

		final int returnIndex = this.arguments.size() - 1;

		if (returnIndex > 0)
		{
			buffer.append(this.arguments.get(0));
			for (int i = 1; i < returnIndex; i++)
			{
				buffer.append(", ").append(this.arguments.get(i));
			}
		}

		buffer.append(") -> ").append(this.arguments.get(returnIndex));
		return buffer.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final IType parameterType;
		final int parameterTypeTag;
		final int size = this.arguments.size();
		if (size == 2 && (parameterTypeTag = (parameterType = this.arguments.get(0)).typeTag()) != TUPLE
			    && parameterTypeTag != LAMBDA && !Formatting.getBoolean("lambda.single.wrap"))
		{
			// Single Parameter Type that is neither a Lambda Type nor a Tuple Type

			parameterType.toString(prefix, buffer);

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (size > 1)
		{
			buffer.append('(');
			if (Formatting.getBoolean("lambda.open_paren.space_after"))
			{
				buffer.append(' ');
			}

			Util.astToString(prefix, this.arguments.getTypes(), size - 1,
			                 Formatting.getSeparator("lambda.separator", ','), buffer);

			if (Formatting.getBoolean("lambda.close_paren.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(')');

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (Formatting.getBoolean("lambda.empty.wrap"))
		{
			buffer.append("()");

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append("->");

		if (Formatting.getBoolean("lambda.arrow.space_after"))
		{
			buffer.append(' ');
		}

		this.arguments.get(size - 1).toString("", buffer);
	}
}

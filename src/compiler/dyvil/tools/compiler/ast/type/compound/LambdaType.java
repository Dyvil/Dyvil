package dyvil.tools.compiler.ast.type.compound;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.generic.ResolvedGenericType;
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

public final class LambdaType extends ResolvedGenericType
{
	private static final IClass[] functionClasses = new IClass[22];

	// Metadata
	protected boolean extension;

	public LambdaType()
	{
		super(null);
	}

	public LambdaType(@NonNull TypeList arguments)
	{
		super(null, null, arguments);
	}

	public LambdaType(@NonNull ICodePosition position, IType... arguments)
	{
		super(position, null, arguments);
	}

	public LambdaType(@NonNull ICodePosition position, TypeList arguments)
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
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.arguments.size() != 1 || value.isType(this))
		{
			return value.withType(this, typeContext, markers, context);
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

		this.arguments.checkTypes(markers, context, TypePosition.GENERIC_ARGUMENT);
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
		final LambdaType lambdaType = new LambdaType(this.position, arguments);
		lambdaType.position = this.position;
		lambdaType.extension = this.extension;
		return lambdaType;
	}

	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');

		final int returnIndex = this.arguments.size() - 1;

		buffer.append(this.arguments.get(0));
		for (int i = 1; i < returnIndex; i++)
		{
			buffer.append(", ").append(this.arguments.get(i));
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

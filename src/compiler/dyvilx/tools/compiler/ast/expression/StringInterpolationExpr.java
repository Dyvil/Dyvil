package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.constant.StringValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.CaseClasses;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class StringInterpolationExpr implements IValue
{
	public static final class LazyFields
	{
		public static final IClass STRING_INTERPOLATION_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS.resolveClass(
			Name.fromRaw("FromStringInterpolation"));

		private LazyFields()
		{
			// no instances
		}
	}

	protected SourcePosition position;

	private ArgumentList values;

	public StringInterpolationExpr()
	{
		this.values = new ArgumentList();
	}

	public StringInterpolationExpr(SourcePosition position)
	{
		this.values = new ArgumentList(3);
		this.position = position;
	}

	public StringInterpolationExpr(IValue... values)
	{
		final int size = values.length;
		this.position = size == 0 ? null : SourcePosition.$dot$dot(values[0].getPosition(), values[size - 1].getPosition());

		this.values = new ArgumentList(values);
	}

	public static StringInterpolationExpr apply(IValue lhs, IValue rhs)
	{
		if (lhs.valueTag() == STRING_INTERPOLATION)
		{
			final StringInterpolationExpr lhsInterpol = (StringInterpolationExpr) lhs;
			if (rhs.valueTag() == STRING_INTERPOLATION)
			{
				lhsInterpol.appendAll((StringInterpolationExpr) rhs);
			}
			else
			{
				lhsInterpol.append(rhs);
			}

			return lhsInterpol;
		}
		if (rhs.valueTag() == STRING_INTERPOLATION)
		{
			final StringInterpolationExpr rhsInterpol = (StringInterpolationExpr) rhs;
			rhsInterpol.prepend(lhs);
			return rhsInterpol;
		}
		return new StringInterpolationExpr(lhs, rhs);
	}

	@Override
	public int valueTag()
	{
		return STRING_INTERPOLATION;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	public ArgumentList getValues()
	{
		return this.values;
	}

	public void append(IValue value)
	{
		this.values.add(value);
	}

	public void appendAll(StringInterpolationExpr interpol)
	{
		this.values = this.values.concat(interpol.values);
	}

	public void prepend(IValue value)
	{
		this.values.insert(0, value);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.STRING;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSuperType(type, Types.STRING))
		{
			return this;
		}

		Annotation annotation;
		if ((annotation = type.getAnnotation(Types.FROMSTRING_CLASS)) != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		if ((annotation = type.getAnnotation(LazyFields.STRING_INTERPOLATION_CONVERTIBLE)) == null)
		{
			return null;
		}

		return new LiteralConversion(this, annotation, this.values).withType(type, typeContext, markers, context);
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, Types.STRING) || this.isConvertible(type);
	}

	private boolean isConvertible(IType type)
	{
		return type.getAnnotation(Types.FROMSTRING_CLASS) != null
		       || type.getAnnotation(LazyFields.STRING_INTERPOLATION_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		return this.isConvertible(type) ? CONVERSION_MATCH : MISMATCH;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.values.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.values.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.values.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.values.check(markers, context);

		for (IValue value : this.values)
		{
			if (Types.isVoid(value.getType()))
			{
				markers.add(Markers.semanticError(value.getPosition(), "string.interpolation.void"));
			}
		}
	}

	@Override
	public IValue foldConstants()
	{
		this.values.foldConstants();

		// Merges adjacent String constants into one

		final int size = this.values.size();
		final ArgumentList newValues = new ArgumentList(size);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++)
		{
			final IValue value = this.values.get(i);
			if (value.toStringBuilder(builder))
			{
				continue;
			}

			moveString(builder, newValues);
			newValues.add(value);
		}
		moveString(builder, newValues);

		if (newValues.isEmpty())
		{
			return new StringValue("");
		}

		// assert newValues.size() >= 1
		final IValue first = newValues.getFirst();
		if (newValues.size() == 1 && isString(first))
		{
			return first;
		}

		this.values = newValues;
		return this;
	}

	private static boolean isString(IValue value)
	{
		return Types.isSuperType(Types.STRING, value.getType());
	}

	private static void moveString(StringBuilder builder, ArgumentList newValues)
	{
		if (builder.length() > 0)
		{
			newValues.add(new StringValue(builder.toString()));
			builder.delete(0, builder.length());
		}
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.values.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeExpression(writer);

		if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.lineNumber());
		}
	}

	private void writeExpression(MethodWriter writer)
	{
		final int size = this.values.size();
		switch (size)
		{
		case 0: // not possible
			writer.visitLdcInsn("");
			return;
		case 1:
			// "\(someValue)"
			CaseClasses.writeToString(writer, this.values.getFirst());
			return;
		case 2:
			final IValue first = this.values.getFirst();
			final IValue last = this.values.getLast();
			if (isString(first) && isString(last))
			{
				// two non-null String values -> write first.concat(last)
				first.writeExpression(writer, Types.STRING);
				last.writeExpression(writer, Types.STRING);
				writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat",
				                       "(Ljava/lang/String;)Ljava/lang/String;", false);
				return;
			}
			// continue as normal
		}

		int estSize = 0;
		for (int i = 0; i < size; i++)
		{
			estSize += this.values.get(i).stringSize();
		}

		writer.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(estSize);
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V", false);

		for (int i = 0; i < size; i++)
		{
			final IValue value = this.values.get(i);
			value.writeExpression(writer, null);
			CaseClasses.writeStringAppend(writer, value.getType());
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
		                       false);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final int size = this.values.size();

		buffer.append('"');
		for (int i = 0; i < size; i++)
		{
			final IValue value = this.values.get(i);
			final int tag = value.valueTag();
			if (tag == IValue.STRING || tag == IValue.CHAR)
			{
				StringLiterals.appendStringLiteralBody(value.stringValue(), buffer);
			}
			else
			{
				// \(value)

				buffer.append('\\').append('(');
				value.toString(prefix, buffer);
				buffer.append(')');
			}
		}
		buffer.append('"');
	}
}

package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class StringInterpolationExpr implements IValue
{
	public static final class LazyFields
	{
		public static final IClass STRING_INTERPOLATION_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass(
			"StringInterpolationConvertible");

		private LazyFields()
		{
			// no instances
		}
	}

	protected ICodePosition position;

	private IValue[] values  = new IValue[2];
	private String[] strings = new String[3];
	private int valueCount;

	public StringInterpolationExpr(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return STRING_INTERPOLATION;
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

	public void addString(String stringPart)
	{
		final int index = this.valueCount;
		if (index >= this.strings.length)
		{
			final String[] temp = new String[index + 1];
			System.arraycopy(this.strings, 0, temp, 0, this.strings.length);
			this.strings = temp;
		}
		this.strings[index] = stringPart;
	}

	public void addValue(IValue value)
	{
		final int index = this.valueCount++;
		if (index >= this.values.length)
		{
			final IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.values, 0, temp, 0, this.values.length);
			this.values = temp;
		}
		this.values[index] = value;
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
		if (type.isSuperTypeOf(Types.STRING))
		{
			return this;
		}

		IAnnotation annotation;
		if ((annotation = type.getAnnotation(Types.STRING_CONVERTIBLE_CLASS)) != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		if ((annotation = type.getAnnotation(LazyFields.STRING_INTERPOLATION_CONVERTIBLE)) == null)
		{
			return null;
		}

		StringValue string;
		if (this.valueCount > 0)
		{
			// stringCount = valueCount + 1
			StringBuilder builder = new StringBuilder();
			builder.append(this.strings[0]);
			for (int i = 1; i <= this.valueCount; i++)
			{
				builder.append('\\').append(i);
				builder.append(this.strings[i]);
			}
			string = new StringValue(this.position, builder.toString());
		}
		else
		{
			string = new StringValue("");
		}

		final ArgumentList list = new ArgumentList(this.valueCount);
		list.addValue(string);
		for (int i = 0; i < this.valueCount; i++)
		{
			list.addValue(this.values[i]);
		}

		return new LiteralConversion(this, annotation, list).withType(type, typeContext, markers, context);
	}

	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(Types.STRING) || this.isConvertible(type);
	}

	private boolean isConvertible(IType type)
	{
		final IClass theClass = type.getTheClass();
		return theClass.getAnnotation(Types.STRING_CONVERTIBLE_CLASS) != null
			       || theClass.getAnnotation(LazyFields.STRING_INTERPOLATION_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		final int distance = Types.getDistance(type, Types.STRING);
		if (distance != 0)
		{
			return distance;
		}
		return this.isConvertible(type) ? CONVERSION_MATCH : 0;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			final IValue value = this.values[i];
			value.check(markers, context);

			if (value.getType() == Types.VOID)
			{
				markers.add(Markers.semantic(value.getPosition(), "string.interpolation.void"));
			}
		}
	}

	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		String string = this.strings[0];

		int estSize = string.length();
		for (int i = 0; i < this.valueCount; i++)
		{
			estSize += this.values[i].stringSize();
			estSize += this.strings[i + 1].length();
		}

		writer.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(estSize);
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V", false);

		CaseClasses.writeStringAppend(writer, string);

		for (int i = 0; i < this.valueCount; i++)
		{
			final IValue value = this.values[i];
			value.writeExpression(writer, null);
			CaseClasses.writeStringAppend(writer, value.getType());

			string = this.strings[i + 1];
			CaseClasses.writeStringAppend(writer, string);
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
		                       false);

		if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		String stringPart = this.strings[0];

		buffer.append('"');
		LexerUtil.appendStringLiteralBody(stringPart, buffer);

		for (int i = 0; i < this.valueCount; i++)
		{
			buffer.append('\\').append('(');
			this.values[i].toString(prefix, buffer);
			stringPart = this.strings[i + 1];
			buffer.append(')');

			LexerUtil.appendStringLiteralBody(stringPart, buffer);
		}
		buffer.append('"');
	}
}

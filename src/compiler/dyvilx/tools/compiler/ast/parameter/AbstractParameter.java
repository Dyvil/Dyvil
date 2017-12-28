package dyvilx.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.ast.type.compound.LambdaType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public abstract class AbstractParameter extends Variable implements IParameter
{
	protected @NonNull Name label;

	// Metadata
	protected @Nullable ICallableMember method;

	protected int index;

	private IType covariantType;

	public AbstractParameter()
	{
	}

	public AbstractParameter(Name name)
	{
		this(name, null);
	}

	public AbstractParameter(Name name, IType type)
	{
		super(name, type);
		this.label = name;
	}

	public AbstractParameter(ICallableMember callable, SourcePosition position, Name name, IType type)
	{
		super(position, name, type);
		this.label = name;
		this.method = callable;
	}

	public AbstractParameter(ICallableMember callable, SourcePosition position, Name name, IType type,
		                        AttributeList attributes)
	{
		super(position, name, type, attributes);
		this.label = name;
		this.method = callable;
	}

	@Override
	public Name getLabel()
	{
		return this.label;
	}

	@Override
	public void setLabel(Name label)
	{
		this.label = label;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.METHOD_PARAMETER;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.PARAMETER;
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.method.getEnclosingClass();
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
		this.covariantType = null;
	}

	@Override
	public boolean setReferenceType()
	{
		return false;
	}

	@Override
	public String getInternalName()
	{
		return this.name == null ? null : this.name.qualified;
	}

	@Override
	public String getQualifiedLabel()
	{
		return this.label == null ? "" : this.label.qualified;
	}

	@Override
	public IType getCovariantType()
	{
		if (this.covariantType != null)
		{
			return this.covariantType;
		}

		return this.covariantType = this.type.asParameterType();
	}

	@Override
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.method != null && this.method.hasModifier(Modifiers.GENERATED))
		{
			this.attributes.addFlag(Modifiers.GENERATED);
		}

		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		this.covariantType = null;

		final LambdaType functionType;
		if (this.type != null && (functionType = this.type.extract(LambdaType.class)) != null)
		{
			if (functionType.isExtension())
			{
				this.attributes.addFlag(Modifiers.INFIX_FLAG);
			}
			else if (this.attributes.hasFlag(Modifiers.INFIX_FLAG))
			{
				functionType.setExtension(true);
			}
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		IParameter.super.writeInit(writer, value);
	}

	@Override
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
		if (this.name != null)
		{
			super.writeLocal(writer, start, end);
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributes.toInlineString(indent, buffer);

		if (this.label != this.name)
		{
			appendNullableName(buffer, this.label);
			buffer.append(' ');
		}

		appendNullableName(buffer, this.name);

		final boolean varargs = this.isVarargs();
		if (varargs && !this.type.canExtract(ArrayType.class))
		{
			// non-array varargs
			buffer.append("...");
		}

		if (this.type != null && this.type != Types.UNKNOWN)
		{
			Formatting.appendSeparator(buffer, "parameter.type_ascription", ':');
			if (varargs && this.type.canExtract(ArrayType.class))
			{
				// array varargs
				this.type.extract(ArrayType.class).getElementType().toString(indent, buffer);
				buffer.append("...");
			}
			else
			{
				this.type.toString(indent, buffer);
			}
		}

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(indent, buffer);
		}
	}

	protected static void appendNullableName(@NonNull StringBuilder buffer, Name name)
	{
		if (name != null)
		{
			buffer.append(name);
		}
		else
		{
			buffer.append('_');
		}
	}
}

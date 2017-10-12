package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.generic.ClassGenericType;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public final class TypeOperator extends AbstractValue
{
	public static final class LazyFields
	{
		public static final IClass    TYPE_CLASS = Package.dyvilReflectTypes.resolveClass("Type");
		public static final ClassType TYPE       = new ClassType(TYPE_CLASS);

		public static final IClass TYPE_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                              .resolveClass(Name.fromRaw("FromType"));

		private LazyFields()
		{
			// no instances
		}
	}

	protected IType type;

	// Metadata
	private IType genericType;

	public TypeOperator(SourcePosition position)
	{
		this.position = position;
	}

	public TypeOperator(IType type)
	{
		this.setType(type);
	}

	@Override
	public int valueTag()
	{
		return TYPE_OPERATOR;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			return this.genericType = new ClassGenericType(LazyFields.TYPE_CLASS, this.type);
		}
		return this.genericType;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final Annotation annotation = type.getAnnotation(LazyFields.TYPE_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}

		return Types.isSuperType(type, this.getType()) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType()) || type.getAnnotation(LazyFields.TYPE_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(LazyFields.TYPE_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = dyvilx.tools.compiler.ast.type.builtin.Types.UNKNOWN;
			markers.add(Markers.semantic(this.position, "typeoperator.invalid"));
			return;
		}

		this.type = this.type.resolveType(markers, context);
		this.genericType = new ClassGenericType(LazyFields.TYPE_CLASS, this.type);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);

		if (type != null)
		{
			this.genericType.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return "type<" + this.type + ">";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("type<");
		this.type.toString(indent, buffer);
		buffer.append('>');
	}
}

package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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

	public TypeOperator(ICodePosition position)
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
			ClassGenericType generic = new ClassGenericType(LazyFields.TYPE_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IAnnotation annotation = type.getAnnotation(LazyFields.TYPE_CONVERTIBLE);
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
			this.type = dyvil.tools.compiler.ast.type.builtin.Types.UNKNOWN;
			markers.add(Markers.semantic(this.position, "typeoperator.invalid"));
			return;
		}

		this.type = this.type.resolveType(markers, context);
		ClassGenericType generic = new ClassGenericType(LazyFields.TYPE_CLASS);
		generic.addType(this.type);
		this.genericType = generic;
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
			this.genericType.writeCast(writer, type, this.getLineNumber());
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

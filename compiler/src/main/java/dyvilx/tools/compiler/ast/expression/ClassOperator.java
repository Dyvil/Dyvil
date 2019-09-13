package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.Type;
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
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public final class ClassOperator implements IValue
{
	public static final class LazyFields
	{
		public static final IClass    CLASS_CLASS = Package.javaLang.resolveClass("Class");
		public static final ClassType CLASS       = new ClassType(CLASS_CLASS);

		public static final IClass CLASS_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                               .resolveClass(Name.fromRaw("FromClass"));

		private LazyFields()
		{
			// no instances
		}
	}

	protected IType type;

	// Metadata
	private SourcePosition position;
	private IType          genericType;

	public ClassOperator(SourcePosition position)
	{
		this.position = position;
	}

	public ClassOperator(IType type)
	{
		this.setType(type);
	}

	@Override
	public int valueTag()
	{
		return CLASS_OPERATOR;
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

	@Override
	public boolean isResolved()
	{
		return true;
	}

	// Annotations

	@Override
	public boolean isAnnotationConstant()
	{
		return true;
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this;
	}

	// Typing

	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			return this.genericType = new ClassGenericType(LazyFields.CLASS_CLASS, this.type);
		}
		return this.genericType;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final Annotation annotation = type.getAnnotation(LazyFields.CLASS_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}

		return Types.isSuperType(type, this.getType()) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType()) || type.getAnnotation(LazyFields.CLASS_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		final int i = IValue.super.getTypeMatch(type, implicitContext);
		if (i != MISMATCH)
		{
			return i;
		}
		if (type.getAnnotation(LazyFields.CLASS_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return MISMATCH;
	}

	// Phases

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = dyvilx.tools.compiler.ast.type.builtin.Types.UNKNOWN;
			markers.add(Markers.semantic(this.position, "classoperator.invalid"));
			return;
		}

		this.type = this.type.resolveType(markers, context);
		this.genericType = new ClassGenericType(LazyFields.CLASS_CLASS, this.type);
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
		this.type.checkType(markers, context, TypePosition.CLASS | TypePosition.REIFY_FLAG);
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

	// Compilation

	@Override
	public int stringSize()
	{
		if (this.type.isPrimitive())
		{
			return this.type.getName().qualified.length();
		}

		IClass iClass = this.type.getTheClass();
		if (iClass == null)
		{
			return 20;
		}

		if (iClass.isInterface())
		{
			return "interface ".length() + iClass.getInternalName().length();
		}

		return "class ".length() + iClass.getInternalName().length();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		if (this.type.isPrimitive())
		{
			builder.append(this.type.getName().qualified);
			return true;
		}

		IClass iClass = this.type.getTheClass();
		if (iClass == null)
		{
			return false;
		}

		if (iClass.isInterface())
		{
			builder.append("interface ");
		}
		else
		{
			builder.append("class ");
		}

		builder.append(iClass.getFullName());
		return true;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.type.writeClassExpression(writer, false);

		if (type != null)
		{
			this.genericType.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		visitor.visit(key, Type.getObjectType(this.type.getInternalName()));
	}

	// String

	@Override
	public String toString()
	{
		return "class<" + this.type + ">";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("class<");
		this.type.toString(indent, buffer);
		buffer.append('>');
	}
}

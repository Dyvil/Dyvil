package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ClassOperator;
import dyvilx.tools.compiler.ast.expression.TypeOperator;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public class CodeTypeParameter extends TypeParameter
{
	protected SourcePosition position;

	public CodeTypeParameter(SourcePosition position, ITypeParametric generic, Name name, Variance variance)
	{
		super(generic, name, variance);
		this.position = position;
	}

	public CodeTypeParameter(SourcePosition position, ITypeParametric generic, Name name, Variance variance,
		AttributeList annotations)
	{
		super(generic, name, variance);
		this.attributes = annotations;
		this.position = position;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.attributes.resolveTypes(markers, context, this);

		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolveType(markers, context);
		}

		this.upperBound = this.upperBound.resolveType(markers, context);
		this.upperBounds = getUpperBounds(this.upperBound);

		// The first upper bound is meant to be a class bound.
		IType type = this.upperBounds[0];

		IClass typeClass = type.getTheClass();
		if (typeClass != null && !typeClass.isInterface())
		{
			// If the first type is a class type (not an interface), it becomes the erasure type.
			this.erasure = type;
		}

		// Check if the remaining upper bounds are interfaces
		for (int i = 1, count = this.upperBounds.length; i < count; i++)
		{
			type = this.upperBounds[i];
			typeClass = type.getTheClass();

			if (typeClass != null && !typeClass.isInterface())
			{
				final Marker marker = Markers.semanticError(type.getPosition(), "type_parameter.bound.class");
				marker.addInfo(Markers.getSemantic("class.declaration", Util.classSignatureToString(typeClass)));
				markers.add(marker);
			}
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.resolve(markers, context);
		}
		if (this.upperBound != null)
		{
			this.upperBound.resolve(markers, context);
		}

		this.attributes.resolve(markers, context);
		this.computeReifiedKind();
	}

	@Override
	protected void computeReifiedKind()
	{
		super.computeReifiedKind();

		final IType type;
		final Reified.Type reifiedKind = this.getReifiedKind();
		if (reifiedKind == Reified.Type.TYPE)
		{
			type = TypeOperator.LazyFields.TYPE;
		}
		else if (reifiedKind != null)
		{
			type = ClassOperator.LazyFields.CLASS;
		}
		else
		{
			return;
		}

		if (this.getReifiedKind() != null)
		{
			final AttributeList attributes = AttributeList
				                                 .of(Modifiers.MANDATED | Modifiers.SYNTHETIC | Modifiers.FINAL);
			final Name name = Name.apply("reify_" + this.getName().qualified);
			final CodeParameter parameter = new CodeParameter(null, this.getPosition(), name, type, attributes);
			this.setReifyParameter(parameter);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.attributes.checkTypes(markers, context);
		if (this.lowerBound != null)
		{
			this.lowerBound.checkType(markers, context, IType.TypePosition.SUPER_TYPE_ARGUMENT);
		}
		if (this.upperBound != null)
		{
			this.upperBound.checkType(markers, context, IType.TypePosition.SUPER_TYPE_ARGUMENT);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.attributes.check(markers, context, ElementType.TYPE_PARAMETER);
		if (this.lowerBound != null)
		{
			this.lowerBound.check(markers, context);
		}
		if (this.upperBound != null)
		{
			this.upperBound.check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		this.attributes.foldConstants();
		if (this.lowerBound != null)
		{
			this.lowerBound.foldConstants();
		}
		if (this.upperBound != null)
		{
			this.upperBound.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.attributes.cleanup(compilableList, classCompilableList);
		if (this.lowerBound != null)
		{
			this.lowerBound.cleanup(compilableList, classCompilableList);
		}
		if (this.upperBound != null)
		{
			this.upperBound.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void addBoundAnnotation(Annotation annotation, int index, TypePath typePath)
	{
	}

	@Override
	public void writeParameter(MethodWriter writer) throws BytecodeException
	{
		if (this.reifyParameter != null)
		{
			this.reifyParameter.writeParameter(writer);
		}
	}
}

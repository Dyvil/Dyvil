package dyvil.tools.compiler.ast.generic;

import dyvil.annotation.Reified;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ClassOperator;
import dyvil.tools.compiler.ast.expression.TypeOperator;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.lang.annotation.ElementType;

public class CodeTypeParameter extends TypeParameter
{
	protected SourcePosition position;
	protected CodeParameter reifyParameter;

	public CodeTypeParameter(SourcePosition position, ITypeParametric generic, Name name, Variance variance)
	{
		super(generic, name, variance);
		this.position = position;
	}

	public CodeTypeParameter(SourcePosition position, ITypeParametric generic, Name name, Variance variance, AnnotationList annotations)
	{
		super(generic, name, variance);
		this.annotations = annotations;
		this.position = position;
	}

	@Override
	public IParameter getReifyParameter()
	{
		return this.reifyParameter;
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
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}

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
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
			this.computeReifiedKind();
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}
		if (this.lowerBound != null)
		{
			this.lowerBound.checkType(markers, context, IType.TypePosition.SUPER_TYPE_ARGUMENT);
		}
		if (this.upperBound != null)
		{
			this.upperBound.checkType(markers, context, IType.TypePosition.SUPER_TYPE_ARGUMENT);
		}

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
			this.reifyParameter = new CodeParameter(Name.apply("reify_" + this.name.qualified), type);
			this.reifyParameter.getModifiers().addIntModifier(Modifiers.MANDATED | Modifiers.SYNTHETIC | Modifiers.FINAL);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, ElementType.TYPE_PARAMETER);
		}
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
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}
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
		if (this.annotations != null)
		{
			this.annotations.cleanup(compilableList, classCompilableList);
		}
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
	public void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath)
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

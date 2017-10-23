package dyvilx.tools.compiler.ast.attribute.annotation;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public class CodeAnnotation extends Annotation
{
	protected SourcePosition position;

	public CodeAnnotation(SourcePosition position)
	{
		this.position = position;
	}

	public CodeAnnotation(SourcePosition position, IType type)
	{
		this.position = position;
		this.type = type;
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
		if (this.type == null)
		{
			markers.add(Markers.semanticError(this.position, "annotation.type.invalid"));
			this.type = Types.UNKNOWN;
		}

		super.resolveTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		super.check(markers, context, target);

		if (this.type == null || !this.type.isResolved())
		{
			return;
		}

		final IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return;
		}

		if (!theClass.isAnnotation())
		{
			markers.add(Markers.semanticError(this.position, "annotation.type", this.type.getName()));
			return;
		}

		if (target == null)
		{
			return;
		}

		final IClassMetadata metadata = theClass.getMetadata();
		if (!metadata.isTarget(target))
		{
			final Marker error = Markers.semanticError(this.position, "annotation.target", this.type.getName());
			error.addInfo(Markers.getSemantic("annotation.target.element", target));
			error.addInfo(Markers.getSemantic("annotation.target.allowed", metadata.getTargets()));
			markers.add(error);
		}
	}
}

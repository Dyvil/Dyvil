package dyvilx.tools.compiler.ast.attribute.annotation;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;

public class ExternalAnnotation extends Annotation
{
	private boolean typeResolved;

	public ExternalAnnotation()
	{
	}

	public ExternalAnnotation(IType type)
	{
		super(type);
	}

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public IType getType()
	{
		if (this.typeResolved)
		{
			return this.type;
		}

		this.typeResolved = true;
		return this.type = this.type.resolveType(null, RootPackage.rootPackage);
	}
}

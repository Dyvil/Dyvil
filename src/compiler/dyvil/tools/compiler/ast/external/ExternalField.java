package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class ExternalField extends Field
{
	private static final int ANNOTATIONS = 1;
	private static final int RETURN_TYPE = 2;

	private int resolved;

	public ExternalField(IClass iclass, Name name, String desc, IType type, ModifierSet modifierSet)
	{
		super(iclass, name, type, modifierSet);
		this.descriptor = desc;
	}

	private IContext getCombiningContext()
	{
		return new CombiningContext(this.enclosingClass, Package.rootPackage);
	}

	private void resolveAnnotations()
	{
		this.resolved |= ANNOTATIONS;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, this.getCombiningContext(), this);
		}
	}

	private void resolveReturnType()
	{
		this.resolved |= RETURN_TYPE;
		this.type = this.type.resolveType(null, this.getCombiningContext());
	}

	@Override
	public IType getType()
	{
		if ((this.resolved & RETURN_TYPE) == 0)
		{
			this.resolveReturnType();
		}
		return this.type;
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}

		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		return this.annotations.getAnnotation(type);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}
}

package dyvilx.tools.compiler.ast.external;

import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

public class ExternalClassParameter extends ClassParameter
{
	private static final int TYPE        = 2;

	private byte resolved;

	public ExternalClassParameter(IClass enclosingClass, Name name, String desc, IType type, ModifierSet modifiers)
	{
		super(enclosingClass, name, type);
		this.descriptor = desc;
		if (modifiers != null)
		{
			this.modifiers = modifiers;
		}
	}

	private IContext getCombiningContext()
	{
		return new CombiningContext(this.enclosingClass, Package.rootPackage);
	}

	private void resolveAnnotations()
	{
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, RootPackage.rootPackage, this);
		}
	}

	private void resolveReturnType()
	{
		this.resolved |= TYPE;
		this.type = this.type.resolveType(null, this.getCombiningContext());
	}

	@Override
	public IType getType()
	{
		if ((this.resolved & TYPE) == 0)
		{
			this.resolveReturnType();
		}
		return this.type;
	}

	@Override
	public IType getCovariantType()
	{
		if ((this.resolved & TYPE) == 0)
		{
			this.resolveReturnType();
		}
		return super.getCovariantType();
	}

	@Override
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
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

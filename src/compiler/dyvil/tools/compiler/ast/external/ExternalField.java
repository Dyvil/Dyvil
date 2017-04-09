package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.structure.RootPackage;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class ExternalField extends Field
{
	private static final int RETURN_TYPE    = 1 << 1;
	private static final int CONSTANT_VALUE = 1 << 2;

	private int resolved = CONSTANT_VALUE;
	private Object constantValue;

	public ExternalField(IClass iclass, Name name, String desc, IType type, ModifierSet modifierSet)
	{
		super(iclass, name, type, modifierSet);
		this.descriptor = desc;
	}

	public Object getConstantValue()
	{
		return this.constantValue;
	}

	public void setConstantValue(Object constantValue)
	{
		this.constantValue = constantValue;
		this.resolved &= ~CONSTANT_VALUE;
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
		this.resolved |= RETURN_TYPE;
		this.type = this.type.resolveType(null, this.getCombiningContext());
	}

	private void resolveConstantValue()
	{
		if ((this.resolved & RETURN_TYPE) == 0)
		{
			this.resolveReturnType();
		}

		this.resolved |= CONSTANT_VALUE;

		final IValue value = IValue.fromObject(this.constantValue);
		if (value != null)
		{
			this.value = value.withType(this.type, null, null, this.getCombiningContext());
		}
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
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
	}

	@Override
	public IValue getValue()
	{
		if ((this.resolved & CONSTANT_VALUE) == 0)
		{
			this.resolveConstantValue();
		}
		return super.getValue();
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

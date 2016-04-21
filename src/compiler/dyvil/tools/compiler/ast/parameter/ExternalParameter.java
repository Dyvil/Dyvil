package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ExternalParameter extends AbstractParameter
{
	private boolean resolved;

	public ExternalParameter(Name name, IType type)
	{
		super(name, type);
	}

	private void resolveTypes()
	{
		this.resolved = true;

		this.resolveTypes(null, ((IExternalCallableMember) this.method).getExternalContext());
	}

	@Override
	public IType getType()
	{
		if (!this.resolved)
		{
			this.resolveTypes();
		}

		return super.getType();
	}

	@Override
	public IType getInternalType()
	{
		if (!this.resolved)
		{
			this.resolveTypes();
		}

		return super.getInternalType();
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

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
		assert false;
	}
}

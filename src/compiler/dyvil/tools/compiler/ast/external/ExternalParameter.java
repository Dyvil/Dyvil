package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.AbstractParameter;
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

	public ExternalParameter(Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(null, name, type, modifiers, annotations);
	}

	private void resolveTypes()
	{
		this.resolved = true;

		this.resolveTypes(null, ((IExternalCallableMember) this.method).getExternalContext());
	}

	public void addTypeAnnotation(IAnnotation annotation, TypePath path)
	{
		this.type = IType.withAnnotation(this.type, annotation, path);
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
	public int getLocalSlots()
	{
		// Do not perform type resolution
		return this.type.getLocalSlots();
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
		assert false;
	}
}

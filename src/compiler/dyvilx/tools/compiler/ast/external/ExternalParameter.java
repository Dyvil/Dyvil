package dyvilx.tools.compiler.ast.external;

import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.method.IExternalCallableMember;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.parameter.AbstractParameter;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

public class ExternalParameter extends AbstractParameter
{
	private boolean resolved;

	public ExternalParameter(ICallableMember callable, Name name, IType type)
	{
		super(callable, null, name, type);
	}

	public ExternalParameter(ICallableMember callable, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(callable, null, name, type, modifiers, annotations);
	}

	private void resolveTypes()
	{
		this.resolved = true;

		this.resolveTypes(null, ((IExternalCallableMember) this.method).getExternalContext());
	}

	private void resolveAnnotations()
	{
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, RootPackage.rootPackage, this);
		}
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
	public IType getCovariantType()
	{
		if (!this.resolved)
		{
			this.resolveTypes();
		}

		return super.getCovariantType();
	}

	@Override
	public int getLocalSlots()
	{
		// Do not perform type resolution
		return this.type.getLocalSlots();
	}

	@Override
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
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

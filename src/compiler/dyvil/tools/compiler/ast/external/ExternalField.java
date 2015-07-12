package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ExternalField extends Field
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	
	public ExternalField(IClass iclass, int access, Name name, IType type)
	{
		super(iclass, name, type);
		this.modifiers = access;
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveReturnType()
	{
		this.returnTypeResolved = true;
		this.type = this.type.resolve(null, this.theClass, TypePosition.RETURN_TYPE);
	}
	
	@Override
	public IType getType()
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		return this.type;
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return this.annotations[index];
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return super.getAnnotation(type);
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

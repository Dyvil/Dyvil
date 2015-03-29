package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ExternalConstructor extends Constructor
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalConstructor(IClass iclass)
	{
		super(iclass);
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
		this.type = this.type.resolve(null, Package.rootPackage);
	}
	
	private void resolveParameters()
	{
		this.parametersResolved = true;
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveExceptions()
	{
		this.exceptionsResolved = true;
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolve(null, Package.rootPackage);
		}
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
	public IType getType(ITypeContext context)
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		return this.type.getConcreteType(context);
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return this.parameters[index];
	}
	
	@Override
	public int getSignatureMatch(IArguments arguments)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getSignatureMatch(arguments);
	}
	
	@Override
	public IType getException(int index)
	{
		if (!this.exceptionsResolved)
		{
			this.resolveExceptions();
		}
		return this.exceptions[index];
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
	public Annotation getAnnotation(IType type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (a.type.equals(type))
			{
				return a;
			}
		}
		return null;
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

package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.IExternalMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.visitor.AnnotationVisitorImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ExternalConstructor extends Constructor implements IExternalMethod
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalConstructor(IClass iclass)
	{
		super(iclass);
		this.type = iclass.getType();
	}
	
	@Override
	public IParameter getParameter_(int index)
	{
		return this.parameters[index];
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, Package.rootPackage, this);
		}
	}
	
	private void resolveReturnType()
	{
		this.returnTypeResolved = true;
		this.type = this.theClass.getType();
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
			this.exceptions[i] = this.exceptions[i].resolveType(null, Package.rootPackage);
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
	public IParameter getParameter(int index)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return this.parameters[index];
	}
	
	@Override
	public float getSignatureMatch(IArguments arguments)
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
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
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
	
	@Override
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IArguments arguments)
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		super.checkArguments(markers, position, context, arguments);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.EXCEPTION_PARAMETER:
		{
			int index = TypeReference.getExceptionIndex(typeRef);
			this.exceptions[index] = IType.withAnnotation(this.exceptions[index], annotation, typePath, 0, typePath.getLength());
			break;
		}
		case TypeReference.METHOD_FORMAL_PARAMETER:
		{
			int index = TypeReference.getFormalParameterIndex(typeRef);
			IParameter param = this.parameters[index];
			param.setType(IType.withAnnotation(param.getType(), annotation, typePath, 0, typePath.getLength()));
		}
		}
		return new AnnotationVisitorImpl(null, annotation);
	}
}

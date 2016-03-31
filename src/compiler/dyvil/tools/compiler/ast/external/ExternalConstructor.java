package dyvil.tools.compiler.ast.external;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.AbstractConstructor;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ExternalConstructor extends AbstractConstructor implements IExternalCallableMember
{
	private boolean annotationsResolved;
	private boolean returnTypeResolved;
	private boolean parametersResolved;
	private boolean exceptionsResolved;
	
	public ExternalConstructor(IClass iclass)
	{
		super(iclass);
		this.type = iclass.getType();
	}
	
	@Override
	public IParameter getParameterNoResolve(int index)
	{
		return this.parameters[index];
	}

	@Override
	public void setIntrinsicData(IntrinsicData intrinsicData)
	{
	}

	@Override
	public InitializerCall getInitializer()
	{
		return null;
	}

	@Override
	public void setInitializer(InitializerCall initializer)
	{
	}

	@Override
	public IValue getValue()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{
	}

	private IContext getCombiningContext()
	{
		return new CombiningContext(this, new CombiningContext(this.enclosingClass, Package.rootPackage));
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, this.getCombiningContext(), this);
		}
	}
	
	private void resolveReturnType()
	{
		this.returnTypeResolved = true;
		this.type = this.enclosingClass.getType();
	}
	
	private void resolveParameters()
	{
		final IContext context = this.getCombiningContext();

		this.parametersResolved = true;
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, context);
		}
	}
	
	private void resolveExceptions()
	{
		final IContext context = this.getCombiningContext();

		this.exceptionsResolved = true;
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(null, context);
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
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments)
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		super.checkArguments(markers, position, context, type, arguments);
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
	public void write(ClassWriter writer) throws BytecodeException
	{
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
			this.exceptions[index] = IType
					.withAnnotation(this.exceptions[index], annotation, typePath, 0, typePath.getLength());
			break;
		}
		case TypeReference.METHOD_FORMAL_PARAMETER:
		{
			int index = TypeReference.getFormalParameterIndex(typeRef);
			IParameter param = this.parameters[index];
			param.setType(IType.withAnnotation(param.getType(), annotation, typePath, 0, typePath.getLength()));
		}
		}
		return new AnnotationReader(null, annotation);
	}
}

package dyvil.tools.compiler.ast.external;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IntValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.ArrayValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ExternalMethod extends Method
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	genericsResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalMethod(IClass iclass)
	{
		super(iclass);
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation annotation = this.annotations[i];
			annotation.resolveTypes(null, Package.rootPackage);
			
			if (annotation.type.getTheClass() != Types.AIntrinsic.theClass)
			{
				continue;
			}
			
			try
			{
				ArrayValue array = (ArrayValue) annotation.arguments.getValue(0, Annotation.VALUE);
				
				int len = array.valueCount();
				int[] opcodes = new int[len];
				for (int j = 0; j < len; j++)
				{
					IntValue v = (IntValue) array.getValue(j);
					opcodes[j] = v.value;
				}
				this.intrinsicOpcodes = opcodes;
			}
			catch (NullPointerException | ClassCastException ex)
			{
			}
		}
	}
	
	private void resolveReturnType()
	{
		this.returnTypeResolved = true;
		this.type = this.type.resolve(null, this);
	}
	
	private void resolveGenerics()
	{
		this.genericsResolved = true;
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveParameters()
	{
		this.parametersResolved = true;
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, this);
		}
	}
	
	private void resolveExceptions()
	{
		this.exceptionsResolved = true;
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolve(null, this);
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
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this.generics[index];
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
	public int getSignatureMatch(Name name, IValue instance, IArguments arguments)
	{
		if (name != this.name)
		{
			return 0;
		}
		
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getSignatureMatch(name, instance, arguments);
	}
	
	@Override
	public IValue checkArguments(MarkerList markers, IValue instance, IArguments arguments, ITypeContext typeContext)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return super.checkArguments(markers, instance, arguments, typeContext);
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
	public IClass resolveClass(Name name)
	{
		return Package.rootPackage.resolveClass(name);
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
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeCall(writer, instance, arguments, type);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeJump(writer, dest, instance, arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		super.writeInvJump(writer, dest, instance, arguments);
	}
}

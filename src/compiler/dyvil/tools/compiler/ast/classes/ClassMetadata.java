package dyvil.tools.compiler.ast.classes;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.ClassParameterSetter;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int	CONSTRUCTOR	= 1;
	protected static final int	APPLY		= 2;
	protected static final int	EQUALS		= 4;
	protected static final int	HASHCODE	= 8;
	protected static final int	TOSTRING	= 16;
	
	protected final IClass theClass;
	
	protected IConstructor	constructor;
	protected IConstructor	superConstructor;
	
	protected byte methods;
	
	public ClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IConstructor getConstructor()
	{
		return this.constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		IClassBody body = this.theClass.getBody();
		if (body != null && body.constructorCount() > 0)
		{
			IConstructor c = body.getConstructor(this.theClass.getParameters(), this.theClass.parameterCount());
			if (c != null)
			{
				this.constructor = c;
				this.methods |= CONSTRUCTOR;
				return;
			}
			
			if (this.theClass.parameterCount() == 0)
			{
				this.methods |= CONSTRUCTOR;
				return;
			}
		}
		
		Constructor constructor = new Constructor(this.theClass);
		constructor.type = this.theClass.getType();
		constructor.modifiers = Modifiers.PUBLIC;
		
		int parameterCount = this.theClass.parameterCount();
		
		IParameter[] parameters = this.theClass.getParameters();
		constructor.setParameters(parameters, parameterCount);
		
		if (parameterCount > 0 && parameters[parameterCount - 1].isVarargs())
		{
			constructor.setVarargs();
		}
		
		this.constructor = constructor;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}
		
		IType superType = this.theClass.getSuperType();
		if (superType == null)
		{
			return;
		}
		
		IConstructor match = IContext.resolveConstructor(superType, EmptyArguments.INSTANCE);
		if (match != null)
		{
			this.superConstructor = match;
			return;
		}
		
		markers.add(this.theClass.getPosition(), "constructor.super", superType.toString());
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}
		
		float match = this.constructor.getSignatureMatch(arguments);
		if (match > 0)
		{
			list.add(new ConstructorMatch(this.constructor, match));
		}
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}
		
		StatementList list = new StatementList();
		if (instanceFields != null)
		{
			list.addValue(instanceFields);
		}
		if (this.superConstructor != null)
		{
			list.addValue(new InitializerCall(null, this.superConstructor, EmptyArguments.INSTANCE, true));
		}
		int count = this.theClass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = this.theClass.getParameter(i);
			list.addValue(new ClassParameterSetter(this.theClass, param));
		}
		
		this.constructor.setValue(list);
		this.constructor.write(writer, instanceFields);
	}
}

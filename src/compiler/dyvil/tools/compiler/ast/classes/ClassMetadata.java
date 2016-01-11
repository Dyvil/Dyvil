package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.ClassParameterSetter;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int CONSTRUCTOR = 1;
	protected static final int APPLY       = 2;
	protected static final int EQUALS      = 4;
	protected static final int HASHCODE    = 8;
	protected static final int TOSTRING    = 16;
	
	protected static final int WRITE_OBJECT  = 32;
	protected static final int WRITE_REPLACE = 64;
	protected static final int READ_OBJECT   = 128;
	protected static final int READ_RESOLVE  = 256;
	
	protected final IClass theClass;
	
	protected IConstructor constructor;
	protected IValue superInitializer;
	
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
	
	protected void checkMethods()
	{
		IClassBody body = this.theClass.getBody();
		if (body != null)
		{
			int count = body.methodCount();
			for (int i = 0; i < count; i++)
			{
				this.checkMethod(body.getMethod(i));
			}
		}
	}
	
	private void checkMethod(IMethod method)
	{
		Name name = method.getName();
		if (name == Names.equals)
		{
			if (method.parameterCount() == 1 && method.getParameter(0).getType().isSameType(Types.OBJECT))
			{
				this.methods |= EQUALS;
			}
			return;
		}
		if (name == Names.hashCode)
		{
			if (method.parameterCount() == 0)
			{
				this.methods |= HASHCODE;
			}
			return;
		}
		if (name == Names.toString)
		{
			if (method.parameterCount() == 0)
			{
				this.methods |= TOSTRING;
			}
			return;
		}
		if (name == Names.apply)
		{
			if (method.parameterCount() == this.theClass.parameterCount())
			{
				final int len = this.theClass.parameterCount();
				for (int i = 0; i < len; i++)
				{
					final IType methodParameterType = method.getParameter(i).getType();
					final IType classParameterType = this.theClass.getParameter(i).getType();
					if (!methodParameterType.isSameType(classParameterType))
					{
						return;
					}
				}
				
				this.methods |= APPLY;
			}
			return;
		}
		if (name == Names.readResolve || name == Names.writeReplace)
		{
			if (method.parameterCount() == 0 && method.getType().isSameType(Types.OBJECT))
			{
				this.methods |= name == Names.writeReplace ? WRITE_REPLACE : READ_RESOLVE;
				return;
			}
			return;
		}
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.theClass.getSuperConstructorArguments().resolveTypes(markers, context);
	}
	
	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		IClassBody body = this.theClass.getBody();
		if (body != null && body.constructorCount() > 0)
		{
			final IConstructor constructor = body
					.getConstructor(this.theClass.getParameters(), this.theClass.parameterCount());
			if (constructor != null)
			{
				this.constructor = constructor;
				this.methods |= CONSTRUCTOR;
				return;
			}
			
			if (this.theClass.parameterCount() == 0)
			{
				this.methods |= CONSTRUCTOR;
				return;
			}
		}
		
		Constructor constructor = new Constructor(this.theClass, new FlagModifierSet(Modifiers.PUBLIC));
		int parameterCount = this.theClass.parameterCount();

		IParameter[] parameters = new IParameter[parameterCount];

		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter classParameter = this.theClass.getParameter(i);
			parameters[i] = new MethodParameter(classParameter.getPosition(), classParameter.getName(),
			                                    classParameter.getType(), classParameter.getModifiers());
			parameters[i].setIndex(i);
		}
		//*/

		constructor.setParameters(parameters, parameterCount);
		
		if (parameterCount > 0 && parameters[parameterCount - 1].isVarargs())
		{
			constructor.setVarargs();
		}
		
		constructor.resolveTypes(markers, context);
		this.constructor = constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}
		
		final IType superType = this.theClass.getSuperType();
		if (superType == null)
		{
			return;
		}

		final StatementList constructorBody = new StatementList();
		final InitializerCall initializerCall = new InitializerCall(this.theClass.getPosition(), null,
		                                                            this.theClass.getSuperConstructorArguments(), true);

		this.superInitializer = initializerCall.resolve(markers, this.constructor);
		constructorBody.addValue(this.superInitializer);

		for (int i = 0, count = this.theClass.parameterCount(); i < count; i++)
		{
			IParameter param = this.constructor.getParameter(i);
			constructorBody.addValue(new ClassParameterSetter(this.theClass, param));
		}

		this.constructor.setValue(constructorBody);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if ((this.methods & CONSTRUCTOR) == 0)
		{
			this.superInitializer.checkTypes(markers, this.constructor);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if ((this.methods & CONSTRUCTOR) == 0)
		{
			this.superInitializer.check(markers, this.constructor);
		}
	}

	@Override
	public void foldConstants()
	{
		if ((this.methods & CONSTRUCTOR) == 0)
		{
			this.superInitializer.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if ((this.methods & CONSTRUCTOR) == 0)
		{
			this.superInitializer.cleanup(this.constructor, compilableList);
		}
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}
		
		float match = this.constructor.getSignatureMatch(arguments);
		if (match > 0)
		{
			list.add(this.constructor, match);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if ((this.methods & CONSTRUCTOR) != 0)
		{
			return;
		}

		this.constructor.write(writer);
	}
}

package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.ClassParameterSetter;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.constructor.CodeConstructor;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int CONSTRUCTOR = 0x1;
	protected static final int APPLY       = 0x2;
	protected static final int EQUALS      = 0x4;
	protected static final int HASHCODE    = 0x8;
	protected static final int TOSTRING    = 0x10;

	protected static final int READ_RESOLVE  = 0x1001;
	protected static final int WRITE_REPLACE = 0x1002;
	// protected static final int WRITE_OBJECT  = 0x1004;
	// protected static final int READ_OBJECT   = 0x1008;

	protected static final int INSTANCE_FIELD = 0x2001;

	protected final IClass theClass;

	protected IConstructor    constructor;
	protected InitializerCall superInitializer;

	protected int members;

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
			if (method.parameterCount() == 1 && method.getParameter(0).getInternalType().getTheClass() == Types.OBJECT_CLASS)
			{
				this.members |= EQUALS;
			}
			return;
		}
		if (name == Names.hashCode)
		{
			if (method.parameterCount() == 0)
			{
				this.members |= HASHCODE;
			}
			return;
		}
		if (name == Names.toString)
		{
			if (method.parameterCount() == 0)
			{
				this.members |= TOSTRING;
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
					if (!Types.isSameType(methodParameterType, classParameterType))
					{
						return;
					}
				}

				this.members |= APPLY;
			}
			return;
		}
		if (name == Names.readResolve || name == Names.writeReplace)
		{
			if (method.parameterCount() == 0 && method.getType().getTheClass() == Types.OBJECT_CLASS)
			{
				this.members |= name == Names.writeReplace ? WRITE_REPLACE : READ_RESOLVE;
			}
		}
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		final IArguments superConstructorArguments = this.theClass.getSuperConstructorArguments();
		if (superConstructorArguments != null)
		{
			superConstructorArguments.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		// Check if a constructor needs to be generated

		final IClassBody body = this.theClass.getBody();
		if (body != null && body.constructorCount() > 0)
		{
			final IConstructor constructor = body.getConstructor(this.theClass.getParameters(),
			                                                     this.theClass.parameterCount());
			if (constructor != null)
			{
				this.constructor = constructor;
				this.members |= CONSTRUCTOR;
			}

			if (this.theClass.parameterCount() == 0)
			{
				this.members |= CONSTRUCTOR;
			}
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) != 0)
		{
			return;
		}

		// Generate the constructor signature

		CodeConstructor constructor = new CodeConstructor(this.theClass, new FlagModifierSet(Modifiers.PUBLIC));
		int parameterCount = this.theClass.parameterCount();

		IParameter[] parameters = new IParameter[parameterCount];

		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter classParameter = this.theClass.getParameter(i);
			parameters[i] = new MethodParameter(classParameter.getPosition(), classParameter.getName(),
			                                    classParameter.getType(), classParameter.getModifiers(),
			                                    classParameter.getAnnotations());
			parameters[i].setIndex(i);
		}

		constructor.setParameters(parameters, parameterCount);

		if (parameterCount > 0 && parameters[parameterCount - 1].isVarargs())
		{
			constructor.setVariadic();
		}

		constructor.resolveTypes(markers, context);
		this.constructor = constructor;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) != 0)
		{
			return;
		}

		// Generate the constructor body

		final IType superType = this.theClass.getSuperType();
		if (superType == null)
		{
			return;
		}

		this.superInitializer = (InitializerCall) new InitializerCall(this.theClass.getPosition(), null,
		                                                              this.theClass.getSuperConstructorArguments(),
		                                                              true).resolve(markers, this.constructor);
		this.constructor.setInitializer(this.superInitializer);

		final StatementList constructorBody = new StatementList();
		for (int i = 0, count = this.theClass.parameterCount(); i < count; i++)
		{
			constructorBody.addValue(new ClassParameterSetter(this.theClass, this.constructor.getParameter(i)));
		}

		this.constructor.setValue(constructorBody);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.checkTypes(markers, this.constructor);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.checkNoError(markers, this.constructor);
		}
	}

	@Override
	public void foldConstants()
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.cleanup(this.constructor, compilableList);
		}
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		if ((this.members & CONSTRUCTOR) != 0)
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
		if ((this.members & CONSTRUCTOR) != 0)
		{
			return;
		}

		this.constructor.write(writer);
	}
}

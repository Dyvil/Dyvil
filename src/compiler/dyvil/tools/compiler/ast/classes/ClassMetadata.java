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
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int	CONSTRUCTOR	= 1;
	protected static final int	APPLY		= 2;
	protected static final int	EQUALS		= 4;
	protected static final int	HASHCODE	= 8;
	protected static final int	TOSTRING	= 16;
	
	protected static final int	WRITE_OBJECT	= 32;
	protected static final int	WRITE_REPLACE	= 64;
	protected static final int	READ_OBJECT		= 128;
	protected static final int	READ_RESOLVE	= 256;
	
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
	
	private void checkMethod(IMethod m)
	{
		Name name = m.getName();
		if (name == Names.equals)
		{
			if (m.parameterCount() == 1 && m.getParameter(0).getType().equals(Types.OBJECT))
			{
				this.methods |= EQUALS;
			}
			return;
		}
		if (name == Names.hashCode)
		{
			if (m.parameterCount() == 0)
			{
				this.methods |= HASHCODE;
			}
			return;
		}
		if (name == Names.toString)
		{
			if (m.parameterCount() == 0)
			{
				this.methods |= TOSTRING;
			}
			return;
		}
		if (name == Names.apply)
		{
			if (m.parameterCount() == this.theClass.parameterCount())
			{
				int len = this.theClass.parameterCount();
				for (int i = 0; i < len; i++)
				{
					IType t1 = m.getParameter(i).getType();
					IType t2 = m.getParameter(i).getType();
					if (!t1.equals(t2))
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
			if (m.parameterCount() == 0 && m.getType().getTheClass() == Types.OBJECT_CLASS)
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
	}
	
	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
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
		
		Constructor constructor = new Constructor(this.theClass, Modifiers.PUBLIC);
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
	public void resolve(MarkerList markers, IContext context)
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
		
		markers.add(I18n.createMarker(this.theClass.getPosition(), "constructor.super", superType.toString()));
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
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

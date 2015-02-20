package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.IParameterized;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class LambdaValue extends ASTNode implements IValue, IValued, IParameterized, IContext
{
	public static final Handle	BOOTSTRAP	= new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
													"(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;" + "Ljava/lang/invoke/MethodType;"
															+ "Ljava/lang/invoke/MethodType;" + "Ljava/lang/invoke/MethodHandle;"
															+ "Ljava/lang/invoke/MethodType;)" + "Ljava/lang/invoke/CallSite;");
	public List<Parameter>		parameters;
	public IValue				value;
	
	protected IType				type;
	protected IMethod			method;
	
	private IContext			context;
	
	public LambdaValue(ICodePosition position)
	{
		this.position = position;
		this.parameters = new ArrayList();
	}
	
	public LambdaValue(ICodePosition position, Parameter parameter)
	{
		this.position = position;
		this.parameters = new ArrayList(1);
		this.parameters.add(parameter);
	}
	
	public LambdaValue(ICodePosition position, List<Parameter> parameters)
	{
		this.position = position;
		this.parameters = parameters;
	}
	
	@Override
	public int getValueType()
	{
		return LAMBDA;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public void addParameter(Parameter parameter)
	{
		this.parameters.add(parameter);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type == null ? Type.NONE : this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			IMethod method = iclass.getFunctionalMethod();
			if (method != null)
			{
				if (this.parameters.size() == 1)
				{
					Parameter param = this.parameters.get(0);
					if (param.type == null)
					{
						param.type = method.getParameters().get(0).type;
					}
				}
				
				this.type = type;
				this.method = method;
				return this;
			}
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			IMethod method = iclass.getFunctionalMethod();
			if (method != null)
			{
				// TODO
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (Parameter p : this.parameters)
		{
			if (p.type != null)
			{
				p.type = p.type.resolve(markers, context);
			}
		}
		
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.context = context;
		this.value = this.value.resolve(markers, this);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.value.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IType getThisType()
	{
		return this.context.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.method != null)
		{
			for (Parameter param : this.parameters)
			{
				if (param.isName(name))
				{
					return new FieldMatch(param, 1);
				}
			}
		}
		
		// TODO Capturing Variables
		
		return this.context.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.parameters.size();
		if (len == 0)
		{
			buffer.append(Formatting.Method.emptyParameters);
		}
		else if (len == 1)
		{
			Parameter param = this.parameters.get(0);
			if (param.type != null)
			{
				buffer.append('(');
				param.toString(prefix, buffer);
				buffer.append(')');
			}
			else
			{
				buffer.append(param.name);
			}
		}
		else
		{
			Util.astToString(this.parameters, Formatting.Method.parameterSeperator, buffer);
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.value.toString(prefix, buffer);
	}
}

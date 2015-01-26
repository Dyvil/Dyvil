package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.IParameterized;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class LambdaValue extends ASTNode implements IValue, IValued, IParameterized
{
	public static final Handle	lambdaMetafactory	= new Handle(
															Opcodes.H_INVOKESTATIC,
															"java/lang/invoke/LambdaMetafactory",
															"metafactory",
															"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/Callsite;");
	
	public List<Parameter>		parameters;
	public IValue				value;
	
	protected IType				type;
	protected IMethod			method;
	
	public LambdaValue(ICodePosition position)
	{
		this.position = position;
		this.parameters = new ArrayList();
	}
	
	public LambdaValue(ICodePosition position, List<Parameter> parameters)
	{
		this.position = position;
		this.parameters = parameters;
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
	public void setVarargs()
	{
	}
	
	@Override
	public boolean isVarargs()
	{
		return false;
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
	public int getValueType()
	{
		return LAMBDA;
	}
	
	@Override
	public boolean requireType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass != null)
		{
			IMethod method = iclass.getFunctionalMethod();
			if (method != null)
			{
				this.type = type;
				this.method = method;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (Parameter p : this.parameters)
		{
			p.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
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
			this.parameters.get(0).toString(prefix, buffer);
		}
		else
		{
			Util.astToString(this.parameters, Formatting.Method.parameterSeperator, buffer);
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.value.toString(prefix, buffer);
	}
}

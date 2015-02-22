package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public final class LambdaValue extends ASTNode implements IValue, IBaseMethod
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
	private int					index;
	
	private String				owner;
	private String				name;
	private String				desc;
	private IType				returnType;
	private List<IVariable>		capturedFields;
	
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
		if (this.type == null)
		{
			LambdaType lt = new LambdaType();
			for (Parameter param : this.parameters)
			{
				lt.argumentTypes.add(param.type == null ? Type.NONE : param.type);
			}
			lt.returnType = this.value.getType();
			this.type = lt;
			return lt;
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass == null)
		{
			return null;
		}
		IMethod method = iclass.getFunctionalMethod();
		if (method == null)
		{
			return null;
		}
		
		List<Parameter> params = method.getParameters();
		int len = this.parameters.size();
		if (len != params.size())
		{
			return null;
		}
		for (int i = 0; i < len; i++)
		{
			Parameter lambdaParam = this.parameters.get(i);
			Parameter param = params.get(i);
			if (lambdaParam.type == null)
			{
				lambdaParam.type = param.type;
				continue;
			}
			if (!param.type.equals(lambdaParam.type))
			{
				return null;
			}
		}
		
		this.type = type;
		this.method = method;
		return this;
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
		IClass iclass = context.getThisType().getTheClass();
		if (iclass != null)
		{
			this.owner = iclass.getInternalName();
			ClassBody body = iclass.getBody();
			if (body != null)
			{
				body.addLambda(this);
				this.index = body.lambdas.size();
			}
		}
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.method.hasTypeVariables())
		{
			Map<String, IType> typeArguments = new HashMap();
			this.type.addTypeVariables(null, typeArguments);
			this.returnType = this.method.getType(typeArguments);
		}
		else
		{
			this.returnType = this.method.getType();
		}
		
		this.context = context;
		this.value = this.value.resolve(markers, this);
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
		// TODO Capture this
		
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
		
		FieldMatch match = this.context.resolveField(name);
		if (match != null && match.theField instanceof IVariable)
		{
			IVariable var = (IVariable) match.theField;
			if (this.capturedFields == null)
			{
				this.capturedFields = new ArrayList();
				this.capturedFields.add(var);
				return match;
			}
			if (!this.capturedFields.contains(var))
			{
				this.capturedFields.add(var);
			}
		}
		
		return match;
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
		int len = 0;
		if (this.capturedFields != null)
		{
			len = this.capturedFields.size();
			for (int i = 0; i < len; i++)
			{
				this.capturedFields.get(i).writeGet(writer, null);
			}
		}
		
		String desc = this.getDescriptor();
		String name = this.method.getQualifiedName();
		jdk.internal.org.objectweb.asm.Type type = jdk.internal.org.objectweb.asm.Type.getMethodType(this.method.getDescriptor());
		Handle handle = new Handle(Opcodes.H_INVOKESTATIC, this.owner, this.name, this.desc);
		writer.visitInvokeDynamicInsn(name, desc, len, this.type, BOOTSTRAP, type, handle, type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	private String getDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (this.capturedFields != null)
		{
			for (IVariable var : this.capturedFields)
			{
				var.getType().appendExtendedName(buffer);
			}
		}
		buffer.append(')');
		this.type.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	private String getDescriptor(List<Parameter> params, IType returnType)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (this.capturedFields != null)
		{
			for (IVariable var : this.capturedFields)
			{
				var.getType().appendExtendedName(buffer);
			}
		}
		for (Parameter par : params)
		{
			par.type.appendExtendedName(buffer);
		}
		buffer.append(')');
		returnType.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		List<Parameter> params = this.method.getParameters();
		this.name = "lambda$" + this.index;
		this.desc = this.getDescriptor(params, this.returnType);
		// TODO Instance Lambdas (capturing this)
		// TODO Exceptions
		MethodWriter mw = new MethodWriter(writer, writer.visitMethod(Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC, this.name, this.desc, null,
				null));
		
		// Updated Captured Field Indexes
		
		int len = 0;
		int[] prevIndex = null;
		
		if (this.capturedFields != null)
		{
			len = this.capturedFields.size();
			prevIndex = new int[len];
			for (int i = 0; i < len; i++)
			{
				IVariable var = this.capturedFields.get(i);
				prevIndex[i] = var.getIndex();
				var.setIndex(i);
				
				mw.visitParameter(var.getQualifiedName(), var.getType(), i);
			}
		}
		
		int len1 = params.size();
		for (int i = 0; i < len1; i++)
		{
			Parameter param = params.get(i);
			param.index = i + len;
			mw.visitParameter(param.qualifiedName, param.type, param.index);
		}
		
		// Write the Value
		
		mw.visitCode();
		this.value.writeExpression(mw);
		mw.visitEnd(this.method.getType());
		
		// Reset Captured Field Indexes
		
		for (int i = 0; i < len; i++)
		{
			IVariable var = this.capturedFields.get(i);
			var.setIndex(prevIndex[i]);
		}
		
		for (int i = 0; i < len1; i++)
		{
			Parameter param = params.get(i);
			param.index = i;
		}
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
			Util.astToString(prefix, this.parameters, Formatting.Method.parameterSeperator, buffer);
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.value.toString(prefix, buffer);
	}
}

package dyvil.tools.compiler.ast.value;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.LambdaParameter;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class LambdaValue extends ASTNode implements IValue, IValued, IClassCompilable, IContext, ITypeContext
{
	public static final Handle	BOOTSTRAP	= new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
													"(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;" + "Ljava/lang/invoke/MethodType;"
															+ "Ljava/lang/invoke/MethodType;" + "Ljava/lang/invoke/MethodHandle;"
															+ "Ljava/lang/invoke/MethodType;)" + "Ljava/lang/invoke/CallSite;");
	public LambdaParameter[]	parameters;
	public int					parameterCount;
	public IValue				value;
	
	protected IType				type;
	protected IMethod			method;
	
	private IContext			context;
	private int					index;
	
	private String				owner;
	private String				name;
	private String				desc;
	private IType				returnType;
	private IVariable[]			capturedFields;
	private int					capturedFieldCount;
	private IType				thisType;
	
	public LambdaValue(ICodePosition position)
	{
		this.position = position;
		this.parameters = new LambdaParameter[2];
	}
	
	public LambdaValue(ICodePosition position, LambdaParameter param)
	{
		this.position = position;
		this.parameters = new LambdaParameter[1];
		this.parameters[0] = param;
		this.parameterCount = 1;
	}
	
	public LambdaValue(ICodePosition position, LambdaParameter[] params)
	{
		this.position = position;
		this.parameters = params;
		this.parameterCount = params.length;
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
			for (int i = 0; i < this.parameterCount; i++)
			{
				IType t = this.parameters[i].type;
				lt.addType(t == null ? Type.NONE : t);
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
		return this.isType(type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass == null)
		{
			return false;
		}
		IMethod method = iclass.getFunctionalMethod();
		if (method == null)
		{
			return false;
		}
		
		if (this.parameterCount != method.parameterCount())
		{
			return false;
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter lambdaParam = this.parameters[i];
			Parameter param = method.getParameter(i);
			if (lambdaParam.type == null)
			{
				lambdaParam.type = param.type;
				continue;
			}
			if (!param.type.equals(lambdaParam.type))
			{
				return false;
			}
		}
		
		this.type = type;
		this.method = method;
		return true;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	@Override
	public IType resolveType(String name)
	{
		IType type = this.type.resolveType(name);
		if (type == null)
		{
			System.out.println("what now? " + name);
		}
		return type;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter param = this.parameters[i];
			if (param.type != null)
			{
				param.type = param.type.resolve(markers, context);
			}
		}
		
		this.context = context;
		this.value.resolveTypes(markers, this);
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
				this.index = body.lambdas.size() - 1;
			}
		}
		
		// Value gets resolved in check()
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.method != null)
		{
			if (this.method.hasTypeVariables())
			{
				this.returnType = this.method.getType(this);
				
				for (int i = 0; i < this.parameterCount; i++)
				{
					LambdaParameter param = this.parameters[i];
					param.baseType = method.getParameter(i).type;
					param.type = param.type.getConcreteType(this);
				}
			}
			else
			{
				this.returnType = this.method.getType();
			}
		}
		else
		{
			markers.add(Markers.create(this.position, "lambda.method"));
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
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IType getThisType()
	{
		return this.thisType = this.context.getThisType();
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
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter param = this.parameters[i];
			if (param.isName(name))
			{
				return new FieldMatch(param, 1);
			}
		}
		
		FieldMatch match = this.context.resolveField(name);
		if (match != null && match.theField instanceof IVariable)
		{
			IVariable var = (IVariable) match.theField;
			if (this.capturedFields == null)
			{
				this.capturedFields = new IVariable[2];
				this.capturedFields[0] = var;
				this.capturedFieldCount = 1;
				return match;
			}
			
			// Check if the variable is already in the array
			for (int i = 0; i < this.capturedFieldCount; i++)
			{
				if (this.capturedFields[i] == var)
				{
					// If yes, return the match and skip adding the variable
					// again.
					return match;
				}
			}
			
			int index = this.capturedFieldCount++;
			if (this.capturedFieldCount > this.capturedFields.length)
			{
				IVariable[] temp = new IVariable[this.capturedFieldCount];
				System.arraycopy(this.capturedFields, 0, temp, 0, index);
				this.capturedFields = temp;
			}
			this.capturedFields[index] = var;
		}
		
		return match;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
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
		int len;
		int handleType;
		if (this.thisType != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, 0, this.thisType);
			handleType = Opcodes.H_INVOKESPECIAL;
			len = 1 + this.capturedFieldCount;
		}
		else
		{
			handleType = Opcodes.H_INVOKESTATIC;
			len = this.capturedFieldCount;
		}
		
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].writeGet(writer, null);
		}
		
		String desc = this.getInvokeDescriptor();
		String name = this.method.getQualifiedName();
		jdk.internal.org.objectweb.asm.Type type1 = jdk.internal.org.objectweb.asm.Type.getMethodType(this.method.getDescriptor());
		jdk.internal.org.objectweb.asm.Type type2 = jdk.internal.org.objectweb.asm.Type.getMethodType(this.getSpecialDescriptor());
		Handle handle = new Handle(handleType, this.owner, this.name, this.desc);
		writer.visitInvokeDynamicInsn(name, desc, len, this.type, BOOTSTRAP, type1, handle, type2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	private String getInvokeDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (this.thisType != null)
		{
			this.thisType.appendExtendedName(buffer);
		}
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].getType().appendExtendedName(buffer);
		}
		buffer.append(')');
		this.type.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	private String getSpecialDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].type.appendExtendedName(buffer);
		}
		buffer.append(')');
		this.returnType.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	private String getLambdaDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].getType().appendExtendedName(buffer);
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].type.appendExtendedName(buffer);
		}
		buffer.append(')');
		this.returnType.appendExtendedName(buffer);
		return buffer.toString();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		// TODO Exceptions
		
		this.name = "lambda$" + this.index;
		this.desc = this.getLambdaDescriptor();
		
		boolean instance = this.thisType != null;
		int modifiers = instance ? Modifiers.PRIVATE | Modifiers.SYNTHETIC : Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		MethodWriter mw = new MethodWriter(writer, writer.visitMethod(modifiers, this.name, this.desc, null, null));
		
		// Updated Captured Field Indexes
		
		int[] prevIndex = null;
		
		if (instance)
		{
			mw.addLocal(this.thisType);
		}
		
		if (this.capturedFieldCount > 0)
		{
			prevIndex = new int[this.capturedFieldCount];
			for (int i = 0; i < this.capturedFieldCount; i++)
			{
				IVariable var = this.capturedFields[i];
				prevIndex[i] = var.getIndex();
				var.setIndex(mw.visitParameter(var.getQualifiedName(), var.getType()));
			}
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter param = this.parameters[i];
			param.index = mw.visitParameter(param.qualifiedName, param.type);
		}
		
		// Write the Value
		
		mw.visitCode();
		this.value.writeExpression(mw);
		mw.visitEnd(this.method.getType());
		
		// Reset Captured Field Indexes
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IVariable var = this.capturedFields[i];
			var.setIndex(prevIndex[i]);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.parameterCount == 0)
		{
			buffer.append(Formatting.Method.emptyParameters);
		}
		else if (this.parameterCount == 1)
		{
			LambdaParameter param = this.parameters[0];
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
			Util.astToString(prefix, this.parameters, this.parameterCount, Formatting.Method.parameterSeperator, buffer);
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.value.toString(prefix, buffer);
	}
}

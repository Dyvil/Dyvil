package dyvil.tools.compiler.ast.value;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.LambdaParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class LambdaValue extends ASTNode implements IValue, IValued, IClassCompilable, IContext
{
	public static final Handle	BOOTSTRAP	= new Handle(ClassFormat.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
													"(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;" + "Ljava/lang/invoke/MethodType;"
															+ "Ljava/lang/invoke/MethodType;" + "Ljava/lang/invoke/MethodHandle;"
															+ "Ljava/lang/invoke/MethodType;)" + "Ljava/lang/invoke/CallSite;");
	public LambdaParameter[]	parameters;
	public int					parameterCount;
	public IValue				value;
	
	/**
	 * The instantiated type this lambda expression represents
	 */
	protected IType				type;
	
	/**
	 * The abstract method this lambda expression implements
	 */
	protected IMethod			method;
	
	private IContext			context;
	private int					index;
	
	private String				owner;
	private String				name;
	private String				lambdaDesc;
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
				lt.addType(t == null ? Types.UNKNOWN : t);
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
			IParameter param = method.getParameter(i);
			if (lambdaParam.type == null)
			{
				lambdaParam.type = param.getType();
				continue;
			}
			if (!param.getType().equals(lambdaParam.type))
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
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter param = this.parameters[i];
			if (param.name == name)
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
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
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
		this.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Value gets resolved in check()
		
		IType type = context.getThisType();
		if (type == null)
		{
			return this;
		}
		
		IClass iclass = type.getTheClass();
		if (iclass == null)
		{
			return this;
		}
		
		this.owner = iclass.getInternalName();
		this.index = iclass.compilableCount();
		iclass.addCompilable(this);
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.context = context;
		
		this.value = this.value.resolve(markers, this);
		
		if (this.method != null)
		{
			if (this.method.hasTypeVariables())
			{
				for (int i = 0; i < this.parameterCount; i++)
				{
					LambdaParameter param = this.parameters[i];
					param.baseType = this.method.getParameter(i).getType();
					param.type = param.type.getConcreteType(this.type);
				}
				
				this.returnType = this.method.getType(this.type);
			}
			else
			{
				this.returnType = this.method.getType();
			}
			
			IValue value1 = this.value.withType(this.returnType);
			if (value1 == null)
			{
				Marker marker = markers.create(this.value.getPosition(), "lambda.type");
				marker.addInfo("Method Return Type: " + this.returnType);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
		}
		else
		{
			markers.add(this.position, "lambda.method");
		}
		
		this.value.checkTypes(markers, this);
		
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
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
	public void writeExpression(MethodWriter writer)
	{
		this.name = "lambda$" + this.index;
		this.lambdaDesc = this.getLambdaDescriptor();
		
		int handleType;
		if (this.thisType != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, 0);
			handleType = ClassFormat.H_INVOKESPECIAL;
		}
		else
		{
			handleType = ClassFormat.H_INVOKESTATIC;
		}
		
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].writeGet(writer, null);
		}
		
		String name = this.getName();
		String desc = this.getLambdaDescriptor();
		String invokedName = this.method.getName().qualified;
		String invokedType = this.getInvokeDescriptor();
		org.objectweb.asm.Type type1 = org.objectweb.asm.Type.getMethodType(this.method.getDescriptor());
		org.objectweb.asm.Type type2 = org.objectweb.asm.Type.getMethodType(this.getSpecialDescriptor());
		Handle handle = new Handle(handleType, this.owner, name, desc);
		writer.writeInvokeDynamic(invokedName, invokedType, BOOTSTRAP, type1, handle, type2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	private String getName()
	{
		if (this.name != null)
		{
			return this.name;
		}
		
		return this.name = "lambda$" + this.index;
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
		if (this.lambdaDesc != null)
		{
			return this.lambdaDesc;
		}
		
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
		return this.lambdaDesc = buffer.toString();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		boolean instance = this.thisType != null;
		int modifiers = instance ? Modifiers.PRIVATE | Modifiers.SYNTHETIC : Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.getName(), this.getLambdaDescriptor(), null, null));
		
		// Updated Captured Field Indexes
		
		int[] prevIndex = null;
		
		if (instance)
		{
			mw.setInstanceMethod();
		}
		
		if (this.capturedFieldCount > 0)
		{
			prevIndex = new int[this.capturedFieldCount];
			for (int i = 0; i < this.capturedFieldCount; i++)
			{
				IVariable var = this.capturedFields[i];
				prevIndex[i] = var.getIndex();
				var.setIndex(mw.registerParameter(var.getName().qualified, var.getType()));
			}
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			LambdaParameter param = this.parameters[i];
			param.index = mw.registerParameter(param.name.qualified, param.type);
		}
		
		// Write the Value
		
		mw.begin();
		this.value.writeExpression(mw);
		mw.end(this.method.getType());
		
		// Reset Captured Field Indexes
		
		for (int i = 0; i < this.capturedFieldCount; i++)
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

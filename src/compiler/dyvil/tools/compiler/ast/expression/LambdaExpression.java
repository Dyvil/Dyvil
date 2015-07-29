package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.MapTypeContext;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class LambdaExpression implements IValue, IValued, IClassCompilable, IDefaultContext
{
	public static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/LambdaMetafactory", "metafactory",
			"(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;" + "Ljava/lang/invoke/MethodType;" + "Ljava/lang/invoke/MethodType;"
					+ "Ljava/lang/invoke/MethodHandle;" + "Ljava/lang/invoke/MethodType;)" + "Ljava/lang/invoke/CallSite;");
					
	protected ICodePosition position;
	
	protected IParameter[]	parameters;
	protected int			parameterCount;
	protected IValue		value;
	
	// Metadata
	
	/**
	 * The instantiated type this lambda expression represents
	 */
	protected IType	type;
	private IType	returnType;
	
	/**
	 * The abstract method this lambda expression implements
	 */
	protected IMethod method;
	
	private CaptureVariable[]	capturedFields;
	private int					capturedFieldCount;
	private IClass				thisClass;
	
	private String	owner;
	private String	name;
	private String	lambdaDesc;
	
	public LambdaExpression(ICodePosition position)
	{
		this.position = position;
		this.parameters = new IParameter[2];
	}
	
	public LambdaExpression(ICodePosition position, IParameter param)
	{
		this.position = position;
		this.parameters = new IParameter[1];
		this.parameters[0] = param;
		this.parameterCount = 1;
	}
	
	public LambdaExpression(ICodePosition position, IParameter[] params, int paramCount)
	{
		this.position = position;
		this.parameters = params;
		this.parameterCount = paramCount;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int valueTag()
	{
		return LAMBDA;
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		this.owner = internalName;
		this.name = "lambda$" + index;
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
	
	public void setMethod(IMethod method)
	{
		this.method = method;
	}
	
	public IMethod getMethod()
	{
		return this.method;
	}
	
	public IType getReturnType()
	{
		return this.returnType;
	}
	
	public void setReturnType(IType returnType)
	{
		this.returnType = returnType;
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
			LambdaType lt = new LambdaType(this.parameterCount);
			for (int i = 0; i < this.parameterCount; i++)
			{
				lt.addType(this.parameters[i].getType());
			}
			lt.setType(this.returnType != null ? this.returnType : Types.UNKNOWN);
			this.type = lt;
			return lt;
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isType(type))
		{
			return null;
		}
		
		this.type = type;
		this.method = type.getFunctionalMethod();
		
		if (this.method != null)
		{
			this.inferTypes(markers);
			
			IContext context1 = new CombiningContext(this, context);
			this.value = this.value.resolve(markers, context1);
			
			IType valueType = this.value.getType();
			
			IValue value1 = this.value.withType(this.returnType, this.returnType, markers, context1);
			if (value1 == null)
			{
				Marker marker = markers.create(this.value.getPosition(), "lambda.type");
				marker.addInfo("Method Return Type: " + this.returnType);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
				valueType = this.value.getType();
			}
			
			ITypeContext tempContext = new MapTypeContext();
			this.method.getType().inferTypes(valueType, tempContext);
			IType type1 = this.method.getTheClass().getType().getConcreteType(tempContext);
			
			type.inferTypes(type1, typeContext);
			
			this.returnType = valueType;
		}
		
		if (this.type.typeTag() == IType.LAMBDA)
		{
			// Trash the old lambda type and generate a new one from scratch
			this.type = null;
			this.type = this.getType();
		}
		else
		{
			this.type = type.getConcreteType(typeContext);
		}
		
		return this;
	}
	
	private void inferTypes(MarkerList markers)
	{
		if (!this.method.hasTypeVariables())
		{
			for (int i = 0; i < this.parameterCount; i++)
			{
				IParameter param = this.parameters[i];
				if (param.getType() == Types.UNKNOWN)
				{
					param.setType(this.method.getParameter(i).getType());
				}
			}
			
			this.returnType = this.method.getType();
			return;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			IType parType = param.getType();
			if (parType != Types.UNKNOWN)
			{
				continue;
			}
			
			IType methodParamType = this.method.getParameter(i).getType();
			IType concreteType = methodParamType.getConcreteType(this.type).getParameterType();
			
			// Can't infer parameter type
			if (concreteType == methodParamType && concreteType.hasTypeVariables())
			{
				markers.add(param.getPosition(), "lambda.parameter.type", param.getName());
			}
			param.setType(concreteType);
		}
		
		this.returnType = this.method.getType().getConcreteType(this.type).getReturnType();
		return;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.type != null && type.isSuperTypeOf(this.type))
		{
			return true;
		}
		
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
			IParameter lambdaParam = this.parameters[i];
			IParameter param = method.getParameter(i);
			IType lambdaParamType = lambdaParam.getType();
			if (lambdaParamType == null)
			{
				continue;
			}
			if (!param.getType().equals(lambdaParamType))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return this.isType(type) ? 1 : 0;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		
		return null;
	}
	
	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		this.thisClass = type;
		return new VariableThis();
	}
	
	@Override
	public IVariable capture(IVariable variable)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameters[i] == variable)
			{
				return variable;
			}
		}
		
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureVariable[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureVariable(variable);
		}
		
		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable var = this.capturedFields[i];
			if (var.variable == variable)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return var;
			}
		}
		
		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureVariable[] temp = new CaptureVariable[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureVariable(variable);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.resolveTypes(markers, context);
		}
		
		this.value.resolveTypes(markers, new CombiningContext(this, context));
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Resolving the value happens in withType
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, new CombiningContext(this, context));
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		compilableList.addCompilable(this);
		
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.lambdaDesc = this.getLambdaDescriptor();
		
		int handleType;
		if (this.thisClass != null)
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
			CaptureVariable var = this.capturedFields[i];
			writer.writeVarInsn(var.getActualType().getLoadOpcode(), var.variable.getIndex());
		}
		
		String name = this.name;
		String desc = this.getLambdaDescriptor();
		String invokedName = this.method.getName().qualified;
		String invokedType = this.getInvokeDescriptor();
		dyvil.tools.asm.Type type1 = dyvil.tools.asm.Type.getMethodType(this.method.getDescriptor());
		dyvil.tools.asm.Type type2 = dyvil.tools.asm.Type.getMethodType(this.getSpecialDescriptor());
		Handle handle = new Handle(handleType, this.owner, name, desc);
		
		writer.writeLineNumber(this.getLineNumber());
		writer.writeInvokeDynamic(invokedName, invokedType, BOOTSTRAP, type1, handle, type2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	private String getInvokeDescriptor()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		if (this.thisClass != null)
		{
			buffer.append('L').append(this.thisClass.getInternalName()).append(';');
		}
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			this.capturedFields[i].getActualType().appendExtendedName(buffer);
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
			this.parameters[i].getType().appendExtendedName(buffer);
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
			this.capturedFields[i].getActualType().appendExtendedName(buffer);
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getType().appendExtendedName(buffer);
		}
		buffer.append(')');
		this.returnType.appendExtendedName(buffer);
		return this.lambdaDesc = buffer.toString();
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		boolean instance = this.thisClass != null;
		int modifiers = instance ? Modifiers.PRIVATE | Modifiers.SYNTHETIC : Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name, this.getLambdaDescriptor(), null, null));
		
		int index = 0;
		if (instance)
		{
			mw.setThisType(this.thisClass.getInternalName());
			index = 1;
		}
		
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable capture = this.capturedFields[i];
			capture.index = index;
			index = mw.registerParameter(index, capture.variable.getName().qualified, capture.getActualType(), 0);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.setIndex(index);
			index = mw.registerParameter(index, param.getName().qualified, param.getType(), 0);
		}
		
		// Write the Value
		
		mw.begin();
		this.value.writeExpression(mw);
		mw.end(this.returnType);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.parameterCount == 1)
		{
			IParameter param = this.parameters[0];
			if (param.getType() != Types.UNKNOWN)
			{
				buffer.append('(');
				param.toString(prefix, buffer);
				buffer.append(')');
			}
			else
			{
				buffer.append(param.getName());
			}
			buffer.append(' ');
		}
		else if (this.parameterCount > 1)
		{
			buffer.append('(');
			IParameter first = this.parameters[0];
			if (first.getType() == Types.UNKNOWN)
			{
				buffer.append(first.getName());
				for (int i = 1; i < this.parameterCount; i++)
				{
					buffer.append(", ").append(this.parameters[i].getName());
				}
			}
			else
			{
				Util.astToString(prefix, this.parameters, this.parameterCount, ", ", buffer);
			}
			buffer.append(") ");
		}
		
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.value.toString(prefix, buffer);
	}
}

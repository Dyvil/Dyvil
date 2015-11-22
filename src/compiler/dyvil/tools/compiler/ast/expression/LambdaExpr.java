package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.access.AbstractCall;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.MapTypeContext;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class LambdaExpr implements IValue, IClassCompilable, IDefaultContext, IValueConsumer
{
	public static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/LambdaMetafactory",
	                                                  "metafactory",
	                                                  "(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;"
			                                                  + "Ljava/lang/invoke/MethodType;"
			                                                  + "Ljava/lang/invoke/MethodType;"
			                                                  + "Ljava/lang/invoke/MethodHandle;"
			                                                  + "Ljava/lang/invoke/MethodType;)"
			                                                  + "Ljava/lang/invoke/CallSite;");

	protected ICodePosition position;
	
	protected IParameter[] parameters;
	protected int          parameterCount;
	protected IValue       value;
	
	// Metadata
	
	/**
	 * The instantiated type this lambda expression represents
	 */
	protected IType type;
	private   IType returnType;
	
	/**
	 * The abstract method this lambda expression implements
	 */
	protected IMethod method;
	
	private CaptureVariable[] capturedFields;
	private int               capturedFieldCount;
	private IClass            thisClass;
	
	private String owner;
	private String name;
	private String lambdaDesc;
	
	private int directInvokeOpcode;
	
	public LambdaExpr(ICodePosition position)
	{
		this.position = position;
		this.parameters = new IParameter[2];
	}
	
	public LambdaExpr(ICodePosition position, IParameter param)
	{
		this.position = position;
		this.parameters = new IParameter[1];
		this.parameters[0] = param;
		this.parameterCount = 1;
	}
	
	public LambdaExpr(ICodePosition position, IParameter[] params, int paramCount)
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
	public void setPosition(ICodePosition position)
	{
		this.position = position;
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
	public boolean isResolved()
	{
		return this.type != null && this.type.isResolved();
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
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.isType(type))
		{
			return null;
		}
		
		if (type.getTheClass() == Types.OBJECT_CLASS)
		{
			type = this.getType();
		}
		
		this.type = type;
		this.method = type.getFunctionalMethod();
		
		if (this.method != null)
		{
			this.inferTypes(markers);
			
			IContext context1 = new CombiningContext(this, context);
			this.value = this.value.resolve(markers, context1);
			
			IType valueType = this.value.getType();
			
			if (this.returnType == Types.UNKNOWN)
			{
				this.returnType = valueType;
			}
			else
			{
				ITypeVariable typeVariable = this.returnType.getTypeVariable();
				if (typeVariable != null)
				{
					this.returnType = typeVariable.getParameterType();
				}
			}
			
			IValue value1 = this.value.withType(this.returnType, this.returnType, markers, context1);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.value.getPosition(), "lambda.type");
				marker.addInfo(I18n.getString("method.type", this.returnType));
				marker.addInfo(I18n.getString("value.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = value1;
				valueType = this.value.getType();
			}
			
			this.inferReturnType(type, typeContext, valueType);
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

	public void inferReturnType(IType type, ITypeContext typeContext, IType valueType)
	{
		ITypeContext tempContext = new MapTypeContext();
		this.method.getType().inferTypes(valueType, tempContext);
		IType type1 = this.method.getTheClass().getType().getConcreteType(tempContext);
		
		type.inferTypes(type1, typeContext);
		
		this.returnType = valueType;
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
			if (param.getType() != Types.UNKNOWN)
			{
				continue;
			}
			
			IType methodParamType = this.method.getParameter(i).getType();
			IType concreteType = methodParamType.getConcreteType(this.type).getParameterType();
			
			// Can't infer parameter type
			if (concreteType == Types.UNKNOWN)
			{
				markers.add(I18n.createMarker(param.getPosition(), "lambda.parameter.type", param.getName()));
			}
			param.setType(concreteType);
		}
		
		this.returnType = this.method.getType().getConcreteType(this.type).getReturnType();
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
		if (iclass == Types.OBJECT_CLASS)
		{
			return true;
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
			if (lambdaParamType == Types.UNKNOWN)
			{
				continue;
			}
			if (!param.getType().isSameType(lambdaParamType))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass() == Types.OBJECT_CLASS)
		{
			return 2;
		}
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
		return VariableThis.DEFAULT;
	}
	
	@Override
	public IAccessible getAccessibleImplicit()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter parameter = this.parameters[i];
			if (parameter.getName() == Names.$it)
			{
				return parameter;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isMember(IVariable variable)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			if (this.parameters[i] == variable)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
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
				return variable;
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
		this.value = this.value.cleanup(context, compilableList);
		
		if (this.capturedFieldCount == 0)
		{
			if (this.value instanceof AbstractCall && this.value.valueTag() != COMPOUND_CALL)
			{
				AbstractCall call = (AbstractCall) this.value;
				
				IMethod method = call.getMethod();
				if (method != null && this.checkCall(call.getReceiver(), call.getArguments(), method))
				{
					switch (method.getInvokeOpcode())
					{
					case Opcodes.INVOKEVIRTUAL:
						this.directInvokeOpcode = ClassFormat.H_INVOKEVIRTUAL;
						break;
					case Opcodes.INVOKESTATIC:
						this.directInvokeOpcode = ClassFormat.H_INVOKESTATIC;
						break;
					case Opcodes.INVOKEINTERFACE:
						this.directInvokeOpcode = ClassFormat.H_INVOKEINTERFACE;
						break;
					case Opcodes.INVOKESPECIAL:
						this.directInvokeOpcode = ClassFormat.H_INVOKESPECIAL;
						break;
					}
					
					this.name = method.getName().qualified;
					this.owner = method.getTheClass().getInternalName();
					this.lambdaDesc = method.getDescriptor();
					return this;
				}
			}
			// To avoid trouble with anonymous classes
			else if (this.value.getClass() == ConstructorCall.class)
			{
				ConstructorCall c = (ConstructorCall) this.value;
				IConstructor ctor = c.getConstructor();
				if (this.checkCall(null, c.getArguments(), ctor))
				{
					this.directInvokeOpcode = ClassFormat.H_NEWINVOKESPECIAL;
					this.name = "<init>";
					this.owner = ctor.getTheClass().getInternalName();
					this.lambdaDesc = ctor.getDescriptor();
					
					return this;
				}
			}
		}
		
		compilableList.addCompilable(this);
		
		return this;
	}
	
	private boolean checkCall(IValue instance, IArguments arguments, IParameterized p)
	{
		boolean receiver = false;
		
		if (instance != null)
		{
			if (instance.isPrimitive())
			{
				return false;
			}
			
			if (this.parameterCount <= 0)
			{
				return false;
			}
			
			if (isFieldAccess(instance, this.parameters[0]))
			{
				if (arguments.size() != this.parameterCount - 1)
				{
					return false;
				}
				
				for (int i = 1; i < this.parameterCount; i++)
				{
					IValue v = arguments.getValue(i - 1, p.getParameter(i - 1));
					if (!isFieldAccess(v, this.parameters[i]))
					{
						return false;
					}
				}
				
				this.value = null;
				return true;
			}
			
			receiver = true;
		}
		
		if (arguments.size() != this.parameterCount)
		{
			return false;
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IValue v = arguments.getValue(i, p.getParameter(i));
			if (!isFieldAccess(v, this.parameters[i]))
			{
				return false;
			}
		}
		
		if (receiver)
		{
			if (instance.isPrimitive())
			{
				return false;
			}
			
			this.value = instance;
			this.thisClass = instance.getType().getTheClass();
		}
		else
		{
			this.value = null;
		}
		
		return true;
	}
	
	private static boolean isFieldAccess(IValue value, IDataMember member)
	{
		return value.valueTag() == IValue.FIELD_ACCESS && ((FieldAccess) value).getField() == member;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		int handleType;
		
		if (this.directInvokeOpcode == 0)
		{
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
				writer.writeVarInsn(var.getActualType().getLoadOpcode(), var.variable.getLocalIndex());
			}
		}
		else
		{
			if (this.value != null)
			{
				this.value.writeExpression(writer);
			}
			
			handleType = this.directInvokeOpcode;
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
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	/**
	 * @return the descriptor that contains the captured instance and captured
	 * variables (if present) as the argument types and the instantiated
	 * method type as the return type.
	 */
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
	
	/**
	 * @return the specialized method type of the SAM method, as opposed to
	 * {@link IMethod#getDescriptor()}.
	 */
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
	
	/**
	 * @return the descriptor of the (synthetic) lambda callback method,
	 * including captured variables, parameter types and the return
	 * type.
	 */
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
		for (int i = this.directInvokeOpcode != 0 && this.directInvokeOpcode != Opcodes.INVOKESTATIC ? 1 : 0;
		     i < this.parameterCount; i++)
		{
			this.parameters[i].getType().appendExtendedName(buffer);
		}
		buffer.append(')');
		if (this.directInvokeOpcode == ClassFormat.H_NEWINVOKESPECIAL)
		{
			buffer.append('V');
		}
		else
		{
			this.returnType.appendExtendedName(buffer);
		}
		return this.lambdaDesc = buffer.toString();
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.directInvokeOpcode != 0)
		{
			return;
		}
		
		boolean instance = this.thisClass != null;
		int modifiers = instance ?
				Modifiers.PRIVATE | Modifiers.SYNTHETIC :
				Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		MethodWriter mw = new MethodWriterImpl(writer,
		                                       writer.visitMethod(modifiers, this.name, this.getLambdaDescriptor(),
		                                                          null, null));
		
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
			param.setLocalIndex(index);
			index = mw.registerParameter(index, param.getName().qualified, param.getType(), 0);
		}
		
		// Write the Value
		
		mw.begin();
		this.value.writeExpression(mw, this.returnType);
		mw.end(this.returnType);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
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

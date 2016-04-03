package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.access.AbstractCall;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.MapTypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.IParametric;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.LambdaType;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.CaptureHelper;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class LambdaExpr implements IValue, IClassCompilable, IDefaultContext, IValueConsumer, IParameterList
{
	public static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/LambdaMetafactory",
	                                                  "metafactory",
	                                                  "(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;"
		                                                  + "Ljava/lang/invoke/MethodType;"
		                                                  + "Ljava/lang/invoke/MethodType;"
		                                                  + "Ljava/lang/invoke/MethodHandle;"
		                                                  + "Ljava/lang/invoke/MethodType;)"
		                                                  + "Ljava/lang/invoke/CallSite;");

	public static final TypeChecker.MarkerSupplier LAMBDA_MARKER_SUPPLIER = TypeChecker.markerSupplier("lambda.type",
	                                                                                                   "method.type",
	                                                                                                   "value.type");

	// Flags

	private static final int HANDLE_TYPE_MASK = 0b1111;
	private static final int VALUE_RESOLVED   = 0b10000;

	protected IParameter[] parameters;
	protected int          parameterCount;

	protected IValue value;

	// Metadata

	protected ICodePosition position;

	/**
	 * Stores various metadata about this Lambda Expression. See Flags constants for possible values
	 */
	private int flags;

	/**
	 * The instantiated type this lambda expression represents
	 */
	protected IType type;

	/**
	 * The return type of this lambda expression (the type of the return value)
	 */
	private IType returnType;

	/**
	 * The abstract method this lambda expression implements
	 */
	protected IMethod method;

	protected CaptureHelper captureHelper;

	/**
	 * The enclosing class internal name
	 */
	private String owner;

	/**
	 * The name of the synthetic lambda method
	 */
	private String name;

	/**
	 * The descriptor of the synthetic lambda method
	 */
	private String descriptor;

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
	public void setValue(IValue value)
	{
		this.value = value;
	}

	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public int parameterCount()
	{
		return this.parameterCount;
	}

	@Override
	public void addParameter(IParameter parameter)
	{
		int index = this.parameterCount++;
		if (index >= this.parameters.length)
		{
			IParameter[] temp = new IParameter[index + 1];
			System.arraycopy(this.parameters, 0, temp, 0, index);
			this.parameters = temp;
		}
		this.parameters[index] = parameter;
	}

	@Override
	public void setParameter(int index, IParameter parameter)
	{
		this.parameters[index] = parameter;
	}

	@Override
	public IParameter getParameter(int index)
	{
		return this.parameters[index];
	}

	@Override
	public IParameter[] getParameters()
	{
		return this.parameters;
	}

	public void setMethod(IMethod method)
	{
		this.method = method;
	}

	public IMethod getMethod()
	{
		return this.method;
	}

	@Override
	public IType getReturnType()
	{
		return this.returnType;
	}

	public void setReturnType(IType returnType)
	{
		this.returnType = returnType;
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
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			final LambdaType lambdaType = new LambdaType(this.parameterCount);
			for (int i = 0; i < this.parameterCount; i++)
			{
				lambdaType.addType(this.parameters[i].getType());
			}
			lambdaType.setType(this.returnType != null ? this.returnType : Types.UNKNOWN);

			return this.type = lambdaType;
		}
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	/**
	 * Returns the Handle Type for Direct Invocation if this is a Method Pointer, {@code 0} otherwise.
	 *
	 * @return the Handle Type for Direct Invocation
	 */
	public int getHandleType()
	{
		return this.flags & HANDLE_TYPE_MASK;
	}

	private void setHandleType(int handleType)
	{
		this.flags = this.flags & ~HANDLE_TYPE_MASK | handleType;
	}

	private CaptureHelper getCaptureHelper()
	{
		if (this.captureHelper != null)
		{
			return this.captureHelper;
		}
		return this.captureHelper = new CaptureHelper(CaptureVariable.FACTORY);
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

			final IContext combinedContext = context.push(this);

			if ((this.flags & VALUE_RESOLVED) == 0)
			{
				this.value = this.value.resolve(markers, combinedContext);
				this.flags |= VALUE_RESOLVED;
			}

			if (this.returnType == Types.UNKNOWN)
			{
				this.returnType = this.value.getType();
			}

			this.value = TypeChecker
				             .convertValue(this.value, this.returnType, this.returnType, markers, combinedContext,
				                           LAMBDA_MARKER_SUPPLIER);

			this.inferReturnType(type, typeContext, this.value.getType());

			context.pop();
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
		final ITypeContext tempContext = new MapTypeContext();
		this.method.getType().inferTypes(valueType, tempContext);

		final IType concreteType = this.method.getEnclosingClass().getType().getConcreteType(tempContext);

		type.inferTypes(concreteType, typeContext);

		this.returnType = valueType;
	}

	private void inferTypes(MarkerList markers)
	{
		if (!this.method.hasTypeVariables())
		{
			for (int i = 0; i < this.parameterCount; i++)
			{
				final IParameter parameter = this.parameters[i];
				if (parameter.getType() == Types.UNKNOWN)
				{
					parameter.setType(this.method.getParameter(i).getType());
				}
			}

			this.returnType = this.method.getType();
			return;
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			final IParameter parameter = this.parameters[i];
			if (parameter.getType() != Types.UNKNOWN)
			{
				continue;
			}

			final IType methodParamType = this.method.getParameter(i).getType();
			final IType concreteType = methodParamType.getConcreteType(this.type).asParameterType();

			// Can't infer parameter type
			if (concreteType == Types.UNKNOWN)
			{
				markers.add(Markers.semantic(parameter.getPosition(), "lambda.parameter.type", parameter.getName()));
			}
			parameter.setType(concreteType);
		}

		this.returnType = this.method.getType().getConcreteType(this.type).asParameterType();
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.type != null && Types.isSuperType(type, this.type))
		{
			return true;
		}

		final IClass interfaceClass = type.getTheClass();
		if (interfaceClass == null)
		{
			return false;
		}
		if (interfaceClass == Types.OBJECT_CLASS)
		{
			return true;
		}

		final IMethod method = interfaceClass.getFunctionalMethod();
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
			final IParameter lambdaParameter = this.parameters[i];

			final IType lambdaParameterType = lambdaParameter.getType();
			if (lambdaParameterType == Types.UNKNOWN)
			{
				continue;
			}

			final IParameter methodParameter = method.getParameter(i);
			final IType methodParameterType = methodParameter.getType().asParameterType();
			if (!Types.isSuperType(methodParameterType, lambdaParameterType))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int getTypeMatch(IType type)
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
			final IParameter parameter = this.parameters[i];
			if (parameter.getName() == name)
			{
				return parameter;
			}
		}

		return null;
	}

	@Override
	public IAccessible getAccessibleThis(IClass type)
	{
		this.getCaptureHelper().setThisClass(type);
		return VariableThis.DEFAULT;
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

		return this.getCaptureHelper().capture(variable);
	}

	@Override
	public byte checkException(IType type)
	{
		return this.method != null ? this.method.checkException(type) : FALSE;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, context);
		}

		if (this.value != null)
		{
			context = context.push(this);
			this.value.resolveTypes(markers, context);
			context.pop();
		}
		else
		{
			markers.add(Markers.semantic(this.position, "lambda.value.invalid"));
			this.value = new VoidValue(this.position);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			final IParameter parameter = this.parameters[i];

			if (parameter.getType() == Types.UNKNOWN)
			{
				parameter.setType(null); // avoid missing parameter type error
				parameter.resolve(markers, context);
				parameter.setType(Types.UNKNOWN);

				// Resolving the value happens in withType
				return this;
			}

			parameter.resolve(markers, context);
		}

		// All parameter types are known, we can actually resolve the return value now

		context = context.push(this);
		this.value = this.value.resolve(markers, context);
		context.pop();

		this.flags |= VALUE_RESOLVED;
		this.returnType = this.value.getType();

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, context);
		}

		context = context.push(this);
		this.value.checkTypes(markers, context);
		context = context.pop();

		if (this.captureHelper != null)
		{
			this.captureHelper.checkCaptures(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, context);
		}

		context = context.push(this);
		this.value.check(markers, context);
		context.pop();
	}

	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}

		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(context, compilableList);
		}

		context = context.push(this);
		this.value = this.value.cleanup(context, compilableList);
		context.pop();

		if (this.captureHelper == null || !this.captureHelper.hasCaptures())
		{
			if (this.value instanceof AbstractCall)
			{
				final AbstractCall call = (AbstractCall) this.value;
				final IMethod method = call.getMethod();

				if (method != null && this.checkCall(call.getReceiver(), call.getArguments(), method))
				{
					this.setHandleType(ClassFormat.insnToHandle(method.getInvokeOpcode()));
					this.name = method.getName().qualified;
					this.owner = method.getEnclosingClass().getInternalName();
					this.descriptor = method.getDescriptor();
					return this;
				}
			}
			// To avoid trouble with anonymous classes
			else if (this.value.getClass() == ConstructorCall.class)
			{
				final ConstructorCall call = (ConstructorCall) this.value;
				final IConstructor constructor = call.getConstructor();

				if (this.checkCall(null, call.getArguments(), constructor))
				{
					this.setHandleType(ClassFormat.H_NEWINVOKESPECIAL);
					this.name = "<init>";
					this.owner = constructor.getEnclosingClass().getInternalName();
					this.descriptor = constructor.getDescriptor();

					return this;
				}
			}
		}

		compilableList.addCompilable(this);

		return this;
	}

	private boolean checkCall(IValue receiver, IArguments arguments, IParametric parametric)
	{
		final int params = this.parameterCount;
		if (receiver == null)
		{
			if (arguments.size() != params)
			{
				return false;
			}

			for (int i = 0; i < params; i++)
			{
				final IValue argument = arguments.getValue(i, parametric.getParameter(i));
				if (!isFieldAccess(argument, this.parameters[i]))
				{
					return false;
				}
			}

			return true;
		}

		if (params <= 0 || arguments.size() != params - 1 || !isFieldAccess(receiver, this.parameters[0]))
		{
			return false;
		}

		for (int i = 1; i < params; i++)
		{
			final IValue argument = arguments.getValue(i - 1, parametric.getParameter(i - 1));
			if (!isFieldAccess(argument, this.parameters[i]))
			{
				return false;
			}
		}

		return true;
	}

	private static boolean isFieldAccess(IValue value, IDataMember member)
	{
		return value.valueTag() == IValue.FIELD_ACCESS && ((FieldAccess) value).getField() == member;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		int handleType = this.getHandleType();
		if (handleType == 0)
		{
			handleType = ClassFormat.H_INVOKESTATIC;
			if (this.captureHelper != null)
			{
				if (this.captureHelper.isThisCaptured())
				{
					handleType = ClassFormat.H_INVOKESPECIAL;
				}
				this.captureHelper.writeCaptures(writer);
			}
		}

		final String desc = this.getTargetDescriptor();
		final String invokedName = this.method.getName().qualified;
		final String invokedType = this.getInvokeDescriptor();

		final dyvil.tools.asm.Type methodDescriptorType = dyvil.tools.asm.Type
			                                                  .getMethodType(this.method.getDescriptor());
		final dyvil.tools.asm.Type lambdaDescriptorType = dyvil.tools.asm.Type
			                                                  .getMethodType(this.getLambdaDescriptor());
		final Handle handle = new Handle(handleType, this.owner, this.name, desc);

		writer.visitLineNumber(this.getLineNumber());
		writer.visitInvokeDynamicInsn(invokedName, invokedType, BOOTSTRAP, methodDescriptorType, handle,
		                              lambdaDescriptorType);

		if (type != null)
		{
			this.type.writeCast(writer, type, this.getLineNumber());
		}
	}

	/**
	 * @return the descriptor that contains the captured instance and captured variables (if present) as the argument
	 * types and the instantiated method type as the return type.
	 */
	private String getInvokeDescriptor()
	{
		final StringBuilder builder = new StringBuilder().append('(');

		if (this.captureHelper != null)
		{
			this.captureHelper.appendThisCaptureType(builder);
			this.captureHelper.appendCaptureTypes(builder);
		}

		builder.append(')');

		this.type.appendExtendedName(builder);

		return builder.toString();
	}

	/**
	 * @return the specialized descriptor of the lambda callback method, including parameter types and return type, but
	 * excluding captured variables
	 */
	private String getLambdaDescriptor()
	{
		final StringBuilder buffer = new StringBuilder().append('(');

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getType().appendExtendedName(buffer);
		}

		buffer.append(')');

		this.returnType.appendExtendedName(buffer);
		return buffer.toString();
	}

	/**
	 * @return the descriptor of the (synthetic) lambda callback method, including captured variables, parameter types
	 * and the return type.
	 */
	private String getTargetDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		assert this.getHandleType() == 0;

		final StringBuilder builder = new StringBuilder().append('(');

		if (this.captureHelper != null)
		{
			this.captureHelper.appendCaptureTypes(builder);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getType().appendExtendedName(builder);
		}

		builder.append(')');

		this.returnType.appendExtendedName(builder);

		return this.descriptor = builder.toString();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.getHandleType() != 0)
		{
			return;
		}

		final boolean thisCaptured = this.captureHelper != null && this.captureHelper.isThisCaptured();
		final int modifiers = thisCaptured ?
			                      Modifiers.PRIVATE | Modifiers.SYNTHETIC :
			                      Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;

		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name,
		                                                                                  this.getTargetDescriptor(),
		                                                                                  null, null));

		int index = 0;

		if (this.captureHelper != null)
		{
			if (thisCaptured)
			{
				methodWriter.setThisType(this.owner);
				index = 1;
			}

			this.captureHelper.writeCaptureParameters(methodWriter, index);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeInit(methodWriter);
		}

		// Write the Value

		methodWriter.visitCode();
		this.value.writeExpression(methodWriter, this.returnType);
		methodWriter.visitEnd(this.returnType);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		IParameter parameter;
		if (this.parameterCount == 1 && (parameter = this.parameters[0]).getType() == Types.UNKNOWN // single parameter
			    && !Formatting.getBoolean("lambda.single.wrap"))
		{
			buffer.append(parameter.getName());

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (this.parameterCount > 0)
		{
			buffer.append('(');
			if (Formatting.getBoolean("lambda.open_paren.space_after"))
			{
				buffer.append(' ');
			}

			Util.astToString(prefix, this.parameters, this.parameterCount,
			                 Formatting.getSeparator("lambda.separator", ','), buffer);

			if (Formatting.getBoolean("lambda.close_paren.space_before"))
			{
				buffer.append(' ');
			}

			buffer.append(')');

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (Formatting.getBoolean("lambda.empty.wrap"))
		{
			buffer.append("()");
			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}

		buffer.append("=>");
		if (Formatting.getBoolean("lambda.arrow.space_after"))
		{
			buffer.append(' ');
		}

		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
		}
	}
}

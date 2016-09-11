package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.access.AbstractCall;
import dyvil.tools.compiler.ast.access.ConstructorCall;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.MapTypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
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

public final class LambdaExpr implements IValue, IClassCompilable, IDefaultContext, IValueConsumer, IParametric
{
	public static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/LambdaMetafactory",
	                                                  "metafactory",
	                                                  ClassFormat.BSM_HEAD + "Ljava/lang/invoke/MethodType;"
		                                                  + "Ljava/lang/invoke/MethodHandle;"
		                                                  + "Ljava/lang/invoke/MethodType;" + ClassFormat.BSM_TAIL);

	public static final TypeChecker.MarkerSupplier LAMBDA_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"lambda.value.type.incompatible", "return.type", "value.type");

	// Flags

	private static final int HANDLE_TYPE_MASK    = 0b00001111;
	private static final int VALUE_RESOLVED      = 0b00010000;
	public static final  int IMPLICIT_PARAMETERS = 0b00100000;
	private static final int EXPLICIT_RETURN     = 0b01000000;

	protected ParameterList parameters;

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
		this.parameters = new ParameterList(2);
	}

	public LambdaExpr(ICodePosition position, IParameter param)
	{
		this.position = position;
		this.parameters = new ParameterList(param);
	}

	public LambdaExpr(ICodePosition position, IParameter[] params, int paramCount)
	{
		this.position = position;
		this.parameters = new ParameterList(params, paramCount);
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

	// Parameters

	@Override
	public IParameterList getParameterList()
	{
		return this.parameters;
	}

	// Return Value

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	public IValue getValue()
	{
		return this.value;
	}

	// Metadata

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
		this.flags |= EXPLICIT_RETURN;
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
		if (this.type != null)
		{
			return this.type;
		}

		final int count = this.parameters.size();
		final LambdaType lambdaType = new LambdaType(count);
		for (int i = 0; i < count; i++)
		{
			lambdaType.addType(this.parameters.get(i).getType());
		}
		lambdaType.setType(this.returnType != null ? this.returnType : Types.UNKNOWN);

		return this.type = lambdaType;
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

	public void setImplicitParameters(boolean implicitParameters)
	{
		if (implicitParameters)
		{
			this.flags |= IMPLICIT_PARAMETERS;
		}
		else
		{
			this.flags &= ~IMPLICIT_PARAMETERS;
		}
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

		assert this.method != null; // Otherwise isType would have returns false

		this.inferTypes(markers);

		final IContext combinedContext = context.push(this);

		if ((this.flags & VALUE_RESOLVED) == 0)
		{
			this.value = this.value.resolve(markers, combinedContext);
			this.flags |= VALUE_RESOLVED;
		}

		if (this.returnType.isUninferred())
		{
			this.returnType = this.value.getType();
		}

		this.value = TypeChecker.convertValue(this.value, this.returnType, this.returnType, markers, combinedContext,
		                                      LAMBDA_MARKER_SUPPLIER);

		this.inferReturnType(type, this.value.getType());

		context.pop();

		return this;
	}

	public void inferReturnType(IType type, IType valueType)
	{
		final ITypeContext tempContext = new MapTypeContext();
		this.method.getType().inferTypes(valueType, tempContext);

		final IType concreteType = this.method.getEnclosingClass().getType().getConcreteType(tempContext);

		type.inferTypes(concreteType, tempContext);

		if (this.returnType == null)
		{
			this.returnType = valueType;
		}

		this.type = type.getConcreteType(tempContext);
	}

	private void checkReturnType(MarkerList markers, IType expectedReturnType)
	{
		if (this.returnType == null)
		{
			this.returnType = expectedReturnType;
			return;
		}

		if (!Types.isSuperType(expectedReturnType, this.returnType))
		{
			markers.add(TypeChecker.typeError(this.returnType.getPosition(), expectedReturnType, this.returnType,
			                                  "lambda.return_type.incompatible", "return.type", "lambda.return_type"));
		}
	}

	private void inferTypes(MarkerList markers)
	{
		if (!this.method.hasTypeVariables())
		{
			for (int i = 0, count = this.parameters.size(); i < count; i++)
			{
				final IParameter parameter = this.parameters.get(i);
				if (parameter.getType().isUninferred())
				{
					final IType type = this.method.getParameterList().get(i).getType()
					                              .atPosition(parameter.getPosition());
					parameter.setType(type);
				}
			}

			this.checkReturnType(markers, this.method.getType());
			return;
		}

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			if (!parameter.getType().isUninferred())
			{
				continue;
			}

			final ICodePosition position = parameter.getPosition();
			final IType methodParamType = this.method.getParameterList().get(i).getType();
			final IType concreteType = methodParamType.getConcreteType(this.type);

			// Can't infer parameter type
			if (this.type.isUninferred())
			{
				if ((this.flags & IMPLICIT_PARAMETERS) != 0)
				{
					markers.add(Markers.semanticError(position, "lambda.parameter.implicit"));
				}
				else
				{
					markers.add(Markers.semanticError(position, "lambda.parameter.type", parameter.getName()));
				}
			}

			// asReturnType is required for Wildcard Types
			parameter.setType(concreteType.asReturnType().atPosition(position));
		}

		this.checkReturnType(markers, this.method.getType().getConcreteType(this.type));
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

		final IParameterList methodParameters = method.getParameterList();
		final int parameterCount = this.parameters.size();

		if (parameterCount != methodParameters.size())
		{
			return false;
		}

		for (int i = 0; i < parameterCount; i++)
		{
			final IType lambdaParameterType = this.parameters.get(i).getInternalType();
			if (lambdaParameterType.isUninferred())
			{
				continue;
			}

			final IType methodParameterType = methodParameters.get(i).getInternalType();
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
			return SUBTYPE_MATCH;
		}
		return this.isType(type) ? EXACT_MATCH : MISMATCH;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.parameters.resolveParameter(name);
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
		return this.parameters.isParameter(variable);
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
		this.parameters.resolveTypes(markers, context);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType = this.returnType.resolveType(markers, context);
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
			this.value = new DummyValue();
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		boolean delayResolve = false;

		for (int i = 0, count = this.parameters.size(); i < count; i++)
		{
			final IParameter parameter = this.parameters.get(i);

			if (parameter.getType().isUninferred())
			{
				parameter.setType(null); // avoid missing parameter type error
				parameter.resolve(markers, context);
				parameter.setType(Types.UNKNOWN);

				// Resolving the value happens in withType
				delayResolve = true;
			}
			else
			{
				parameter.resolve(markers, context);
			}
		}

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.resolve(markers, context);
		}

		if (delayResolve)
		{
			return this;
		}

		// All parameter types are known, we can actually resolve the return value now

		context = context.push(this);
		this.value = this.value.resolve(markers, context);
		context.pop();

		this.flags |= VALUE_RESOLVED;
		if (this.returnType == null)
		{
			this.returnType = this.value.getType();
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.parameters.checkTypes(markers, context);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.checkType(markers, context, IType.TypePosition.RETURN_TYPE);
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
		this.parameters.check(markers, context);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.check(markers, context);
		}

		context = context.push(this);
		this.value.check(markers, context);
		context.pop();
	}

	@Override
	public IValue foldConstants()
	{
		this.parameters.foldConstants();

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.foldConstants();
		}

		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.parameters.cleanup(context, compilableList);

		if (this.returnType != null && (this.flags & EXPLICIT_RETURN) != 0)
		{
			this.returnType.cleanup(context, compilableList);
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
		final int parameterCount = this.parameters.size();
		final IParameterList parameterList = parametric.getParameterList();

		if (receiver == null)
		{
			if (arguments.size() != parameterCount)
			{
				return false;
			}

			for (int i = 0; i < parameterCount; i++)
			{
				final IValue argument = arguments.getValue(i, parameterList.get(i));
				if (!isFieldAccess(argument, this.parameters.get(i)))
				{
					return false;
				}
			}

			return true;
		}

		if (arguments.size() != parameterCount - 1 || !isFieldAccess(receiver, this.parameters.get(0)))
		{
			return false;
		}

		for (int i = 1; i < parameterCount; i++)
		{
			final IValue argument = arguments.getValue(i - 1, parameterList.get(i - 1));
			if (!isFieldAccess(argument, this.parameters.get(i)))
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
		final String invokedName = this.method.getInternalName();
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
		final StringBuilder builder = new StringBuilder().append('(');
		this.parameters.appendDescriptor(builder);
		builder.append(')');

		this.returnType.appendExtendedName(builder);
		return builder.toString();
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
		this.parameters.appendDescriptor(builder);
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

		this.parameters.writeInit(methodWriter);

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
		final int parameterCount = this.parameters.size();

		if (parameterCount == 1 && (parameter = this.parameters.get(0)).getType().isUninferred() // single parameter
			    && !Formatting.getBoolean("lambda.single.wrap"))
		{
			buffer.append(parameter.getName());

			if (Formatting.getBoolean("lambda.arrow.space_before"))
			{
				buffer.append(' ');
			}
		}
		else if (parameterCount > 0)
		{
			buffer.append('(');
			if (Formatting.getBoolean("lambda.open_paren.space_after"))
			{
				buffer.append(' ');
			}

			Util.astToString(prefix, this.parameters.getParameterArray(), parameterCount,
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

package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InitializerCall implements ICall
{
	protected ICodePosition position;

	protected boolean isSuper;
	protected IArguments arguments = EmptyArguments.INSTANCE;

	// Metadata
	protected IType        targetType;
	protected IConstructor constructor;

	public InitializerCall(ICodePosition position, boolean isSuper)
	{
		this.position = position;
		this.isSuper = isSuper;
	}

	public InitializerCall(ICodePosition position, boolean isSuper, IArguments arguments, IType targetType)
	{
		this.position = position;
		this.isSuper = isSuper;
		this.arguments = arguments;
		this.targetType = targetType;
	}

	public InitializerCall(ICodePosition position, boolean isSuper, IArguments arguments, IType targetType, IConstructor constructor)
	{
		this.position = position;
		this.constructor = constructor;
		this.arguments = arguments;
		this.targetType = targetType;
		this.isSuper = isSuper;
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
		return INITIALIZER_CALL;
	}

	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}

	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}

	public boolean isSuper()
	{
		return this.isSuper;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
	}

	@Override
	public void checkArguments(MarkerList markers, IContext context)
	{
		this.constructor.checkArguments(markers, this.position, context, this.targetType, this.arguments);
	}

	private IType getTargetType(IContext context)
	{
		if (this.targetType != null)
		{
			return this.targetType;
		}

		final IType type = context.getThisType();

		if (!this.isSuper)
		{
			return this.targetType = type;
		}
		return this.targetType = type.getTheClass().getSuperType();
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		final IType targetType = this.getTargetType(context);
		if (targetType == null || !targetType.isResolved())
		{
			return null;
		}

		final IConstructor match = IContext.resolveConstructor(context, targetType, this.arguments);
		if (match != null)
		{
			this.constructor = match;
			this.checkArguments(markers, context);
			return this;
		}

		return null;
	}

	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
		IClass iclass = context.getThisClass();
		Marker marker = Markers.semantic(this.position, "resolve.constructor", iclass.getName().qualified);
		if (!this.arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			this.arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}

		markers.add(marker);
	}

	@Override
	public void resolveArguments(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.arguments.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		markers.add(Markers.semanticError(this.position, "initializercall.invalid"));
		this.checkNoError(markers, context);
	}

	public void checkNoError(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.foldConstants();
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 0);
		this.constructor.writeArguments(writer, this.arguments);
		this.constructor.writeInvoke(writer, this.getLineNumber());
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.isSuper ? "super.init" : "this.init");
		this.arguments.toString(prefix, buffer);
	}
}

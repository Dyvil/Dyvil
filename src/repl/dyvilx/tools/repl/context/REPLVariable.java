package dyvilx.tools.repl.context;

import dyvil.annotation.internal.NonNull;
import dyvil.array.ObjectArray;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.repl.DyvilREPL;

import java.lang.reflect.InvocationTargetException;

public class REPLVariable extends Field
{
	private REPLContext context;

	private Class<?> runtimeClass;
	private Object   displayValue;

	public REPLVariable(REPLContext context, Name name, IValue value)
	{
		this(context, value.getPosition(), name, Types.UNKNOWN, AttributeList.of(Modifiers.FINAL));
		this.setValue(value);
	}

	public REPLVariable(REPLContext context, SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(null, position, name, type, attributes);
		this.context = context;
	}

	protected void setRuntimeClass(Class<?> theClass)
	{
		this.runtimeClass = theClass;
	}

	protected void updateValue(DyvilREPL repl)
	{
		if (this.runtimeClass == null || this.type == Types.VOID)
		{
			return;
		}

		final Object result;
		if (this.property != null)
		{
			try
			{
				final java.lang.reflect.Method method = this.runtimeClass.getDeclaredMethod(this.getInternalName());
				method.setAccessible(true);
				result = method.invoke(null);
			}
			catch (InvocationTargetException ex)
			{
				ex.getCause().printStackTrace(repl.getOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
			catch (ReflectiveOperationException ex)
			{
				ex.printStackTrace(repl.getErrorOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
		}
		else
		{
			try
			{
				final java.lang.reflect.Field field = this.runtimeClass.getDeclaredField(this.getInternalName());
				field.setAccessible(true);
				result = field.get(null);
			}
			catch (ReflectiveOperationException ex)
			{
				ex.printStackTrace(repl.getErrorOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
		}

		this.value = IValue.fromObject(result);
		this.displayValue = result;
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (!Types.isVoid(this.type))
		{
			super.check(markers, context);
			return;
		}

		this.type = Types.UNKNOWN;
		super.check(markers, context);
		this.type = Types.VOID;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (!Types.isVoid(this.type))
		{
			super.write(writer);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (!Types.isVoid(this.type))
		{
			super.writeStaticInit(writer);
			return;
		}
		if (this.value != null)
		{
			this.value.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, this.enclosingClass.getInternalName(), this.getInternalName(),
		                      this.getDescriptor());
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.PUTSTATIC, this.enclosingClass.getInternalName(), this.getInternalName(),
		                      this.getDescriptor());
	}

	@Override
	protected void valueToString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		Formatting.appendSeparator(buffer, "field.assignment", '=');

		if (this.value != null)
		{
			this.value.toString(indent, buffer);
			return;
		}

		try
		{
			ObjectArray.arrayToString(this.displayValue, buffer);
		}
		catch (Throwable t)
		{
			t.printStackTrace(this.context.getCompilationContext().getErrorOutput());
			buffer.append("<error>");
		}
	}
}

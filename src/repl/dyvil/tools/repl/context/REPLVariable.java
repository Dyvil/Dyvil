package dyvil.tools.repl.context;

import dyvil.array.ObjectArray;
import dyvil.io.Console;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.repl.DyvilREPL;

import java.lang.reflect.InvocationTargetException;

public class REPLVariable extends Field
{
	private REPLContext context;

	protected String   bytecodeName;
	private   Class<?> runtimeClass;
	private   Object   displayValue;

	public REPLVariable(REPLContext context, ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                   AnnotationList annotations)
	{
		super(null, position, name, type, modifiers, annotations);
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
				final java.lang.reflect.Method method = this.runtimeClass.getDeclaredMethod(this.name.qualified);
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
				final java.lang.reflect.Field field = this.runtimeClass.getDeclaredFields()[0];
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

	private boolean isConstant()
	{
		return this.hasModifier(Modifiers.FINAL) && this.value != null && isConstant(this.value);
	}

	private static boolean isConstant(IValue value)
	{
		int tag = value.valueTag();
		return tag >= 0 && tag != IValue.NIL && tag < IValue.STRING;
	}

	public String getBytecodeName()
	{
		if (this.bytecodeName != null)
		{
			return this.bytecodeName;
		}
		return this.bytecodeName = this.name.qualified;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final String name = this.getBytecodeName();
		final String descriptor = this.getDescriptor();
		final String methodType = "()" + descriptor;

		if (this.type != Types.VOID)
		{
			// Generate the field holding the value
			writer.visitField(this.modifiers.toFlags(), name, descriptor, null, null);
		}

		// Write the repl$compute$... method
		if (this.value != null)
		{
			final MethodWriter computeWriter = new MethodWriterImpl(writer, writer.visitMethod(
				Modifiers.PRIVATE | Modifiers.STATIC, "repl$compute$" + name, methodType, null, null));
			computeWriter.visitCode();
			this.value.writeExpression(computeWriter, this.type);
			computeWriter.visitEnd(this.type);
		}

		// Write the property, if necessary
		if (this.property != null)
		{
			this.property.write(writer);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		final String name = this.getBytecodeName();
		final String descriptor = this.getDescriptor();
		final String methodType = "()" + descriptor;
		final String owner = this.enclosingClass.getInternalName();

		// Write a call to the repl$compute$... method
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "repl$compute$" + name, methodType, false);
		if (this.type != Types.VOID)
		{
			// Store the value to the field
			writer.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, descriptor);
		}

		if (this.property != null)
		{
			this.property.writeStaticInit(writer);
		}
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.isConstant())
		{
			this.value.writeExpression(writer, this.type);
			return;
		}

		writer.visitFieldInsn(Opcodes.GETSTATIC, this.enclosingClass.getInternalName(), this.getBytecodeName(),
		                      this.getDescriptor());
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.PUTSTATIC, this.enclosingClass.getInternalName(), this.getBytecodeName(),
		                      this.getDescriptor());
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		final boolean colors = this.context.getCompilationContext().config.useAnsiColors();

		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}

		this.modifiers.toString(this.getKind(), buffer);

		this.type.toString(prefix, buffer);
		buffer.append(' ');

		if (colors)
		{
			buffer.append(Console.ANSI_BLUE);
			buffer.append(this.name);
			buffer.append(Console.ANSI_RESET);
		}
		else
		{
			buffer.append(this.name);
		}

		Formatting.appendSeparator(buffer, "field.assignment", '=');

		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
			return;
		}

		try
		{
			ObjectArray.toString(this.displayValue, buffer);
		}
		catch (Throwable t)
		{
			t.printStackTrace(this.context.getCompilationContext().getErrorOutput());
			buffer.append("<error>");
		}
	}
}

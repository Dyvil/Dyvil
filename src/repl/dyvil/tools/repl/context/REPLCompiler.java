package dyvil.tools.repl.context;

import dyvil.io.FileUtils;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.repl.DyvilREPL;

import java.io.File;
import java.security.ProtectionDomain;

public class REPLCompiler
{
	protected static final ClassLoader      CLASS_LOADER      = REPLCompiler.class.getClassLoader();
	private static final   ProtectionDomain PROTECTION_DOMAIN = REPLCompiler.class.getProtectionDomain();

	protected static Class compile(DyvilREPL repl, IClass iclass)
	{
		try
		{
			ClassWriter cw = new ClassWriter(ClassFormat.ASM_VERSION);
			iclass.write(cw);
			cw.visitEnd();
			byte[] bytes = cw.toByteArray();
			return loadClass(repl, iclass.getInternalName(), bytes);
		}
		catch (Throwable t)
		{
			t.printStackTrace(repl.getOutput());
			return null;
		}
	}

	private static void dumpClass(DyvilREPL repl, String name, byte[] bytes)
	{
		int index = name.lastIndexOf('/');
		String fileName;
		if (index <= 0)
		{
			fileName = name + ".class";
		}
		else
		{
			fileName = name.substring(index + 1) + ".class";
		}

		FileUtils.tryWrite(new File(repl.getDumpDir(), fileName), bytes);
	}

	protected static Class loadClass(DyvilREPL repl, String name, byte[] bytes)
	{
		if (repl.getDumpDir() != null)
		{
			dumpClass(repl, name, bytes);
		}

		final Class<?> theClass;
		try
		{
			theClass = ReflectUtils.UNSAFE.defineClass(name.replace('/', '.'), bytes, 0, bytes.length, CLASS_LOADER,
			                                           PROTECTION_DOMAIN);
		}
		catch (Throwable throwable)
		{
			// Verification Error
			throwable.printStackTrace(repl.getErrorOutput());
			return null;
		}

		return initialize(repl, theClass);
	}

	protected static Class loadAnonymousClass(DyvilREPL repl, String name, byte[] bytes)
	{
		if (repl.getDumpDir() != null)
		{
			dumpClass(repl, name, bytes);
		}

		final Class<?> theClass;
		try
		{
			theClass = ReflectUtils.UNSAFE.defineAnonymousClass(REPLVariable.class, bytes, null);
		}
		catch (Throwable throwable)
		{
			// Verification Error
			throwable.printStackTrace(repl.getErrorOutput());
			return null;
		}

		return initialize(repl, theClass);
	}

	private static Class<?> initialize(DyvilREPL repl, Class<?> theClass)
	{
		try
		{
			ReflectUtils.UNSAFE.ensureClassInitialized(theClass);
			return theClass;
		}
		catch (ExceptionInInitializerError initializerError)
		{
			final Throwable cause = initializerError.getCause();
			filterStackTrace(cause);
			cause.printStackTrace(repl.getOutput());
		}
		catch (Throwable throwable)
		{
			filterStackTrace(throwable);
			throwable.printStackTrace(repl.getOutput());
		}
		return null;
	}

	protected static void filterStackTrace(Throwable throwable)
	{
		StackTraceElement[] traceElements = throwable.getStackTrace();
		int count = traceElements.length;
		int lastIndex = count - 1;

		for (; lastIndex >= 0; --lastIndex)
		{
			if (traceElements[lastIndex].getClassName().startsWith("sun.misc.Unsafe"))
			{
				--lastIndex;
				break;
			}
		}

		StackTraceElement[] newTraceElements = new StackTraceElement[lastIndex + 1];
		System.arraycopy(traceElements, 0, newTraceElements, 0, lastIndex + 1);

		throwable.setStackTrace(newTraceElements);

		Throwable cause = throwable.getCause();
		if (cause != null)
		{
			filterStackTrace(cause);
		}

		for (Throwable suppressed : throwable.getSuppressed())
		{
			filterStackTrace(suppressed);
		}
	}
}

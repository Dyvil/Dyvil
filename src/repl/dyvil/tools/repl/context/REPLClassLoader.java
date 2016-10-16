package dyvil.tools.repl.context;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.io.FileUtils;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.header.ICompilable;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.repl.DyvilREPL;

import java.io.File;
import java.io.IOException;

public class REPLClassLoader extends ClassLoader
{
	private final DyvilREPL repl;

	private Map<String, ICompilable> compilables = new HashMap<>();

	public REPLClassLoader(DyvilREPL repl)
	{
		super(REPLClassLoader.class.getClassLoader());
		this.repl = repl;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		final ICompilable compilable = this.compilables.removeKey(name);
		if (compilable == null)
		{
			return super.findClass(name);
		}

		try
		{
			final byte[] bytecode = ClassWriter.compile(compilable);
			dumpClass(this.repl, compilable, bytecode);
			return this.defineClass(name, bytecode, 0, bytecode.length);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace(this.repl.getErrorOutput());
			return super.findClass(name);
		}
	}

	public void register(ICompilable iclass)
	{
		final String fullName = iclass.getFullName();
		this.compilables.put(fullName, iclass);
	}

	private static void dumpClass(DyvilREPL repl, ICompilable compilable, byte[] bytes)
	{
		final File dumpDir = repl.getDumpDir();
		if (dumpDir == null)
		{
			return;
		}

		final String fileName = compilable.getFileName();
		try
		{
			FileUtils.write(new File(repl.getDumpDir(), fileName), bytes);
		}
		catch (IOException e)
		{
			e.printStackTrace(repl.getErrorOutput());
		}
	}

	public Class<?> initialize(ICompilable compilable)
	{
		try
		{
			return this.initialize(this.loadClass(compilable.getFullName()));
		}
		catch (ClassNotFoundException ignored)
		{
			return null;
		}
	}

	private Class<?> initialize(Class<?> theClass)
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
			cause.printStackTrace(this.repl.getErrorOutput());
		}
		catch (Throwable throwable)
		{
			filterStackTrace(throwable);
			throwable.printStackTrace(this.repl.getErrorOutput());
		}
		return null;
	}

	private static void filterStackTrace(Throwable throwable)
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

	public static Class loadAnonymousClass(DyvilREPL repl, byte[] bytes)
	{
		try
		{
			return ReflectUtils.UNSAFE.defineAnonymousClass(REPLVariable.class, bytes, null);
		}
		catch (Throwable throwable)
		{
			// Verification Error
			throwable.printStackTrace(repl.getErrorOutput());
			return null;
		}
	}
}

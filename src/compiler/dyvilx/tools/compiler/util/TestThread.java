package dyvilx.tools.compiler.util;

import dyvil.collection.List;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.library.Library;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public final class TestThread extends Thread
{
	private final DyvilCompiler compiler;

	public TestThread(DyvilCompiler compiler)
	{
		this.compiler = compiler;
	}

	@Override
	public void run()
	{
		final String mainClassName = this.compiler.config.getMainType();
		final String[] args = this.compiler.config.getMainArgs();

		try
		{
			final long startTime = System.nanoTime();

			final Class<?> mainClass = Class.forName(mainClassName, false, this.createClassLoader());
			final Method mainMethod = mainClass.getMethod("main", String[].class);
			mainMethod.invoke(null, new Object[] { args });

			final long endTime = System.nanoTime();

			this.compiler.log(I18n.get("test.completed", Util.toTime(endTime - startTime)));
		}
		catch (VerifyError verifyError)
		{
			final String message = "BYTECODE VERIFICATION FAILED\n\n" + "Main Type: " + mainClassName + '\n' +
				                       "Main Args: " + Arrays.toString(args) +
				                       "\n\n----- ERROR -----\n";

			this.compiler.error(message, verifyError);
		}
		catch (InvocationTargetException ex)
		{
			this.failTest(mainClassName, args, ex.getCause());
		}
		catch (Throwable ex)
		{
			this.failTest(mainClassName, args, ex);
		}
	}

	private ClassLoader createClassLoader() throws MalformedURLException
	{
		final List<Library> libraries = this.compiler.config.libraries;
		final URL[] urls = new URL[1 + libraries.size()];
		urls[0] = this.compiler.config.getOutputDir().toURI().toURL();

		int index = 1;
		for (Library library : libraries)
		{
			urls[index++] = library.getURL();
		}

		return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
	}

	private void failTest(String mainType, String[] args, Throwable ex)
	{
		final String message = "Test Failed\n\n" + "Main Type: " + mainType + '\n' +
			                       "Main Args: " + Arrays.toString(args) +
			                       "\n\n----- ERROR -----\n";
		this.compiler.error(message, ex);
	}
}

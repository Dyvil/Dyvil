package dyvil.tools.compiler.util;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Level;

import dyvil.lang.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.library.Library;

public final class TestThread extends Thread
{
	private static ClassLoader createClassLoader() throws MalformedURLException
	{
		List<Library> libraries = DyvilCompiler.config.libraries;
		URL[] urls = new URL[1 + libraries.size()];
		urls[0] = DyvilCompiler.config.outputDir.toURI().toURL();
		
		int index = 1;
		for (Library l : libraries)
		{
			urls[index++] = l.file.toURI().toURL();
		}
		
		return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
	}
	
	@Override
	public void run()
	{
		String mainType = DyvilCompiler.config.mainType;
		String[] args = DyvilCompiler.config.getMainArgs();
		PrintStream out = System.out;
		PrintStream err = System.err;
		
		System.setOut(DyvilCompiler.loggerOut);
		System.setErr(DyvilCompiler.loggerErr);
		try
		{
			long now = System.currentTimeMillis();
			Class c = Class.forName(mainType, false, createClassLoader());
			Method m = c.getMethod("main", String[].class);
			m.invoke(null, new Object[] { args });
			
			now = System.currentTimeMillis() - now;
			
			System.setOut(out);
			System.setErr(err);
			
			DyvilCompiler.logger.log(Level.INFO, "Test completed (" + Util.toTime(now) + ")");
			
			return;
		}
		catch (VerifyError ve)
		{
			System.setOut(out);
			System.setErr(err);
			
			StringBuilder builder = new StringBuilder("BYTECODE VERIFICATION FAILED\n\n");
			builder.append("Main Type: ").append(mainType).append('\n');
			builder.append("Main Args: ").append(Arrays.toString(args));
			builder.append("\n\n----- ERROR -----\n");
			DyvilCompiler.logger.log(Level.SEVERE, builder.toString(), ve);
			
			return;
		}
		catch (InvocationTargetException ex)
		{
			System.setOut(out);
			System.setErr(err);
			
			DyvilCompiler.logger.log(Level.SEVERE, "TEST EXCEPTION", ex.getCause());
			return;
		}
		catch (Throwable ex)
		{
			System.setOut(out);
			System.setErr(err);
			
			StringBuilder builder = new StringBuilder("TEST FAILED\n\n");
			builder.append("Main Type: ").append(mainType).append('\n');
			builder.append("Main Args: ").append(Arrays.toString(args));
			builder.append("\n\n----- ERROR -----\n");
			DyvilCompiler.logger.log(Level.SEVERE, builder.toString(), ex);
			
			return;
		}
	}
}

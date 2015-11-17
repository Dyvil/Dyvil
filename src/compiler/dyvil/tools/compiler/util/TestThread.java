package dyvil.tools.compiler.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import dyvil.collection.List;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.library.Library;

public final class TestThread extends Thread
{
	private static ClassLoader createClassLoader() throws MalformedURLException
	{
		List<Library> libraries = DyvilCompiler.config.libraries;
		URL[] urls = new URL[1 + libraries.size()];
		urls[0] = DyvilCompiler.config.getOutputDir().toURI().toURL();
		
		int index = 1;
		for (Library l : libraries)
		{
			urls[index++] = l.getURL();
		}
		
		return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
	}
	
	@Override
	public void run()
	{
		String mainType = DyvilCompiler.config.getMainType();
		String[] args = DyvilCompiler.config.getMainArgs();
		
		try
		{
			long now = System.currentTimeMillis();
			Class c = Class.forName(mainType, false, createClassLoader());
			Method m = c.getMethod("main", String[].class);
			m.invoke(null, new Object[] { args });
			
			now = System.currentTimeMillis() - now;
			
			DyvilCompiler.log("Test completed (" + Util.toTime(now) + ")");
			
			return;
		}
		catch (VerifyError ve)
		{
			StringBuilder builder = new StringBuilder("BYTECODE VERIFICATION FAILED\n\n");
			builder.append("Main Type: ").append(mainType).append('\n');
			builder.append("Main Args: ").append(Arrays.toString(args));
			builder.append("\n\n----- ERROR -----\n");
			DyvilCompiler.error(builder.toString(), ve);
			
			return;
		}
		catch (InvocationTargetException ex)
		{
			failTest(mainType, args, ex.getCause());
			return;
		}
		catch (Throwable ex)
		{
			failTest(mainType, args, ex);
			return;
		}
	}
	
	private static void failTest(String mainType, String[] args, Throwable ex)
	{
		StringBuilder builder = new StringBuilder("Test Failed\n\n");
		builder.append("Main Type: ").append(mainType).append('\n');
		builder.append("Main Args: ").append(Arrays.toString(args));
		builder.append("\n\n----- ERROR -----\n");
		DyvilCompiler.error(builder.toString(), ex);
	}
	
	private static void testError(Throwable t)
	{
	
	}
}

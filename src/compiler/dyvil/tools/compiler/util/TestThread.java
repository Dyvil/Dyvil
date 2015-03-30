package dyvil.tools.compiler.util;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;

import dyvil.tools.compiler.DyvilCompiler;

public final class TestThread extends Thread
{
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
			
			Class c = Class.forName(mainType);
			Method m = c.getMethod("main", String[].class);
			m.invoke(null, new Object[] { args });
			
			now = System.currentTimeMillis() - now;
			
			DyvilCompiler.logger.log(Level.INFO, "Test completed (" + Util.toTime(now) + ")");
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
		
		System.setOut(out);
		System.setErr(err);
	}
}

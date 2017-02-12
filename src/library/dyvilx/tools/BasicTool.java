package dyvilx.tools;

import dyvil.io.AppendablePrintStream;
import dyvil.io.BasicPrintStream;
import dyvil.io.Console;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.logging.*;

public abstract class BasicTool implements javax.tools.Tool
{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private PrintStream output;
	private PrintStream errorOutput;
	private Logger      logger;

	private int exitCode;

	protected abstract boolean useAnsiColors();

	@Override
	public java.util.Set<SourceVersion> getSourceVersions()
	{
		return EnumSet.allOf(SourceVersion.class);
	}

	protected void initOutput(OutputStream out, OutputStream err)
	{
		this.setOutput(BasicPrintStream.apply(out, System.out));
		this.setErrorOutput(BasicPrintStream.apply(err, System.err));
	}

	protected void initLogger(File logFile, boolean debug)
	{
		if (logFile == null)
		{
			return;
		}

		this.logger = Logger.getLogger("DYVIL-COMPILER");
		this.logger.setUseParentHandlers(false);
		this.logger.setLevel(Level.ALL);

		final Formatter formatter = new Formatter()
		{
			@Override
			public String format(LogRecord record)
			{
				String message = record.getMessage();
				if (message == null || message.isEmpty())
				{
					return "\n";
				}

				Throwable thrown = record.getThrown();
				StringBuilder builder = new StringBuilder();

				if (debug)
				{
					builder.append('[').append(DATE_FORMAT.format(new Date(record.getMillis()))).append("] [");
					builder.append(record.getLevel()).append("]: ");
				}
				builder.append(message).append('\n');

				if (thrown != null)
				{
					thrown.printStackTrace(new AppendablePrintStream(builder));
				}
				return builder.toString();
			}
		};

		try
		{
			final FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(formatter);
			this.logger.addHandler(fileHandler);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public PrintStream getOutput()
	{
		return this.output;
	}

	public void setOutput(PrintStream output)
	{
		this.output = output;
	}

	public PrintStream getErrorOutput()
	{
		return this.errorOutput;
	}

	public void setErrorOutput(PrintStream errorOutput)
	{
		this.errorOutput = errorOutput;
	}

	public int getExitCode()
	{
		return this.exitCode;
	}

	public void setExitCode(int exitCode)
	{
		this.exitCode = exitCode;
	}

	public void fail()
	{
		this.exitCode = 1;
	}

	public void log(String message)
	{
		this.output.println(message);
		if (this.logger != null)
		{
			this.logger.info(message);
		}
	}

	public void warn(String message)
	{
		if (this.useAnsiColors() && !message.isEmpty())
		{
			this.output.println(Console.ANSI_YELLOW + message + Console.ANSI_RESET);
		}
		else
		{
			this.output.println(message);
		}

		if (this.logger != null)
		{
			this.logger.warning(message);
		}
	}

	public void error(String message)
	{
		this.fail();
		this.errorOutput.println(message);

		if (this.logger != null)
		{
			this.logger.severe(message);
		}
	}

	public void error(String message, Throwable throwable)
	{
		this.fail();
		this.errorOutput.println(message);

		throwable.printStackTrace(this.errorOutput);
		if (this.logger != null)
		{
			this.logger.log(Level.SEVERE, message, throwable);
		}
	}

	public void error(String className, String methodName, Throwable throwable)
	{
		this.fail();

		throwable.printStackTrace(this.errorOutput);
		if (this.logger != null)
		{
			this.logger.throwing(className, methodName, throwable);
		}
	}
}

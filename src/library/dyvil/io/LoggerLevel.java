package dyvil.io;

import dyvil.annotation.internal.NonNull;

import java.util.logging.Level;

public class LoggerLevel extends Level
{
	private static final long serialVersionUID = -8829261068161314749L;

	public LoggerLevel(@NonNull String name, int level)
	{
		super(name, level);
	}
}

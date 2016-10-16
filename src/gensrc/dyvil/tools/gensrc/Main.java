package dyvil.tools.gensrc;

import java.io.File;

public class Main
{
	public static final String TARGET_PREFIX = "target=";
	public static final String SOURCE_PREFIX = "source=";

	public static void main(String[] args)
	{
		final GenSrc instance = new GenSrc(System.out, System.err);

		if (processArguments(args, instance))
		{
			instance.findFiles();
			instance.processTemplates();
		}
	}

	private static boolean processArguments(String[] args, GenSrc gensrc)
	{
		String sourceDir = null;
		String targetDir = null;
		for (int i = 0, size = args.length; i < size; i++)
		{
			final String arg = args[i];

			switch (arg)
			{
			case "-s":
			case "--source":
				if (++i == size)
				{
					System.out.println("Invalid -s argument: Source Directory expected");
				}
				else
				{
					sourceDir = args[i];
				}
				continue;
			case "-t":
			case "--target":
				if (++i == size)
				{
					System.out.println("Invalid -t argument: Target Directory expected");
				}
				continue;
			case "--ansi":
				gensrc.setAnsiColors(true);
				continue;
			}
			if (arg.startsWith(SOURCE_PREFIX))
			{
				sourceDir = arg.substring(SOURCE_PREFIX.length());
			}
			else if (arg.startsWith(TARGET_PREFIX))
			{
				targetDir = arg.substring(TARGET_PREFIX.length());
			}
			else if (sourceDir == null)
			{
				sourceDir = arg;
			}
			else if (targetDir == null)
			{
				targetDir = arg;
			}
			else
			{
				System.out.println("Invalid Argument: " + arg);
			}
		}

		if (sourceDir == null)
		{
			System.out.println("Missing Source Directory");
			return false;
		}
		if (targetDir == null)
		{
			System.out.println("Missing Target Directory");
			return false;
		}

		gensrc.setSourceRoot(new File(sourceDir));
		gensrc.setTargetRoot(new File(targetDir));
		return true;
	}
}

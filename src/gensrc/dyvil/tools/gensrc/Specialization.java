package dyvil.tools.gensrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Specialization
{
	public static final String FILE_NAME_PROPERTY  = "@fileName";
	public static final String ENABLED_PROPERTY    = "@enabled";
	public static final String GEN_NOTICE_PROPERTY = "GEN_NOTICE";
	public static final String TIME_STAMP_PROPERTY = "TIME_STAMP";

	public static final String GEN_NOTICE = "GENERATED SOURCE - DO NOT EDIT";

	private Properties substitutions = new Properties();

	private Specialization parent;

	private final File   sourceFile;
	private final String templateName;
	private final String name;

	private Template template;

	public Specialization(File sourceFile, String templateName, String specName)
	{
		this.sourceFile = sourceFile;
		this.templateName = templateName;
		this.name = specName;
	}

	public File getSourceFile()
	{
		return this.sourceFile;
	}

	public String getTemplateName()
	{
		return this.templateName;
	}

	public Template getTemplate()
	{
		return this.template;
	}

	public void setTemplate(Template template)
	{
		this.template = template;
	}

	public String getName()
	{
		return this.name;
	}

	public String getFileName()
	{
		return this.getSubstitution(FILE_NAME_PROPERTY);
	}

	public boolean isEnabled()
	{
		final String enabled = this.getSubstitution(ENABLED_PROPERTY);
		return enabled == null || "true".equals(enabled);
	}

	public String getSubstitution(String key)
	{
		final String sub = this.substitutions.getProperty(key);
		if (sub != null || this.parent == null)
		{
			return sub;
		}
		return this.parent.getSubstitution(key);
	}

	public void read(File sourceRoot, Map<File, Specialization> specs)
	{
		initDefaults(this.substitutions);

		try (BufferedReader reader = Files.newBufferedReader(this.sourceFile.toPath()))
		{
			this.substitutions.load(reader);
		}
		catch (IOException e)
		{
			// TODO better error handling
			e.printStackTrace();
		}

		final String inherited = this.getSubstitution("inheritFrom");
		if (inherited == null)
		{
			return;
		}

		// If the referenced file starts with '.', it is relative to the parent directory of this spec file
		// Otherwise, it is relative to the source root
		final File specFile = inherited.startsWith(".") ?
			                      new File(this.getSourceFile().getParent(), inherited) :
			                      new File(sourceRoot, inherited);
		final Specialization spec = specs.get(specFile);
		if (spec == null)
		{
			System.out.printf("Unable to resolve and inherit specialization '%s'\n", specFile);
			return;
		}

		this.parent = spec;
	}

	private static void initDefaults(Properties substitutions)
	{
		substitutions.put(GEN_NOTICE_PROPERTY, GEN_NOTICE);
		substitutions.put(TIME_STAMP_PROPERTY, DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()));
	}

	public void processLines(List<String> lines, PrintStream writer)
	{
		for (String line : lines)
		{
			writer.println(this.processLine(line));
		}
	}

	private String processLine(String line)
	{
		final int length = line.length();
		if (length == 0)
		{
			return line;
		}

		final StringBuilder builder = new StringBuilder(length);

		int prev = 0;

		for (int i = 0; i < length; )
		{
			final char c = line.charAt(i);

			if (c == '#' && i + 1 < length && line.charAt(i + 1) == '#')
			{
				// two consecutive ## are stripped

				// append contents before this identifier
				builder.append(line, prev, i);
				i = prev = i + 2; // advance by two characters
				continue;
			}
			if (!Character.isJavaIdentifierStart(c))
			{
				// advance to the first identifier start character
				i++;
				continue;
			}

			// append contents before this identifier
			builder.append(line, prev, i);

			// index of the first character that is not part of this identifier
			final int nextIndex = findNextIndex(line, i + 1, length);
			final String key = line.substring(i, nextIndex);
			final String replacement = this.getSubstitution(key);

			if (replacement != null)
			{
				builder.append(replacement); // append the replacement instead of the identifier
			}
			else
			{
				builder.append(line, i, nextIndex); // append the original identifier
			}
			i = prev = nextIndex;
		}

		// append remaining characters on line
		builder.append(line, prev, length);
		return builder.toString();
	}

	private static int findNextIndex(String line, int startIndex, int length)
	{
		for (; startIndex < length; startIndex++)
		{
			if (!Character.isJavaIdentifierPart(line.charAt(startIndex)))
			{
				return startIndex;
			}
		}
		return length;
	}
}

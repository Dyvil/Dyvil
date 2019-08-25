package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.config.CompilerConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;

public class GenSrcConfig extends CompilerConfig
{
	private File genSrcDir;

	public GenSrcConfig(GenSrc gensrc)
	{
		super(gensrc);
	}

	public File getGenSrcDir()
	{
		return this.genSrcDir;
	}

	public void setGenSrcDir(File genSrcDir)
	{
		this.genSrcDir = genSrcDir;
	}

	@Override
	public void addOptions(Options options)
	{
		super.addOptions(options);

		options.addOption("g", "gensrc-dir", true, "the target directory for generated sources");
	}

	@Override
	public void readOptions(CommandLine cmd)
	{
		super.readOptions(cmd);

		if (cmd.hasOption("gensrc-dir"))
		{
			this.setGenSrcDir(new File(cmd.getOptionValue("gensrc-dir")));
		}
	}
}

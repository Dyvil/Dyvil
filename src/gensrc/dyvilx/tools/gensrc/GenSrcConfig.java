package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.config.CompilerConfig;

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
	public boolean setProperty(String name, String value)
	{
		if ("gensrc_dir".equals(name))
		{
			this.setGenSrcDir(new File(value));
			return true;
		}

		return super.setProperty(name, value);
	}
}

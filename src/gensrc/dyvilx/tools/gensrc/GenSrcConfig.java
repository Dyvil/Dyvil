package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.config.CompilerConfig;

public class GenSrcConfig extends CompilerConfig
{
	private String genSrcDir;

	public GenSrcConfig(GenSrc gensrc)
	{
		super(gensrc);
	}

	public String getGenSrcDir()
	{
		return this.genSrcDir;
	}

	public void setGenSrcDir(String genSrcDir)
	{
		this.genSrcDir = genSrcDir;
	}

	@Override
	public boolean setProperty(String name, String value)
	{
		if ("gensrc_dir".equals(name))
		{
			this.setGenSrcDir(value);
			return true;
		}

		return super.setProperty(name, value);
	}
}

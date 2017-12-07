package dyvilx.tools.gensrc;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.sources.GenSrcFileType;

import java.io.File;

public class GenSrc extends DyvilCompiler
{
	private List<File>     specs     = new ArrayList<>();
	private List<Template> templates = new ArrayList<>();

	@Override
	protected CompilerConfig createConfig()
	{
		return new GenSrcConfig(this);
	}

	@Override
	protected void setupFileFinder()
	{
		this.fileFinder.registerFileType(GenSrcFileType.TEMPLATE_EXTENSION, GenSrcFileType.TEMPLATE);
		this.fileFinder.registerFileType(GenSrcFileType.SPEC_EXTENSION, GenSrcFileType.SPEC);

		this.fileFinder.registerFileType(GenSrcFileType.CODE_EXTENSION, DyvilFileType.DYVIL_UNIT);
		this.fileFinder.registerFileType(GenSrcFileType.HEADER_EXTENSION, DyvilFileType.DYVIL_HEADER);
	}

	public void addSpec(File file)
	{
		this.specs.add(file);
	}

	public void addTemplate(Template template)
	{
		this.templates.add(template);
	}

	@Override
	public void test()
	{
		if (this.config.getMainType() == null)
		{
			this.config.setMainType("dyvilx.tools.gensrc.Runner");

			final List<String> mainArgs = this.config.getMainArgs();
			mainArgs.clear();
			mainArgs.add("output_dir=" + ((GenSrcConfig) this.config).getGenSrcDir());
			for (Template template : this.templates)
			{
				final String internalName = template.getInternalName();

				mainArgs.add("-t");
				mainArgs.add(template.getFullName());

				for (File spec : this.specs)
				{
					final String path = spec.getPath();
					if (path.contains(internalName))
					{
						mainArgs.add(path);
					}
				}
			}
		}
		super.test();
	}
}

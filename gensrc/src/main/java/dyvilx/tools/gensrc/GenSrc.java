package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.sources.GenSrcFileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
		if (this.config.getMainType() != null)
		{
			super.test();
			return;
		}

		this.config.setMainType("dyvilx.tools.gensrc.Runner");

		final List<String> mainArgs = this.config.getMainArgs();
		mainArgs.clear();

		for (File sourceDir : this.config.sourceDirs)
		{
			mainArgs.add("source_dir=" + sourceDir);
		}

		mainArgs.add("output_dir=" + ((GenSrcConfig) this.config).getGenSrcDir());

		for (Template template : this.templates)
		{
			final String templateName = template.getTemplateName();

			mainArgs.add("-t");
			mainArgs.add(template.getFullName());

			for (File spec : this.specs)
			{
				final String specName = spec.getPath();
				if (dyvilx.tools.gensrc.Template.nameMatches(templateName, specName))
				{
					mainArgs.add(specName);
				}
			}
		}
		super.test();
	}
}

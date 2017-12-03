package dyvilx.tools.gensrc;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.sources.GenSrcFileType;

import java.io.File;

public class GenSrc extends DyvilCompiler
{
	private List<File>     specs     = new ArrayList<>();
	private List<Template> templates = new ArrayList<>();

	@Override
	protected void setupFileFinder()
	{
		this.fileFinder.registerFileType(GenSrcFileType.TEMPLATE_EXTENSION, GenSrcFileType.TEMPLATE);
		this.fileFinder.registerFileType(GenSrcFileType.SPEC_EXTENSION, GenSrcFileType.SPEC);
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
			this.config.setMainType("GenerateSources");

			final List<String> mainArgs = this.config.getMainArgs();
			mainArgs.clear();
			for (File spec : this.specs)
			{
				// the paths of the specs become the main arguments
				mainArgs.add(spec.getPath());
			}
		}
		super.test();
	}
}

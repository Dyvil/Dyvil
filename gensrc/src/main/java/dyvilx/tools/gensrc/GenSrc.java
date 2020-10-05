package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.DyvilCompiler;
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
}

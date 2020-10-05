package dyvilx.tools.gensrc;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.gensrc.sources.GenSrcFileType;

public class GenSrc extends DyvilCompiler
{
	@Override
	protected void setupFileFinder()
	{
		this.fileFinder.registerFileType(GenSrcFileType.TEMPLATE_EXTENSION, GenSrcFileType.TEMPLATE);
		this.fileFinder.registerFileType(GenSrcFileType.SPEC_EXTENSION, GenSrcFileType.SPEC);

		this.fileFinder.registerFileType(GenSrcFileType.CODE_EXTENSION, DyvilFileType.DYVIL_UNIT);
		this.fileFinder.registerFileType(GenSrcFileType.HEADER_EXTENSION, DyvilFileType.DYVIL_HEADER);
	}
}

package dyvilx.tools.gensrc.sources;

import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.sources.FileType;
import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.lang.I18n;

public class GenSrcFileType extends DyvilFileType
{
	public static final String TEMPLATE_EXTENSION = ".dgt";
	public static final String SPEC_EXTENSION     = ".dgs";
	public static final String CODE_EXTENSION     = ".dgc";
	public static final String HEADER_EXTENSION   = ".dgh";

	public static final FileType TEMPLATE = new GenSrcFileType("template", Template::new);

	public static final FileType SPEC = new GenSrcFileType("spec", (compiler, pack, inputFile, outputFile) -> null);

	public GenSrcFileType(String identifier, DyvilFileType.HeaderSupplier headerSupplier)
	{
		super(identifier, headerSupplier);
	}

	@Override
	public String getLocalizedName()
	{
		return I18n.get("unit.filetype." + this.identifier);
	}
}

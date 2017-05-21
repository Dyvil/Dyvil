package dyvil.tools.gensrc.ast.scope;

import dyvil.tools.gensrc.ast.Specialization;
import dyvil.tools.gensrc.ast.directive.Directive;
import dyvil.tools.gensrc.ast.directive.LiteralText;
import dyvil.tools.gensrc.lang.I18n;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemplateScope extends LazyScope
{
	public static final String GEN_NOTICE_PROPERTY = "GEN_NOTICE";
	public static final String TIME_STAMP_PROPERTY = "TIME_STAMP";

	public static final String GEN_NOTICE = I18n.get("genNotice");

	public final File sourceFile;
	private final String timeStamp = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now());

	public TemplateScope(File sourceFile, Specialization spec)
	{
		super(null);
		this.importFrom(spec);
		this.sourceFile = sourceFile;
	}

	@Override
	public Directive getParent(String key)
	{
		switch (key)
		{
		case GEN_NOTICE_PROPERTY:
			return new LiteralText(GEN_NOTICE);
		case TIME_STAMP_PROPERTY:
			return new LiteralText(this.timeStamp);
		}
		return null;
	}

	@Override
	public File getSourceFile()
	{
		return this.sourceFile;
	}

	@Override
	public Scope getGlobalParent()
	{
		return this;
	}
}

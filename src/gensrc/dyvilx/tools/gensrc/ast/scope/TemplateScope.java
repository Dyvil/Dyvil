package dyvilx.tools.gensrc.ast.scope;

import dyvilx.tools.gensrc.ast.Specialization;
import dyvilx.tools.gensrc.lang.I18n;

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
	public String getParent(String key)
	{
		switch (key)
		{
		case GEN_NOTICE_PROPERTY:
			return GEN_NOTICE;
		case TIME_STAMP_PROPERTY:
			return this.timeStamp;
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

package dyvil.tools.gensrc.ast.scope;

import java.io.File;

public interface Scope
{
	File getSourceFile();

	Scope getGlobalParent();

	default boolean isDefined(String key)
	{
		return this.getString(key) != null;
	}

	String getString(String key);

	default boolean getBoolean(String key)
	{
		final String substitution = this.getString(key);
		if (substitution == null)
		{
			return false;
		}

		switch (substitution)
		{
		case "0":
		case "false":
		case "null":
			return false;
		}
		return true;
	}
}

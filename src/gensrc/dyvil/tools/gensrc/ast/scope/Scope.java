package dyvil.tools.gensrc.ast.scope;

import java.io.File;

public interface Scope
{
	File getSourceFile();

	Scope getGlobalParent();

	String getReplacement(String key);

	default boolean isDefined(String key)
	{
		return this.getReplacement(key) != null;
	}

	default boolean getBoolean(String key)
	{
		final String substitution = this.getReplacement(key);
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

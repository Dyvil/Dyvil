package dyvil.tools.gensrc.ast.scope;

import dyvil.tools.gensrc.ast.directive.Directive;

import java.io.File;

public interface Scope
{
	File getSourceFile();

	Scope getGlobalParent();

	Directive getReplacement(String key);

	default boolean isDefined(String key)
	{
		return this.getReplacement(key) != null;
	}

	default String getString(String key)
	{
		final Directive dir = this.getReplacement(key);
		return dir.specialize(this);
	}

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

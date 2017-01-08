package dyvil.tools.parsing.ast;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.parsing.position.ICodePosition;

@SuppressWarnings("NullableProblems")
public interface IASTNode
{
	void setPosition(ICodePosition position);

	ICodePosition getPosition();

	default int getLineNumber()
	{
		ICodePosition position = this.getPosition();
		return position == null ? 0 : position.startLine();
	}

	default void expandPosition(@NonNull ICodePosition position)
	{
		final ICodePosition pos = this.getPosition();
		if (pos == null)
		{
			this.setPosition(position);
			return;
		}
		this.setPosition(pos.to(position));
	}

	static @NonNull String toString(@NonNull IASTNode node)
	{
		final StringBuilder builder = new StringBuilder();
		node.toString("", builder);
		return builder.toString();
	}

	void toString(@NonNull String prefix, @NonNull StringBuilder buffer);
}

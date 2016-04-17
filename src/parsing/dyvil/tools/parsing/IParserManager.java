package dyvil.tools.parsing;

import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public interface IParserManager
{
	TokenIterator getTokens();

	MarkerList getMarkers();

	/**
	 * Splits the given token into two parts and returns the first part with the given {@code length}. The second part
	 * is available by accessing the first parts {@link IToken#next()} method. If the length is equal to the length of
	 * the token, it is simply returned and not split.
	 *
	 * @param token
	 * 	the token to split
	 * @param length
	 * 	the length of the first part
	 *
	 * @return the first part, or the token if the length was equal to the length of the token.
	 */
	IToken split(IToken token, int length);

	/**
	 * Splits the given token and schedules the second part to be the next parsed token
	 *
	 * @param token
	 * 	the token to split
	 * @param length
	 * 	the length of the first part
	 */
	void splitJump(IToken token, int length);

	void report(IToken token, String message);

	void report(Marker error);

	void stop();

	void skip();

	void skip(int n);

	void reparse();

	void jump(IToken token);

	void setParser(Parser parser);

	Parser getParser();

	void pushParser(Parser parser);

	void pushParser(Parser parser, boolean reparse);

	void popParser();

	void popParser(boolean reparse);
}

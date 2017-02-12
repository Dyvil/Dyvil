package dyvil.tools.gensrc;

public class Main
{
	public static void main(String[] args)
	{
		final int exitCode = new GenSrc().run(System.in, System.out, System.err, args);
		System.exit(exitCode);
	}
}

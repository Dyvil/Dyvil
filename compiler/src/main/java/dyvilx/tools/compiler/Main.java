package dyvilx.tools.compiler;

public final class Main
{
	public static void main(String[] args)
	{
		System.exit(new DyvilCompiler().run(System.in, System.out, System.err, args));
	}
}

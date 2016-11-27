package dyvil.tools.gensrc.ast;

public class ForReplacementMap extends LazyReplacementMap
{
	private final Specialization forSpec;

	public ForReplacementMap(Specialization forSpec, ReplacementMap parent)
	{
		super(parent);
		this.forSpec = forSpec;
	}

	// The lookup order for this is
	// #define'd values -> values from the imported spec -> values from the parent

	@Override
	public String getParent(String key)
	{
		final String forSpecValue = this.forSpec.getReplacement(key);
		if (forSpecValue != null)
		{
			return forSpecValue;
		}
		return super.getParent(key);
	}
}

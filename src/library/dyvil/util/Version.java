package dyvil.util;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dyvil.lang.Ordered;

/**
 * The <b>Version</b> class represents a {@link Comparable comparable} and
 * {@link Ordered ordered} model for the <a href="http://semver.org">Semantic
 * Versioning</a> format.
 */
public final class Version implements Ordered<Version>, Immutable, Serializable
{
	public enum Element
	{
		MAJOR, MINOR, PATCH, PRERELEASE, BUILD;
	}
	
	private static final long serialVersionUID = 2514051844985966173L;
	
	private static final String		FORMAT	= "^(?:v)?(?:(\\d+)\\.(\\d+)\\.(\\d+))(?:-([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?(?:\\+([\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*))?$";
	private static final Pattern	PATTERN	= Pattern.compile(Version.FORMAT);
	
	private final int		major;
	private final int		minor;
	private final int		patch;
	private final String	prerelease;
	private final String	build;
	
	public Version(String version)
	{
		Matcher matcher = Version.PATTERN.matcher(version);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException("<" + version + "> does not match format " + Version.FORMAT);
		}
		
		this.major = Integer.parseInt(matcher.group(1));
		this.minor = Integer.parseInt(matcher.group(2));
		this.patch = Integer.parseInt(matcher.group(3));
		this.prerelease = matcher.group(4);
		this.build = matcher.group(5);
	}
	
	public Version(int major, int minor, int patch)
	{
		this(major, minor, patch, null, null);
	}
	
	public Version(int major, int minor, int patch, String prerelease, String build)
	{
		if (major < 0)
		{
			throw new IllegalArgumentException(Element.MAJOR + " must be positive");
		}
		if (minor < 0)
		{
			throw new IllegalArgumentException(Element.MINOR + " must be positive");
		}
		if (patch < 0)
		{
			throw new IllegalArgumentException(Element.PATCH + " must be positive");
		}
		
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.prerelease = prerelease;
		this.build = build;
	}
	
	public Version next(Version.Element element)
	{
		if (element == null)
		{
			throw new IllegalArgumentException("Invalid Element");
		}
		
		switch (element)
		{
		case MAJOR:
			return new Version(this.major + 1, 0, 0);
		case MINOR:
			return new Version(this.major, this.minor + 1, 0);
		case PATCH:
			return new Version(this.major, this.minor, this.patch + 1);
		case PRERELEASE:
			throw new IllegalArgumentException("Cannot increment prerelease information");
		case BUILD:
			throw new IllegalArgumentException("Cannot increment build information");
		default:
			return this;
		}
	}
	
	public boolean isInDevelopment()
	{
		return this.major == 0;
	}
	
	public boolean isStable()
	{
		return this.major > 0;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.major).append(".").append(this.minor).append(".").append(this.patch);
		if (this.prerelease != null)
		{
			sb.append('-').append(this.prerelease);
		}
		if (this.build != null)
		{
			sb.append('+').append(this.build);
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + this.major;
		result = prime * result + this.minor;
		result = prime * result + this.patch;
		result = prime * result + ((this.prerelease == null) ? 0 : this.prerelease.hashCode());
		result = prime * result + ((this.build == null) ? 0 : this.build.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		Version other = (Version) obj;
		if (this.major != other.major || this.minor != other.minor || this.patch != other.patch)
		{
			return false;
		}
		if (this.prerelease == null)
		{
			if (other.prerelease != null)
			{
				return false;
			}
		}
		else if (!this.prerelease.equals(other.prerelease))
		{
			return false;
		}
		if (this.build == null)
		{
			if (other.build != null)
			{
				return false;
			}
		}
		else if (!this.build.equals(other.build))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(Version other)
	{
		int cmp = this.major - other.major;
		if (cmp != 0)
		{
			return cmp;
		}
		
		cmp = this.minor - other.minor;
		if (cmp != 0)
		{
			return cmp;
		}
		
		cmp = this.patch - other.patch;
		if (cmp != 0)
		{
			return cmp;
		}
		
		if (other.prerelease != null)
		{
			return other.prerelease.compareTo(this.prerelease);
		}
		if (this.build != null && other.build == null)
		{
			return -1;
		}
		return 0;
	}
}

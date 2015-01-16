package dyvil.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * A utility class for everything related to connecting to the internet.
 * 
 * @author Clashsoft
 */
public class WebUtils
{
	/**
	 * Checks if the given website is available.
	 * 
	 * @param url
	 *            the URL
	 * @return true, if available
	 */
	public static boolean checkWebsiteAvailable(String url)
	{
		try
		{
			URL url1 = new URL(url);
			
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) url1.openConnection();
			con.setRequestMethod("HEAD");
			int response = con.getResponseCode();
			return response == HttpURLConnection.HTTP_OK;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	public static String tryReadWebsite(String url)
	{
		try
		{
			return readWebsite(url);
		}
		catch (IOException ex)
		{
		}
		return null;
	}
	
	/**
	 * Reads the given website with the URL {@code url} or downloads its
	 * contents and returns them as a {@link String}.
	 * 
	 * @param url
	 *            the URL
	 * @return the lines
	 */
	public static String readWebsite(String url) throws IOException
	{
		URL url1 = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(url1.openStream());
		ByteBuffer bytebuf = ByteBuffer.allocate(1024);
		rbc.read(bytebuf);
		return new String(bytebuf.array());
	}
	
	public static void tryDownload(String url, File output)
	{
		try
		{
			download(url, output);
		}
		catch (IOException ex)
		{
		}
	}
	
	/**
	 * Downloads a file from the given {@code url} to the given {@link File}
	 * {@code output}.
	 * 
	 * @param url
	 *            the URL
	 * @param output
	 *            the output file
	 * @throws IOException
	 */
	public static void download(String url, File output) throws IOException
	{
		URL url1 = new URL(url);
		ReadableByteChannel rbc = Channels.newChannel(url1.openStream());
		FileOutputStream fos = new FileOutputStream(output);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
}

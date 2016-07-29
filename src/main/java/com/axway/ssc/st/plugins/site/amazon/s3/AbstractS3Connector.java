package com.axway.ssc.st.plugins.site.amazon.s3;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.AmazonClientException;
import com.axway.ssc.st.plugins.site.amazon.s3.bean.S3Bean;
import com.axway.st.plugins.site.services.ProxyService;

/**
 * @author cmanda 
 */
public abstract class AbstractS3Connector implements S3Connector {

	/** The path separator. */
	public static final String SEPARATOR = "/";

	/** The FTP configuration. */
	private S3Bean mS3Configuration;

	/** The ProxyService reference. */
	private ProxyService mProxyService;

	public AbstractS3Connector(S3Bean config) {
		mS3Configuration = config;
	}

	/**
	 * Sets the ProxyService.
	 *
	 * @param service
	 *            the ProxyService.
	 */
	public void setProxyService(ProxyService service) {
		mProxyService = service;
	}

	/**
	 * Returns the ProxyService.
	 *
	 * @return the ProxyService.
	 */
	public ProxyService getProxyService() {
		return mProxyService;
	}

	/**
	 * Returns the FTP configuration bean.
	 *
	 * @return the FTP configuration
	 */
	protected S3Bean getConfiguration() {
		return mS3Configuration;
	}

	@SuppressWarnings("unused")
	protected void initProxy() {
		boolean hasSocksProxy = false;
		String zone = getConfiguration().getNetworkZone();
		if (zone != null) {
			hasSocksProxy = setProxySettings(zone);
		}
	}

	/**
	 * Set ProxyHost and ProxyPort if there is Socks Proxy for the selected DMZ
	 * Zone.
	 *
	 * @param zone
	 *            network zone selected for the Pluggable Transfer Site from the
	 *            configuration
	 * @return true if the proxy was actually set. False if there is no proxy
	 *         for this zone or there were issue with setting the proxy
	 *         settings.
	 */
	protected abstract boolean setProxySettings(String zone);

	/**
	 * Throw appropriate IOException on FtpException.
	 *
	 * @param exception
	 *            the FtpException.
	 * @throws IOException
	 *             the expected exception.
	 */
	protected void handleS3ClientException(AmazonClientException ace) throws IOException {

		IOException ioexception = new IOException(ace.getMessage());
		ioexception.initCause(ace);
		throw ioexception;
	}

	/**
	 * Checks whether is IPv6 host.
	 *
	 * @param zone
	 *            the zone name
	 * @param hostName
	 *            the host name
	 * @return whether is IPv6 host.
	 */
	boolean isIPv6Host(String zone, String hostName) {
		// The remote name resolution is tied to IPv4 only. We do not cover IPv6
		// yet
		if (zone != null && mProxyService != null) {
			if (zone.equalsIgnoreCase("Default")) {
				zone = "Private";
			}
			if (mProxyService.isRemoteDnsResolutionEnabled(zone)) {
				return false;
			}
		}
		try {
			if (InetAddress.getByName(hostName) instanceof Inet6Address) {
				return true;
			}
			return false;
		} catch (UnknownHostException e) {
			return false;
		}
	}

	/**
	 * Normalizes the given path. Trailing path separator and sequences of
	 * double separator are remove for the internal path representation. The
	 * path name can be absolute or relative.
	 *
	 * @param path
	 *            the path to normalize
	 * @return sanitized path.
	 */
	String normalizePath(String path) {
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("Empty path name not allowed.");
		}
		path = path.replace(File.separatorChar, '/');
		final String doubleSeparator = "//";
		int index = -1;
		while ((index = path.indexOf(doubleSeparator)) >= 0) {
			path = path.substring(0, index) + path.substring(index + 1);
		}
		// make sure path does not end with separator
		if (path.length() > 1 && path.endsWith(SEPARATOR)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

}

package com.axway.ssc.st.plugins.site.amazon.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.axway.st.plugins.site.TransferFailedException;

/**
 * @author cmanda
 *
 */
interface S3Connector {

	/**
	 * Connects to a remote site.
	 *
	 * @return a string representing the connected AmazonS3Client.
	 * @throws TransferFailedException
	 *             on connection failure.
	 */
	public AmazonS3 connect() throws TransferFailedException;

	/**
	 * Disconnects from a remote site.
	 */
	public void disconnect();

	/**
	 * Downloads a file.
	 *
	 * @param outputStream
	 *            the output
	 * @param awsBucketName
	 *            the Bucket name
	 * @param downloadObjectName
	 *            the remote object for download
	 * @throws IOException
	 *             on error
	 */
	public void download(OutputStream outputStream, String awsBucketName, String downloadObjectName) throws IOException;

	/**
	 * Uploads a file.
	 *
	 * @param inputStream
	 *            the input
	 * @param awsBucketName
	 *            the remote parent
	 * @param uploadObjectName
	 *            the remote file name.
	 * @throws IOException
	 *             on error
	 */
	public void upload(InputStream inputStream, String awsBucketName, String uploadObjectName) throws IOException;

	/**
	 * List files from a remote directory, matching the pattern.
	 *
	 * @param remoteDir
	 *            the remote directory to list
	 * @param remotePattern
	 *            the pattern
	 * @return the list of files
	 * @throws IOException
	 *             on error
	 */
	public List<String> listFiles(String remoteDir, String remotePattern) throws IOException;

}

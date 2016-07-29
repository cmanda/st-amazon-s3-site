package com.axway.ssc.st.plugins.site.amazon.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.axway.ssc.st.plugins.site.amazon.s3.bean.S3Bean;
import com.axway.st.plugins.site.TransferFailedException;

/**
 * @author cmanda S3 Connector Implementation
 */
public class S3ConnectorImpl extends AbstractS3Connector {

	private AmazonS3 mS3;

	/**
	 * AWS Standard for upload mode i.e. Files larger than the threshold size
	 * should be uploaded via MULTIPART_MODE
	 */
	public static final long MULTIPART_MODE_THRESHOLD = 100 * 1024 * 1024;

	/**
	 * AWS standard CHUNK SIZE for MULTIPART UPLOADS
	 */
	public static final long MULTIPART_MODE_CHUNK_SIZE = 5 * 1024 * 1024;

	private static final Logger logger = Logger.getLogger(S3ConnectorImpl.class);

	public S3ConnectorImpl(S3Bean config) {
		super(config);
	}

	@Override
	public AmazonS3 connect() throws TransferFailedException {
		String accessKeyId = getConfiguration().getAccessKeyId();
		String secretAccessKey = getConfiguration().getSecretAccessKey();
		Region awsRegion = Region.getRegion(Regions.fromName(getConfiguration().getAwsRegion()));
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);

		try {
			mS3 = new AmazonS3Client(awsCreds);
			mS3.setRegion(awsRegion);
			logger.info("Account Owner : " + mS3.getS3AccountOwner().getDisplayName());
			return mS3;
		} catch (AmazonServiceException ase) {
			// String msg = ase.getLocalizedMessage();
			String genericMsg = "Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for reason below.";

			logger.error(genericMsg);
			String errorMessage = "[AmazonServiceException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ase.getMessage());
			throw new TransferFailedException(errorMessage, ase, true);
		} catch (AmazonClientException ace) {
			// String msg = ace.getLocalizedMessage();
			String genericMsg = "Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.";
			logger.error(genericMsg);
			String errorMessage = "[AmazonClientException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ace.getMessage());
			throw new TransferFailedException(errorMessage, ace, false);
		}
	}

	@Override
	public void disconnect() {
		mS3 = null;
	}

	@Override
	public void download(OutputStream outputStream, String awsBucketName, String downloadObjectName)
			throws IOException {
		S3Object dowloadObject = null;
		try {
			if (awsBucketName != null && !awsBucketName.isEmpty() && mS3.doesBucketExist(awsBucketName)) {
				dowloadObject = mS3.getObject(new GetObjectRequest(awsBucketName, downloadObjectName));
				IOUtils.copy(dowloadObject.getObjectContent(), outputStream);
			}
		} catch (AmazonServiceException ase) {
			// String msg = ase.getLocalizedMessage();
			String genericMsg = "Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for reason below.";

			logger.error(genericMsg);
			String errorMessage = "[AmazonServiceException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ase.getMessage());
			throw new TransferFailedException(errorMessage, ase, true);
		} catch (AmazonClientException ace) {
			// String msg = ace.getLocalizedMessage();
			String genericMsg = "Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.";
			logger.error(genericMsg);
			String errorMessage = "[AmazonClientException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ace.getMessage());
			throw new TransferFailedException(errorMessage, ace, false);
		}
	}

	@Override
	public void upload(InputStream inputStream, String awsBucketName, String uploadObjectName) throws IOException {

		String uploadMode = getConfiguration().getTransferMode();
		String[] userMetadata = getConfiguration().getUserMetadata().split("\\r?\\n");
		String cacheControl = getConfiguration().getCacheControl();
		Long streamLength = 0L;// Long.valueOf(IOUtils.toByteArray(inputStream).length);
		ObjectMetadata uploadMetadata = null;

		try {

			File tmpFile = File.createTempFile("stream2file", "s3");
			tmpFile.deleteOnExit();
			try (FileOutputStream fOut = new FileOutputStream(tmpFile)) {
				IOUtils.copy(inputStream, fOut);
			}
			logger.debug("tmpFile is: " + tmpFile.getAbsolutePath());

			logger.debug("Upload Mode: " + uploadMode);
			logger.debug("UserMetadata: " + Arrays.toString(userMetadata));
			logger.debug("Target Bucket: " + awsBucketName);
			logger.debug("Target Region: " + getConfiguration().getAwsRegion());

			uploadMetadata = new ObjectMetadata();
			//uploadMetadata.setContentLength(streamLength);
			//uploadMetadata.setCacheControl(cacheControl);

			for (String line : userMetadata) {
				String[] keyValues = line.split("=", 2);
				logger.debug("Metadata set for : " + keyValues[0] + " : " + keyValues[1]);
				//uploadMetadata.addUserMetadata(keyValues[0], keyValues[1]);
			}

			if (awsBucketName == null && !mS3.doesBucketExist(awsBucketName)) {
				String genericMsg = "Site Configuration Error: Wrong Bucket Name. Request aborted. "
						+ "Future update might include ability to create new buckets on the fly";
				logger.error(genericMsg);
			} else {
				if (uploadMode.equals("single")) {
					logger.info("S3 Upload to:  " + awsBucketName + " with key: " + uploadObjectName);
					logger.debug("Location of " + awsBucketName + "is: " + mS3.getBucketLocation(awsBucketName));
					if(inputStream == null) {
						logger.error("Might have to create temporary files");
					}
					//logger.debug("Upload Metadata: " + uploadMetadata.getContentLength() + ", " + uploadMetadata.getUserMetadata().get("username"));
					mS3.putObject(awsBucketName, uploadObjectName, inputStream, uploadMetadata);
					logger.info("S3 Upload to:  " + awsBucketName + " with key: " + uploadObjectName
							+ " finished successfully!");

				} else if (uploadMode.equals("multipart")) {
					logger.info("Multipart requests are not supported in this POC code");

				} else if (uploadMode.equals("auto")) {
					logger.info("Use only single method for this POC. Additional lines of code are not written");

				}
			}
		} catch (AmazonServiceException ase) {
			// String msg = ase.getLocalizedMessage();
			String genericMsg = "Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for reason below.";

			logger.error(genericMsg);
			String errorMessage = "[AmazonServiceException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ase.getMessage());
			throw new TransferFailedException(errorMessage, ase, true);
		} catch (AmazonClientException ace) {
			// String msg = ace.getLocalizedMessage();
			String genericMsg = "Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for reason below.";

			logger.error(genericMsg);
			String errorMessage = "[AmazonClientException]: {0}";
			errorMessage = MessageFormat.format(errorMessage, ace.getMessage());
			throw new TransferFailedException(errorMessage, ace, false);
		}

	}

	@Override
	public List<String> listFiles(String remoteDir, String remotePattern) throws IOException {
		List<String> bucketListing = new ArrayList<String>();
		ObjectListing objectListing = mS3
				.listObjects(new ListObjectsRequest().withBucketName(remoteDir).withPrefix(remotePattern));
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			logger.info(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
			bucketListing.add(objectSummary.getKey());
		}
		return bucketListing;
	}

	@Override
	protected boolean setProxySettings(String zone) {
		return false;
	}

	@SuppressWarnings("unused")
	private static void convertStreamToFile(InputStream inputStream, File outputFile) throws IOException {
		final ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
		final WritableByteChannel outputChannel = Channels.newChannel(new FileOutputStream(outputFile));
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

		while (inputChannel.read(buffer) != -1) {
			buffer.flip();
			outputChannel.write(buffer);
			buffer.compact();
		}

		buffer.flip();

		while (buffer.hasRemaining()) {
			outputChannel.write(buffer);
		}
	}

}

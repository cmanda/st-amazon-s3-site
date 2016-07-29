package com.axway.ssc.st.plugins.site.amazon.s3.bean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.axway.st.plugins.site.UIBean;

/**
 * @author cmanda
 */
public class S3Bean implements UIBean {

	/** Holds the Amazon-S3 Bucket Name. */
	@NotNull(message = "AWS S3 Bucket Name cannot be null")
	@Pattern(regexp = "\\S*", message = "AWS S3 Bucket Name could not contain whitespaces.")
	private String mAwsBucketName;

	/** Holds the Amazon-S3 Bucket region port. */
	@NotNull(message = "AWS S3 Region cannot be null")
	private String mAwsRegion;

	/** Holds the Amazon AWS Access Key. */
	@NotNull(message = "AWS Access Key ID cannot be null")
	@Size(min = 1, message = "AWS Access Key ID cannot be empty")
	@Pattern(regexp = "\\S*", message = "AWS Access Key ID cannot contain whitespaces.")
	private String mAccessKeyId;

	/** Holds the Amazon AWS Secret Access Key. */
	@NotNull(message = "AWS Secret Access Key cannot be null")
	@Size(min = 1, message = "AWS Secret Access Key cannot be empty")
	@Pattern(regexp = "\\S*", message = "AWS Secret Access Key ID cannot contain whitespaces.")
	private String mSecretAccessKey;

	private String mDownloadObjectKey;

	/** Holds the network zone name. */
	private String mNetworkZone;

	/** Holds the Transfer Mode for AWS S3: Single, Multipart & Auto **/
	private String mTransferMode;

	private String mContentDisposition;

	private String mCacheControl;

	private String mUserMetadata;

	public String getAwsBucketName() {
		return mAwsBucketName;
	}

	public void setAwsBucketName(String awsBucketName) {
		this.mAwsBucketName = awsBucketName;
	}

	public String getAwsRegion() {
		return mAwsRegion;
	}

	public void setAwsRegion(String awsRegion) {
		this.mAwsRegion = awsRegion;
	}

	public String getAccessKeyId() {
		return mAccessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		this.mAccessKeyId = accessKeyId;
	}

	public String getSecretAccessKey() {
		return mSecretAccessKey;
	}

	public void setSecretAccessKey(String secretAccessKey) {
		this.mSecretAccessKey = secretAccessKey;
	}

	public String getDownloadObjectKey() {
		return mDownloadObjectKey;
	}

	public void setDownloadObjectKey(String downloadObjectKey) {
		this.mDownloadObjectKey = downloadObjectKey;
	}

	public String getNetworkZone() {
		return mNetworkZone;
	}

	public void setNetworkZone(String networkZone) {
		this.mNetworkZone = networkZone;
	}

	public String getTransferMode() {
		return mTransferMode;
	}

	public void setTransferMode(String transferMode) {
		this.mTransferMode = transferMode;
	}

	public String getContentDisposition() {
		return mContentDisposition;
	}

	public void setContentDisposition(String contentDisposition) {
		this.mContentDisposition = contentDisposition;
	}

	public String getCacheControl() {
		return mCacheControl;
	}

	public void setCacheControl(String cacheControl) {
		this.mCacheControl = cacheControl;
	}

	public String getUserMetadata() {
		return mUserMetadata;
	}

	public void setUserMetadata(String userMetadata) {
		this.mUserMetadata = userMetadata;
	}




}

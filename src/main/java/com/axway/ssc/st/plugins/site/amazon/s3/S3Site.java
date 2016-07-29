package com.axway.ssc.st.plugins.site.amazon.s3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.axway.ssc.st.plugins.site.amazon.s3.bean.S3Bean;
import com.axway.st.plugins.site.CustomSite;
import com.axway.st.plugins.site.DestinationFile;
import com.axway.st.plugins.site.FileItem;
import com.axway.st.plugins.site.RemotePartner;
import com.axway.st.plugins.site.SourceFile;
import com.axway.st.plugins.site.services.CertificateService;
import com.axway.st.plugins.site.services.ProxyService;

/**
 * @author cmanda Custom Site Extension.
 */
public class S3Site extends CustomSite {

	/**
	 * UI Bean implementation
	 */
	private S3Bean mS3Bean = new S3Bean();

	/**
	 * S3 Client implementation. Allows connect/disconnect/upload/download/list
	 * of files
	 */
	private AbstractS3Connector mS3Connection;

	/**
	 * Provides Proxy Server Configuration
	 */
	@Inject
	private ProxyService mProxyService;

	/**
	 * Used to create SSLContext, configured with private key, to authenticate
	 * with.
	 */
	@SuppressWarnings("unused")
	@Inject
	private CertificateService mCertificateService;

	public S3Site() {
		setUIBean(mS3Bean);
	}

	/**
	 * Connection for S3 AWS Bucket
	 */
	public AmazonS3 connect() throws IOException {
		mS3Connection = new S3ConnectorBuilder().build(mS3Bean);
		mS3Connection.setProxyService(mProxyService);
		return mS3Connection.connect();
	}

	@Override
	public void finalizeExecution() throws IOException {
		mS3Connection = null;

	}

	@Override
	public void getFile(DestinationFile destFile) throws IOException {
		AmazonS3 s3Client = null;
		if (mS3Connection == null) {
			s3Client = connect();
		}
		mS3Connection.download(
				destFile.getOutputStream(
						new RemotePartner(s3Client.getS3AccountOwner().getDisplayName(), mS3Bean.getAwsBucketName())),
				mS3Bean.getAwsBucketName(), mS3Bean.getDownloadObjectKey());

	}

	@Override
	public List<FileItem> list() throws IOException {
		if (mS3Connection == null) {
			connect();
		}

		List<String> names = mS3Connection.listFiles(mS3Bean.getAwsBucketName(), mS3Bean.getDownloadObjectKey());
		List<FileItem> result = new ArrayList<FileItem>();
		if (names != null && names.size() > 0) {
			for (String name : names) {
				result.add(new FileItem(name));
			}
		}
		return result;
	}

	@Override
	public void putFile(SourceFile srcFile) throws IOException {
		AmazonS3 s3Client = null;
		if (mS3Connection == null) {
			s3Client = connect();
		}
		mS3Connection.upload(
				srcFile.getInputStream(
						new RemotePartner(s3Client.getS3AccountOwner().getDisplayName(), mS3Bean.getAwsBucketName())),
				mS3Bean.getAwsBucketName(), srcFile.getName());

	}

}

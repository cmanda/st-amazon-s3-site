package com.axway.ssc.st.plugins.site.amazon.s3;

import com.axway.ssc.st.plugins.site.amazon.s3.bean.S3Bean;

/**
 * @author cmanda
 */
public class S3ConnectorBuilder {

	/**
	 * Creates and configures S3Connector from the given bean.
	 *
	 * @param config
	 *            the bean, holding the configuration.
	 * @return a S3Connector instance
	 */
	public AbstractS3Connector build(S3Bean config) {
		AbstractS3Connector s3Connector = null;

		s3Connector = new S3ConnectorImpl(config);

		return s3Connector;
	}

}

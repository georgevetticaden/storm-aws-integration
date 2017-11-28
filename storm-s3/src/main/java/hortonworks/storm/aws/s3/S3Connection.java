/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hortonworks.storm.aws.s3;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;


public class S3Connection {
    private static final Logger LOG = LoggerFactory.getLogger(S3Connection.class);
    private final S3ConnectionInfo s3ConnectionInfo;
    private AmazonS3 s3Client;

    public S3Connection (S3ConnectionInfo s3ConnectionInfo) {
        this.s3ConnectionInfo = s3ConnectionInfo;
    }
    
    public void initialize() {
        
        s3Client = AmazonS3ClientBuilder.standard().withRegion(s3ConnectionInfo.getRegion())
        										   .withCredentials(s3ConnectionInfo.getCredentialsProvider())
        										   .withClientConfiguration(s3ConnectionInfo.getClientConfiguration())
        										   .build();
    }
    
	public List<Bucket> listBuckets() {
		return s3Client.listBuckets();
	}    
	
	public void putStringObject(String bucketName, String key, String value) {
		s3Client.putObject(bucketName, key, value);		
	}
	
	public S3Object getObject(String bucketName, String key) {
		return s3Client.getObject(bucketName, key);
	}	
	
	public ObjectListing listObjects(String bucketName) {
		return s3Client.listObjects(bucketName);
	}

    void shutdown () {
        s3Client.shutdown();
    }



}

package hortonworks.storm.aws.s3;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.hortonworks.streamline.streams.common.StreamlineEventImpl;

public class S3ConnectionTest {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(S3ConnectionTest.class);
	private static final Regions REGION = Regions.US_WEST_2;
	private static final String BUCKET_NAME = "alerts-speeding-drivers";
	private static final String ACCESS_KEY = "XXX";
	private static final String SECRET_KEY = "XXX";	
	
	@Test
	public void testConnectionWithDefaultAWSCredentialProviderChain() {
		S3Connection s3Connection = createS3Connection();
		s3Connection.initialize();
		List<Bucket> buckets =  s3Connection.listBuckets();
		assertThat(buckets.isEmpty(), is(false));
		for(Bucket bucket: buckets) {
			LOG.info(bucket.getName());
		}		
	}


	
	@Test
	public void testConnectionWithProvidedCredentialProviderChain() {
		AWSStaticCredentialsProvider staticProvider = new AWSStaticCredentialsProvider(createCredentials());
		S3ConnectionInfo s3ConnectionInfo = new S3ConnectionInfo(staticProvider, new ClientConfiguration(),  REGION.name());
		S3Connection s3Connection = new S3Connection(s3ConnectionInfo);
		s3Connection.initialize();
		s3Connection.listBuckets();
		List<Bucket> buckets =  s3Connection.listBuckets();
		assertThat(buckets.isEmpty(), is(false));
		for(Bucket bucket: buckets) {
			LOG.info(bucket.getName());		
		}
	}
	
	
	@Test
	public void listObjectsinAlertsSpeedingDriversBucket() throws Exception {
		S3Connection s3Connection = createS3Connection();	
		
		ObjectListing alertObjectListing = s3Connection.listObjects(BUCKET_NAME);
		List<S3ObjectSummary> alertSummaries = alertObjectListing.getObjectSummaries();
		LOG.info("Number of alerts in Bucket["+BUCKET_NAME + "] is: "  + alertSummaries.size());
		for(S3ObjectSummary alert: alertSummaries) {
			String alertKey = alert.getKey();
			S3Object alertObject = s3Connection.getObject(BUCKET_NAME, alertKey);
			S3ObjectInputStream alertStream = alertObject.getObjectContent();
			String alertValue = IOUtils.toString(alertStream);
			LOG.info("Alert Key [" + alertKey + "], Alert Value[" + alertValue);
		}
	}	
	
	@Test
	public void putSpeedingAlertInS3() throws Exception{
		StreamlineEventImpl speedingAlert = createStreamLineSpeedingAlertEvent();
		LOG.info(speedingAlert.toString());
		
		String speedingAlertCSV = createAlertASCSV(speedingAlert);
		LOG.info(speedingAlertCSV);
		
		S3Connection s3Connection = createS3Connection();		

		s3Connection.putStringObject(BUCKET_NAME, speedingAlert.getId(), speedingAlertCSV);
		
	}
	
	public static String createAlertASCSV(Map<String, Object> alertMap) throws Exception {
		CsvSchema schema = null;
		CsvSchema.Builder schemaBuilder = CsvSchema.builder();

		for (String col : alertMap.keySet()) {
			schemaBuilder.addColumn(col);
		}
		schema = schemaBuilder.build().withLineSeparator("\r");
		
		Writer writer = new StringWriter();
		CsvMapper mapper = new CsvMapper();
		mapper.writer(schema).writeValues(writer).write(alertMap);
		writer.flush();
		return writer.toString();
	}	


	private StreamlineEventImpl createStreamLineSpeedingAlertEvent() {
		StreamlineEventImpl.Builder builder = StreamlineEventImpl.builder();
		Map<String, Object> alertDetails = new HashMap<String, Object>();
		alertDetails.put("driverId", Integer.valueOf(100));
		alertDetails.put("driverName", "George Vetticaden");
		alertDetails.put("route", "Saint Louis to Chicago");
		alertDetails.put("speed_AVG_Round", Long.valueOf(92));
		
		builder.putAll(alertDetails);
		StreamlineEventImpl streamlineEvent = builder.build();
		return streamlineEvent;
	}
			

	private AWSCredentials createCredentials() {
		BasicAWSCredentials basicCredentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		return basicCredentials;
	}	
	
	private S3Connection createS3Connection() {
		S3ConnectionInfo s3ConnectionInfo = new S3ConnectionInfo(new DefaultAWSCredentialsProviderChain(), new ClientConfiguration(),  REGION.name());
		S3Connection s3Connection = new S3Connection(s3ConnectionInfo);
		s3Connection.initialize();
		return s3Connection;
	}	

}

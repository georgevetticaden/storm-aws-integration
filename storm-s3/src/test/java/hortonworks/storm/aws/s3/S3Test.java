package hortonworks.storm.aws.s3;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.hortonworks.streamline.streams.common.StreamlineEventImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Test {
	
	private static final Logger LOG = LoggerFactory.getLogger(S3Test.class);
	private static final Regions REGION = Regions.US_WEST_2;
	private static final String BUCKET_NAME = "alerts-speeding-drivers";
	
	
	@Test
	public void connect() {
		AmazonS3 s3Client = createS3Client();
	}

	@Test
	public void putSpeedingAlertInS3() throws Exception{
		StreamlineEventImpl speedingAlert = createStreamLineSpeedingAlertEvent();
		LOG.info(speedingAlert.toString());
		
		String speedingAlertCSV = createAlertASCSV(speedingAlert);
		LOG.info(speedingAlertCSV);
		
		AmazonS3 s3Client = createS3Client();
		s3Client.putObject(BUCKET_NAME, speedingAlert.getId(), speedingAlertCSV);
		
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
	
	@Test
	public void listObjectsinAlertsSpeedingDriversBucket() throws Exception {
		AmazonS3 s3Client = createS3Client();
		ObjectListing alertObjectListing = s3Client.listObjects(BUCKET_NAME);
		List<S3ObjectSummary> alertSummaries = alertObjectListing.getObjectSummaries();
		LOG.info("Number of alerts in Bucket["+BUCKET_NAME + "] is: "  + alertSummaries.size());
		for(S3ObjectSummary alert: alertSummaries) {
			String alertKey = alert.getKey();
			S3Object alertObject = s3Client.getObject(BUCKET_NAME, alertKey);
			S3ObjectInputStream alertStream = alertObject.getObjectContent();
			String alertValue = IOUtils.toString(alertStream);
			LOG.info("Alert Key [" + alertKey + "], Alert Value[" + alertValue);
		}
	}
	
	@Test
	public void listBuckets() {
		AmazonS3 s3Client = createS3Client();
		List<Bucket> buckets =  s3Client.listBuckets();
		assertThat(buckets.isEmpty(), is(false));
		for(Bucket bucket: buckets) {
			LOG.info(bucket.getName());
		}
		
	}
	
	private AmazonS3 createS3Client() {
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(REGION).build();
		return s3;
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
	

}

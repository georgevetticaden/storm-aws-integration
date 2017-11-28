package hortonworks.storm.aws.s3.bolt;

import hortonworks.storm.aws.s3.S3Connection;
import hortonworks.storm.aws.s3.S3ConnectionInfo;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.hortonworks.streamline.streams.StreamlineEvent;
import com.hortonworks.streamline.streams.runtime.storm.bolt.AbstractProcessorBolt;

public class S3Bolt extends AbstractProcessorBolt{

	
	private static final long serialVersionUID = 2758179283721487530L;
	private static final Logger LOG = LoggerFactory.getLogger(S3Bolt.class);
	
	private S3ConnectionInfo s3ConnectionInfo;
	private String bucket;
	private transient S3Connection s3Connection;

	 public S3Bolt(S3ConnectionInfo s3ConnectionInfo, String bucket) {
		this.s3ConnectionInfo = s3ConnectionInfo;
		this.bucket = bucket;
	}
	
	@Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		super.prepare(stormConf, context, collector);
		
		LOG.info("About to initialize S3 Connection");
		this.s3Connection = new S3Connection(s3ConnectionInfo);
		this.s3Connection.initialize();
		LOG.info("S3 Connection initiliazed");
    }	
	 
	@Override
	protected void process(Tuple tuple, StreamlineEvent streamLineEvent) throws Exception {
		String event = createStreamLineEventAsCSV(streamLineEvent);
		
		LOG.info("About to persist event[" + event + "] to S3 bucket["+bucket +"]");
		s3Connection.putStringObject(bucket, streamLineEvent.getId(), event);
		LOG.info("Successfully persisted event[" + event + "] to S3 bucket["+bucket +"]");
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		//nothing to do since it is a sink
	}
	
	/**
	 * Takes a StreamLine event and converts into a CSV delimited text with seperator ','
	 * @param alertMap
	 * @return
	 * @throws Exception
	 */
	private static String createStreamLineEventAsCSV(Map<String, Object> alertMap) throws Exception {
		
		/** GJVETT: Need to check if i can create the mapper onnce and reuse. For now creating on each invocation */
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

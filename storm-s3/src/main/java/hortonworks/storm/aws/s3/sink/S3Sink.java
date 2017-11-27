package hortonworks.storm.aws.s3.sink;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import com.hortonworks.streamline.streams.StreamlineEvent;
import com.hortonworks.streamline.streams.runtime.storm.bolt.AbstractProcessorBolt;

public class S3Sink extends AbstractProcessorBolt{

	@Override
	protected void process(Tuple arg0, StreamlineEvent arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

}

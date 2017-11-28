package hortonworks.storm.aws.s3.sink;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import com.hortonworks.streamline.streams.StreamlineEvent;
import com.hortonworks.streamline.streams.runtime.storm.bolt.AbstractProcessorBolt;

public class S3Sink extends AbstractProcessorBolt{

	
	 @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		super.prepare(stormConf, context, collector);
        
    }	
	@Override
	protected void process(Tuple tuple, StreamlineEvent streamLineEvent) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		//nothing to do since it is a sink
	}

}

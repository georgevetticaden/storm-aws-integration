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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class S3ConnectionInfo implements Serializable {


	private static final long serialVersionUID = -4512603876452319460L;
	private final byte[] serializedS3CredsProvider;
    private final byte[] serializedS3ClientConfig;
    private final Regions region;

    private transient AWSCredentialsProvider credentialsProvider;
    private transient ClientConfiguration clientConfiguration;

    /**
     *
     * @param credentialsProvider implementation to provide credentials to connect to s3
     * @param clientConfiguration client configuration to pass to s3 client
     * @param region region to connect to
     */
    public S3ConnectionInfo (AWSCredentialsProvider credentialsProvider, ClientConfiguration clientConfiguration, String regionString) {
        if (regionString == null) {
            throw new IllegalArgumentException("region cannot be null");
        }
        serializedS3CredsProvider = getKryoSerializedBytes(credentialsProvider);
        serializedS3ClientConfig = getKryoSerializedBytes(clientConfiguration);
        this.region = Regions.valueOf(regionString);
    }


    public AWSCredentialsProvider getCredentialsProvider() {
        if (credentialsProvider == null) {
            credentialsProvider = (AWSCredentialsProvider) this.getKryoDeserializedObject(serializedS3CredsProvider);
        }
        return credentialsProvider;
    }

    public ClientConfiguration getClientConfiguration() {
        if (clientConfiguration == null) {
            clientConfiguration = (ClientConfiguration) this.getKryoDeserializedObject(serializedS3ClientConfig);
        }
        return clientConfiguration;
    }

    public Regions getRegion() {
        return region;
    }

    private byte[] getKryoSerializedBytes (final Object obj) {
        final Kryo kryo = new Kryo();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Output output = new Output(os);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.writeClassAndObject(output, obj);
        output.flush();
        return os.toByteArray();
    }

    private Object getKryoDeserializedObject (final byte[] ser) {
        final Kryo kryo = new Kryo();
        final Input input = new Input(new ByteArrayInputStream(ser));
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "S3ConnectionInfo{" +
                "serializedS3CredsProvider=" + Arrays.toString(serializedS3CredsProvider) +
                ", serializedS3ClientConfig=" + Arrays.toString(serializedS3ClientConfig) +
                ", region=" + region +
                '}';
    }
}

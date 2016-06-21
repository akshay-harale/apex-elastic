package com.example.myapexapp.com.example.myapexapp.kafka;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.contrib.elasticsearch.ElasticSearchConnectable;
import com.datatorrent.contrib.elasticsearch.ElasticSearchMapOutputOperator;
import org.apache.apex.malhar.kafka.KafkaSinglePortInputOperator;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 * Created by Akshay Harale<akshay.harale@synerzip.com/> on 16/6/16.
 */
public class KafkaApplication implements StreamingApplication{
    @Override
    public void populateDAG(DAG dag, Configuration conf) {

        KafkaSinglePortInputOperator input = (KafkaSinglePortInputOperator)dag.addOperator("KafkaReader", new KafkaSinglePortInputOperator());

        MessageProcessor processor = dag.addOperator("processor", new MessageProcessor());
        dag.addStream("message",input.outputPort,processor.input);


        //set up the elastic search output operator

        ElasticSearchMapOutputOperator output= dag.addOperator("fireLogOperator", new ElasticSearchMapOutputOperator ());
        try {
            output.setStore(createStore());
        } catch (IOException e) {
            e.printStackTrace();
        }
        output.setIdField("uuid");
        output.setIndexName("firelog");
        output.setType("log");

        dag.addStream("toElastic",processor.output,output.input);
    }
    ElasticSearchConnectable createStore() throws IOException {
        ElasticSearchConnectable store = new ElasticSearchConnectable();
        store.setHostName("127.0.0.1");
        store.setPort(9300);
        store.setClusterName("elasticsearch");
        store.connect();
        return store;
    }
}

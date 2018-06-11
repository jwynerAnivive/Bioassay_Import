package com.anivive.kafka

import com.anivive.util.KafkaConsumerHelper
import com.anivive.util.KafkaProducerHelper
import com.anivive.util.StandardProperty
import com.anivive.util.StringProperty
import com.sun.istack.internal.logging.Logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*

class Queue {
    //We need properties for our connection to archetype-resources.src.main.kotlin.com.anivive.Kafka
    //We like them to be lazy in case we want to test our kafka class without all test values in place
    //Then we can mock our kafka connection and perhaps avoid nasty null pointers on KAFKA_SERVERS + KAFKA_POLL_SIZE
    //KAFKA_SERVERS should look like so: kafka-0.broker.kafka.svc.cluster.local:9092,kafka-1.broker.kafka.svc.cluster.local:9092,
    // kafka-2.broker.kafka.svc.cluster.local:9092
    //KAFKA_POLL_SIZE can be 1 or 10 or 100, probably just test out different values.
    private val props by lazy {
        Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, StandardProperty.KAFKA_SERVERS.value)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, StandardProperty.KAFKA_POLL_SIZE.value!!.toInt())
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        }
    }

    private val logger = Logger.getLogger(Kafka::class.java)
    private val linkTopic = StringProperty("LinkTopic").value!!
    private val groupId = StringProperty("GroupId").value!!

    fun getLinks() {
        KafkaProducerHelper(props, linkTopic).use { producer ->
            /*
                Download.getUrls().forEach {
                    producer.send(it)
                }
             */
        }
    }

    fun pullLinks() {
        KafkaConsumerHelper(props, groupId, linkTopic).use { consumer ->
            consumer.consume { item ->
                /*
                Download.downloadFTP(item)
                */
                logger.info("$item downloaded")
            }
        }
    }
}
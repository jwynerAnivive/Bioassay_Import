package com.anivive.kafka

import com.anivive.Neo.Insert
import com.anivive.process.Download
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

object Queue {

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

    private val logger = Logger.getLogger(Queue::class.java)
    private val fileTopic = StringProperty("FileTopic").value!!
    private val dateTopic = StringProperty("DateTopic").value!!
    private val groupId = StringProperty("GroupId").value!!

    /**
     * Stores the names of files to be updated in one queue, and the files with their dates to be updated in another
     */

    fun storeFiles() {
        KafkaProducerHelper(props, dateTopic).use { dateProducer ->
            KafkaProducerHelper(props, fileTopic).use { fileProducer ->
                Download.getUrls().forEach {
                    fileProducer.send(it.key)
                    dateProducer.send("${it.key}*${it.value}") // filename is used as node property, date is value
                }
                logger.info("files and dates inserted to queue")
            }
        }
    }

    /**
     * pulls the filenames out of the queue, then downloads, unzips, and stores them
     */

    fun pullFiles() {
        val consumer = KafkaConsumerHelper(props, groupId, fileTopic) { files ->
            logger.info("collection of files pulled")
            Download.downloadAndUnzipFiles("Description", files.toList())
            Download.downloadAndUnzipFiles("Data", files.toList())
        }
        consumer.start()
        consumer.close()
    }

    /**
     * After processing is done, dates are pulled and the UpdatedAt node is updated with current dates
     */

    fun pullDates() {
        val consumer = KafkaConsumerHelper(props, groupId, dateTopic) { dates ->
            logger.info("collection of dates pulled")
            Insert.updateDate(dates.toList())
        }
        consumer.start()
        consumer.close()
    }
}
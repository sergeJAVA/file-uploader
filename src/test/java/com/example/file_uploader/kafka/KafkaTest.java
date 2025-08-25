package com.example.file_uploader.kafka;

import com.example.file_uploader.constant.FileStatus;
import com.example.file_uploader.dto.FileDto;
import com.example.file_uploader.testcontainer.Testcontainer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("kafka-test")
public class KafkaTest extends Testcontainer {

    private KafkaConsumer<String, String> uploadConsumer;
    private KafkaConsumer<String, String> statusConsumer;

    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        Properties propsUploadConsumer = new Properties();
        propsUploadConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        propsUploadConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsUploadConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsUploadConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "processor-id");

        Properties propsStatusConsumer = new Properties();
        propsStatusConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        propsStatusConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsStatusConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsStatusConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "status-processor-id");

        uploadConsumer = new KafkaConsumer<>(propsUploadConsumer);
        uploadConsumer.subscribe(List.of("upload"));

        statusConsumer = new KafkaConsumer<>(propsStatusConsumer);
        statusConsumer.subscribe(List.of("status"));
    }

    @Test
    void testUploadTopic() throws JsonProcessingException {
        FileDto fileDto = FileDto.builder()
                .fileName("testFile")
                .fileBytes("content".getBytes())
                .fileStatus(FileStatus.FIRST_VALIDATION_SUCCESS)
                .checksum("TestChecksum")
                .build();

        String message = objectMapper.writeValueAsString(fileDto);

        kafkaTemplate.send("upload", message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records =
                    uploadConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                assertThat(record.value()).isEqualTo(message);
            }
            uploadConsumer.close();
        });
    }

    @Test
    void testStatusTopic() throws JsonProcessingException {
        FileDto fileDto = FileDto.builder()
                .fileName("testFile")
                .fileBytes("content".getBytes())
                .fileStatus(FileStatus.FIRST_VALIDATION_SUCCESS)
                .checksum("TestChecksum")
                .build();

        String message = objectMapper.writeValueAsString(fileDto);

        kafkaTemplate.send("status", message);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> records =
                    statusConsumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                assertThat(record.value()).isEqualTo(message);
            }
            statusConsumer.close();
        });
    }

}

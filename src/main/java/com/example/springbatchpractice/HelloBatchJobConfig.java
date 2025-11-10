package com.example.springbatchpractice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job helloJob() {
        return new JobBuilder("helloJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(helloStep())
                .build();
    }

    @Bean
    public Step helloStep() {
        return new StepBuilder("helloStep", jobRepository)
                .<Integer, String>chunk(CHUNK_SIZE, transactionManager)
                .reader(helloReader())
                .processor(helloProcessor())
                .writer(helloWriter())
                .build();
    }

    @Bean
    public ItemReader<Integer> helloReader() {
        List<Integer> numbers = IntStream.rangeClosed(1, 100)
                .boxed()
                .toList();
        return new ListItemReader<>(numbers);
    }

    @Bean
    public ItemProcessor<Integer, String> helloProcessor() {
        return item -> {
            log.info("Processing item: {}", item);
            return "Processed Item: " + (item * 2);
        };
    }

    @Bean
    public ItemWriter<String> helloWriter() {
        return chunk -> {
            log.warn("--- Writing Chunk (Size: {}) ---", chunk.getItems().size());
            for (String item : chunk.getItems()) {
                log.info(item);
            }
            log.warn("--- Chunk Written ---");
        };
    }
}
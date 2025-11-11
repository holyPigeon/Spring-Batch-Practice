package com.example.springbatchpractice.batch;

import com.example.springbatchpractice.domain.User;
import com.example.springbatchpractice.domain.UserStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DormantUserJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job dormantUserJob() {
        return new JobBuilder("dormantUserJob", jobRepository)
                .start(dormantUserStep())
                .build();
    }

    @Bean
    public Step dormantUserStep() {
        return new StepBuilder("dormantUserStep", jobRepository)
                .<User, User>chunk(CHUNK_SIZE, transactionManager)
                .reader(dormantUserReader())
                .processor(dormantUserProcessor())
                .writer(dormantUserWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> dormantUserReader() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        log.info("휴면 회원 전환 배치: 1년 전 날짜 = {}", oneYearAgo);

        return new JpaPagingItemReaderBuilder<User>()
                .name("dormantUserReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString(
                        "SELECT u FROM User u " +
                                "WHERE u.status = :status " +
                                "AND u.lastLoginDate < :oneYearAgo"
                )
                .parameterValues(Map.of(
                        "status", UserStatus.ACTIVE,
                        "oneYearAgo", oneYearAgo
                ))
                .build();
    }

    @Bean
    public ItemProcessor<User, User> dormantUserProcessor() {
        return user -> {
            log.info("Processing user id: {}, status: {}", user.getId(), user.getStatus());
            user.changeStatusToDormant();

            return user;
        };
    }

    @Bean
    public JpaItemWriter<User> dormantUserWriter() {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
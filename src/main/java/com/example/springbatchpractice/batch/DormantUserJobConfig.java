package com.example.springbatchpractice.batch;

import com.example.springbatchpractice.domain.User;
import com.example.springbatchpractice.domain.UserStatus;
import jakarta.persistence.EntityManagerFactory;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
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
    private static final int LOGGING_CHUNK_INTERVAL = 100;

    @Bean
    public Job dormantUserJob() {
        return new JobBuilder("dormantUserJob", jobRepository)
                .start(dormantUserStep())
                .listener(new DormantJobExecutionListener())
                .build();
    }

    @Bean
    public Step dormantUserStep() {
        return new StepBuilder("dormantUserStep", jobRepository)
                .<User, User>chunk(CHUNK_SIZE, transactionManager)
                .reader(dormantUserReader())
                .processor(dormantUserProcessor())
                .writer(dormantUserWriter())
                .listener(new DormantUserWriteListener())
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
            log.debug("User ID: {}, status: {}로 변경 처리", user.getId(), user.getStatus());
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

    public static class DormantUserWriteListener implements ItemWriteListener<User> {
        private final AtomicLong chunkCount = new AtomicLong(0);

        @Override
        public void afterWrite(Chunk<? extends User> items) {
            long currentChunk = chunkCount.incrementAndGet();

            if (currentChunk % LOGGING_CHUNK_INTERVAL == 0) {
                log.info("### [DormantJob] {} 번째 청크 처리 완료 (누적 {} 건)",
                        currentChunk, currentChunk * CHUNK_SIZE);
            }
        }
    }

    public static class DormantJobExecutionListener implements JobExecutionListener {

        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("###################################################");
            log.info("### [DormantJob] 휴면 회원 전환 배치를 시작합니다. ###");
            log.info("###################################################");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            LocalDateTime startTime = jobExecution.getStartTime();
            LocalDateTime endTime = jobExecution.getEndTime();

            long durationMillis = 0L;

            if (startTime != null && endTime != null) {
                durationMillis = java.time.Duration.between(startTime, endTime).toMillis();
            }

            long totalReadCount = jobExecution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getReadCount)
                    .sum();

            log.info("#####################################################");
            log.info("### [DormantJob] 휴면 회원 전환 배치를 종료합니다. ###");
            log.info("### 총 소요 시간: {} 초", durationMillis / 1000.0);
            log.info("### 총 처리 건수: {} 건", totalReadCount);
            log.info("### 작업 상태: {} ", jobExecution.getStatus());
            log.info("#####################################################");
        }
    }
}
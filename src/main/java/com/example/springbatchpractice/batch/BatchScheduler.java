package com.example.springbatchpractice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dormantUserJob;

    @Scheduled(fixedDelay = 86400000) // 24 hours in milliseconds
    public void runDormantUserJob() {
        try {
            log.info("휴면 회원 전환 배치 스케줄러 시작...");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            jobLauncher.run(dormantUserJob, jobParameters);

        } catch (Exception e) {
            log.error("휴면 회원 배치 실행 중 오류 발생", e);
        }
    }
}
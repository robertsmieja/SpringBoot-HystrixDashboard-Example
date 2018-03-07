package com.robertsmieja.example.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.stereotype.Service;

@Service
@EnableHystrix
public class StringService {
    public static final int MAX_STRING_LENGTH = 100;
    public static final long MIN_DELAY_MILLIS = 100;
    public static final long MAX_DELAY_MILLIS = 1000;
    public static final long TIMEOUT_MILLIS = 500;

    public static final String lineSeperator = System.getProperty("line.separator");
    public static final String FALLBACK_STRING = "FALLBACK!";

    @HystrixCommand(
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "" + TIMEOUT_MILLIS)
            },
            defaultFallback = "fallback"
//            threadPoolKey = "StringService.getRandomStringWithDelay"
    )
    public String getRandomStringWithDelay() throws InterruptedException {
        long millisToSleep = RandomUtils.nextLong(MIN_DELAY_MILLIS, MAX_DELAY_MILLIS);
        Thread.sleep(millisToSleep);
        return getRandomString();
    }

    private String getRandomString(){
        int numChars = RandomUtils.nextInt(0, MAX_STRING_LENGTH);
        return RandomStringUtils.randomAlphanumeric(numChars) + lineSeperator;
    }

    private String fallback(){
        return FALLBACK_STRING + lineSeperator;
    }

}

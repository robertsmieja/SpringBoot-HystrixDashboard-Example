package com.robertsmieja.example.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import static com.netflix.hystrix.HystrixCommandProperties.Setter;
import static com.robertsmieja.example.hystrix.StringService.TIMEOUT_MILLIS;

public class RandomStringCommand extends HystrixCommand<String> {
    private final StringService stringService;

    public RandomStringCommand(StringService stringService) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey("getRandomStringWithDelay"))
                .andCommandPropertiesDefaults(
                        Setter()
                                .withExecutionTimeoutEnabled(true)
                                .withExecutionTimeoutInMilliseconds((int) TIMEOUT_MILLIS)
                )
        );

        this.stringService = stringService;
    }

    @Override
    protected String run() throws Exception {
        return stringService.getRandomStringWithDelay();
    }

    @Override
    protected String getFallback() {
        return stringService.fallback();
    }
}

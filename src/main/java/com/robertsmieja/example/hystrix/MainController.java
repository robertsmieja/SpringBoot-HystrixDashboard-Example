package com.robertsmieja.example.hystrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import rx.Observable;

import java.util.concurrent.TimeUnit;

@RestController
public class MainController {
    @Autowired
    private StringService stringService;

    @GetMapping("/")
    public String index() {
        return "Hello world";
    }

    @GetMapping(path = "/randomString", produces = "text/plain")
    public String getRandomString() throws InterruptedException {
        return stringService.getRandomStringWithDelay();
    }

    @GetMapping(path = "/randomStringStream/{delay}", produces = "text/event-stream")
    public Observable<String> getRandomStringStream(@PathVariable("delay")long delayMillis){
        return Observable.fromCallable(stringService::getRandomStringWithDelay).repeat().delay(delayMillis, TimeUnit.MILLISECONDS);
    }
}

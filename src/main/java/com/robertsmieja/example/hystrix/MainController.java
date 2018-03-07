package com.robertsmieja.example.hystrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @Autowired
    private StringService stringService;

    @GetMapping(path = "/")
    public String index() {
        return "Hello world";
    }

    @GetMapping(path = "/randomString")
    public String getRandomString() throws InterruptedException {
        return stringService.getRandomStringWithDelay();
    }
}

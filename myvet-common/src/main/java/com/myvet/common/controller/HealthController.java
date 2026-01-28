// myvet-common/src/main/java/com/myvet/common/controller/HealthController.java
package com.myvet.common.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController extends BaseController<Void>{

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/version")
    public String version() {
        return "1.0.0";
    }
}
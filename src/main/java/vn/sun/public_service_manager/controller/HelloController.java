package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden // Ẩn endpoint này khỏi Swagger UI vì chỉ là endpoint test
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Public Service Manager!";
    }
}

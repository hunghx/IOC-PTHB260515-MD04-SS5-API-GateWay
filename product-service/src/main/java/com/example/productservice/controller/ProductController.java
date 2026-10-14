package com.example.productservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    @GetMapping("/{id}")
    public String getProductById(@PathVariable String id, @RequestHeader("X-UserId") String userId){
        return "Lấy chi tiết thông tin sản phẩm có id là "+id+" X-UserId = "+userId;
    }
}

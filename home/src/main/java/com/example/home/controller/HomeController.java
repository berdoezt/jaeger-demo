package com.example.home.controller;

import com.example.home.model.Context;
import com.example.home.model.HomeModel;
import com.example.home.service.HomeService;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;

@RestController
public class HomeController {

  private final HomeService homeService;
  private final Tracer tracer;

  @Autowired
  public HomeController(HomeService homeService, Tracer tracer) {
    this.homeService = homeService;
    this.tracer = tracer;
  }

  @GetMapping("/homes")
  public ResponseEntity<HomeModel> getData(
      @RequestParam("id") Integer id
  ) {
    Span span = tracer.buildSpan("HomeController.getData").start();
    HttpStatus httpStatus = HttpStatus.OK;

    HomeModel homeModel = homeService.getData(id, Context.builder()
        .span(span)
        .build());
    span.setTag("http.status_code", httpStatus.value());
    span.setTag("response", String.valueOf(homeModel));
    span.setTag("request", id);
    span.finish();

    return new ResponseEntity<>(homeModel, httpStatus);
  }
}

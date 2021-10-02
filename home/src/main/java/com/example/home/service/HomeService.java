package com.example.home.service;

import com.example.home.model.Context;
import com.example.home.model.HomeModel;
import com.example.home.outbound.HomeOutbound;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

  private final Tracer tracer;
  private final HomeOutbound homeOutbound;

  @Autowired
  public HomeService(Tracer tracer, HomeOutbound homeOutbound) {
    this.tracer = tracer;
    this.homeOutbound = homeOutbound;
  }

  public HomeModel getData(Integer id, Context context) {
    Span span = tracer.buildSpan("HomeService.getData").asChildOf(context.getSpan()).start();

    HomeModel homeModel = homeOutbound.getOrder(id, context);

    foo();

    span.setTag("id", id);
    span.setTag("homeModel", String.valueOf(homeModel));
    span.finish();
    return homeModel;
  }

  private void foo(){
    try {
      TimeUnit.MILLISECONDS.sleep(100);
    }catch (Exception e){

    }

  }
}

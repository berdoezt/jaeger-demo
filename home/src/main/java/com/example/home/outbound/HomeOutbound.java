package com.example.home.outbound;

import com.example.home.model.Context;
import com.example.home.model.HomeModel;
import com.example.home.model.OrderResponse;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.tag.Tags;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HomeOutbound {

  private final Tracer tracer;

  @Autowired
  public HomeOutbound(Tracer tracer) {
    this.tracer = tracer;
  }

  public class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {

    private final HttpHeaders httpHeaders;

    RequestBuilderCarrier(HttpHeaders httpHeaders) {
      this.httpHeaders = httpHeaders;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
      throw new UnsupportedOperationException("carrier is write-only");
    }

    @Override
    public void put(String key, String value) {
      httpHeaders.add(key, value);
    }
  }

  public HomeModel getOrder(Integer id, Context context) {
    String url = String.format("http://localhost:8081/order/%d", id);
    HttpHeaders headers = new HttpHeaders();

    Span span = tracer.buildSpan("HomeOutbound.getOrder").asChildOf(context.getSpan()).start();

    Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_CLIENT);
    Tags.HTTP_METHOD.set(span, "GET");
    Tags.HTTP_URL.set(span, url);
    tracer.inject(span.context(), Builtin.HTTP_HEADERS,
        new RequestBuilderCarrier(headers));

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<OrderResponse> orderResponseResponseEntity = restTemplate
        .exchange(url, HttpMethod.GET, entity, OrderResponse.class);

    OrderResponse orderResponse = orderResponseResponseEntity.getBody();

    span.log("get data from order outbound");
    span.setTag("orderResponse", String.valueOf(orderResponse));

    HomeModel homeModel;

    if (Objects.isNull(orderResponse)) {
      homeModel = null;
    } else {
      homeModel = HomeModel.builder()
          .name(orderResponse.getName())
          .id(orderResponse.getId())
          .build();
    }

    span.finish();

    return homeModel;
  }
}

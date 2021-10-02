package domain

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/opentracing/opentracing-go"
	"github.com/opentracing/opentracing-go/ext"
	"github.com/uber/jaeger-client-go"
	"github.com/uber/jaeger-client-go/config"
	logJaeger "github.com/uber/jaeger-client-go/log"
	metricsJaeger "github.com/uber/jaeger-lib/metrics"
)

func HandlerGetOrder(w http.ResponseWriter, r *http.Request) {
	var err error
	var resultByte []byte

	cfg := &config.Configuration{
		ServiceName: "order",
		Sampler: &config.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
		Reporter: &config.ReporterConfig{
			LogSpans: true,
		},
	}

	jLogger := logJaeger.StdLogger
	jMetricsFactory := metricsJaeger.NullFactory

	tracer, closer, err := cfg.NewTracer(
		config.Logger(jLogger),
		config.Metrics(jMetricsFactory),
	)
	defer closer.Close()

	if err != nil {
		log.Println("[TRACER] can not start tracer", err)
	}

	opentracing.SetGlobalTracer(tracer)

	spanCtx, _ := tracer.Extract(opentracing.HTTPHeaders, opentracing.HTTPHeadersCarrier(r.Header))

	serverSpan := tracer.StartSpan("OrderController.GetOrder", ext.RPCServerOption(spanCtx))
	defer serverSpan.Finish()

	defer func() {
		serverSpan.SetTag("request", r)
		serverSpan.SetTag("response", string(resultByte))
		if err != nil {
			serverSpan.SetTag("error", true)
			serverSpan.LogKV("errorDesc", err.Error())
		} else {
			serverSpan.SetTag("error", false)
		}
	}()

	idReq := strings.TrimPrefix(r.URL.Path, "/order/")

	id, err := strconv.Atoi(idReq)
	if err != nil {
		return
	}

	time.Sleep(250 * time.Millisecond)

	ctx := context.Background()
	ctx = context.WithValue(ctx, "jaegerCtx", serverSpan.Context())

	result, err := GetOrder(ctx, id)
	if err != nil {
		return
	}

	resultByte, err = json.Marshal(result)
	if err != nil {
		return
	}

	w.Header().Add("Content-Type", "application/json")
	w.WriteHeader(200)
	w.Write([]byte(resultByte))

}

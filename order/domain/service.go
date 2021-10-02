package domain

import (
	"context"
	"errors"
	"time"

	"github.com/opentracing/opentracing-go"
)

func GetOrder(ctx context.Context, id int) (Order, error) {

	var err error
	var order Order

	tracer := opentracing.GlobalTracer()

	jaegerCtx := ctx.Value("jaegerCtx").(opentracing.SpanContext)

	span := tracer.StartSpan("OrderService.GetOrder", opentracing.ChildOf(jaegerCtx))
	defer span.Finish()

	defer func() {
		if err != nil {
			span.SetTag("error", true)
			span.LogKV("errorDesc", err.Error())
		} else {
			span.SetTag("error", false)
			span.SetTag("id", id)
			span.SetTag("order", order)
		}
	}()

	time.Sleep(100 * time.Millisecond)

	if id == 234 {
		return order, errors.New("order: invalid order id")
	}

	order = Order{
		Id:        id,
		Price:     10000,
		Name:      "Penerbangan ke Bali",
		OrderType: "FLIGHT",
	}

	return order, nil
}

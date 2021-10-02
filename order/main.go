package main

import (
	"jaeger-demo/order/domain"
	"log"
	"net/http"
)

func main() {

	http.HandleFunc("/order/", domain.HandlerGetOrder)

	err := http.ListenAndServe("localhost:8081", nil)
	if err != nil {
		log.Fatal("failed to start server")
	}

	log.Println("server start on port 8081")
}

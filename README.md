Jaeger Demo

Services:
- Home : Java
- Order : Go

Architecture
Home -> Order

Run Jaeger All-in-One
```
docker run -d --name jaeger -p 16686:16686 -p 6831:6831/udp jaegertracing/all-in-one:1.22
```

Open jaeger console in ```http://localhost:16686```
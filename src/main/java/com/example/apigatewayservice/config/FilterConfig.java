package com.example.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration  // 스프링부트 동작 시 @Configuration이 달린 어노테이션을 모아서 메모리에 등록함
public class FilterConfig {
//    @Bean   // 이 때 등록하는 빈의 이름이 RouteLocator
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/first-service/**")     // first-service라는 작업이 들어오면 아래2줄 작업을한다는뜻.
                        .filters(f -> f.addRequestHeader("first-request", "first-request-header")   // 테스트로 추가한 값
                                .addResponseHeader("first-response", "first-response-header"))      // 테스트로 추가한 값
                        .uri("http://localhost:8081"))  // 필터작업 후 이동한다.
                .route(r -> r.path("/second-service/**")
                        .filters(f -> f.addRequestHeader("second-request", "second-request-header")
                                .addResponseHeader("second-response", "second-response-header"))
                        .uri("http://localhost:8082"))
                .build();
    }
}

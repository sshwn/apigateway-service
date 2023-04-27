package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);    // 새롭게 추가되는 필터 클래스에서 Config(아무것도없어도 해야함) 정보를 필터에적용할수있는 부가정보로서 캐스팅 시켜주는 작업을 부모클래스에 알려주어야한다.  (안하면 class java.lang.Object cannot be cast to class com.example.apigatewayservice.filter.AuthorizationHeaderFilter$Config 오류남)
        this.env = env;
    }

    public static class Config {

    }

    // login -> token반환받음 -> users 호출시 token 정보로 요청한다 -> 토큰은 header에 존재한다
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // api 호출할때 사용자가 header에다 로그인했을때 받았던 토큰을 전달해주는 작업.
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {    // false면 해당하는 값을 사용할 수 없다
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            String authorizationHeader = request.getHeaders().get(org.springframework.http.HttpHeaders.AUTHORIZATION).get(0);   // authorizationHeader 정상적으로 로그인했을때 전달받은 토큰값이 존재할것
            // header 값 안에 토큰이 존재하는지 검사
            String jwt = authorizationHeader.replace("Bearer", "");
            
            if(!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }
            
            return chain.filter(exchange);  // 통과메시지 반환
        });
    }

    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;

        String subject = null;

        try {
            // 값이 정상인지 판단 (user-service에서 토큰 생성한 방식 key 값을 세팅(token.secret))
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(jwt).getBody()
                    .getSubject();

        } catch (Exception ex) {
            returnValue = false;
        }

        if(subject == null || subject.isEmpty()) {
            returnValue = false;
        }



        return returnValue;
    }

    // Mono(단일값), Flux(여러개값) -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        log.error(err);
        return response.setComplete();  // Mono타입으로 전달됨
    }
}

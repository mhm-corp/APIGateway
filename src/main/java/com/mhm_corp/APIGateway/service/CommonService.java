package com.mhm_corp.APIGateway.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CommonService {

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public <T, R> ResponseEntity<R> executeRequest(T request, String endpoint, Class<R> responseType, HttpMethod method,
                                                   String serviceUrl, RestTemplate restTemplate) {
        String url = serviceUrl + endpoint;
        HttpEntity<T> requestEntity = new HttpEntity<>(request, createHeaders());

        ResponseEntity<R> response = restTemplate.exchange(
                url,
                method,
                requestEntity,
                responseType
        );

        return (ResponseEntity<R>) createResponseEntity(response);
    }

    private ResponseEntity<?> createResponseEntity(ResponseEntity<?> response) {
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}

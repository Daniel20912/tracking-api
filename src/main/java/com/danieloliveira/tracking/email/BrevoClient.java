package com.danieloliveira.tracking.email;

import com.danieloliveira.tracking.email.dto.BrevoEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "brevoClient", url = "${brevo.api.url}", configuration = BrevoFeignConfig.class)
public interface BrevoClient {

    @PostMapping("/smtp/email")
    void sendTransactionalEmail(@RequestBody BrevoEmailRequest request);
}

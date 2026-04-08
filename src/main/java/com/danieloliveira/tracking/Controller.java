package com.danieloliveira.tracking;

import com.danieloliveira.tracking.trackingClient.TrackResponse;
import com.danieloliveira.tracking.trackingClient.TrackingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("teste")
public class Controller {

    @Autowired
    private TrackingClient trackingClient;

    @GetMapping("/{codigo}")
    public ResponseEntity<?> rastrearPacote(@PathVariable String codigo) {

        TrackResponse resposta = trackingClient.findTrack(codigo);

        return ResponseEntity.ok(resposta);
    }
}

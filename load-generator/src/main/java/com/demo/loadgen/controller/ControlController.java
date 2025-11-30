package com.demo.loadgen.controller;

import com.demo.loadgen.model.LoadStats;
import com.demo.loadgen.service.LoadGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ControlController {

    @Autowired
    private LoadGeneratorService loadGeneratorService;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "E-Commerce Load Generator");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("endpoints", "/control, /stats, /enable, /disable, /intensity, /reset");
        return ResponseEntity.ok(info);
    }

    @GetMapping("/control")
    public ResponseEntity<Map<String, String>> control() {
        Map<String, String> controls = new HashMap<>();
        controls.put("enable", "POST /enable - Enable load generation");
        controls.put("disable", "POST /disable - Disable load generation");
        controls.put("intensity", "POST /intensity?level={low|medium|high} - Set load intensity");
        controls.put("reset", "POST /reset - Reset statistics");
        controls.put("stats", "GET /stats - View statistics");
        return ResponseEntity.ok(controls);
    }

    @GetMapping("/stats")
    public ResponseEntity<LoadStats> getStats() {
        return ResponseEntity.ok(loadGeneratorService.getStatistics());
    }

    @PostMapping("/enable")
    public ResponseEntity<Map<String, String>> enableLoad() {
        loadGeneratorService.setLoadEnabled(true);
        Map<String, String> response = new HashMap<>();
        response.put("status", "enabled");
        response.put("message", "Load generation enabled");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disableLoad() {
        loadGeneratorService.setLoadEnabled(false);
        Map<String, String> response = new HashMap<>();
        response.put("status", "disabled");
        response.put("message", "Load generation disabled");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/intensity")
    public ResponseEntity<Map<String, String>> setIntensity(@RequestParam String level) {
        loadGeneratorService.setLoadIntensity(level);
        Map<String, String> response = new HashMap<>();
        response.put("intensity", level);
        response.put("message", "Load intensity set to " + level);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetStats() {
        loadGeneratorService.resetStatistics();
        Map<String, String> response = new HashMap<>();
        response.put("status", "reset");
        response.put("message", "Statistics reset successfully");
        return ResponseEntity.ok(response);
    }
}



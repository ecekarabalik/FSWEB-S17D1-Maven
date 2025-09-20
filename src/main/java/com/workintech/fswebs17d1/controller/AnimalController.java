package com.workintech.fswebs17d1.controller;

import com.workintech.fswebs17d1.entity.Animal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/workintech/animal")
public class AnimalController {

    // application.properties'ten değer çekme
    @Value("${course.name}")
    private String courseName;

    @Value("${project.developer.fullname}")
    private String developerFullName;

    // In-memory "DB"
    private final Map<Integer, Animal> animals = new ConcurrentHashMap<>();

    // örnek data
    public AnimalController() {
        animals.put(1, new Animal(1, "Cat"));
        animals.put(2, new Animal(2, "Dog"));
    }

    // opsiyonel: info endpoint (@Value kullanımı)
    @GetMapping("/info")
    public Map<String, String> info() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("course.name", courseName);
        map.put("project.developer.fullname", developerFullName);
        return map;
    }

    // [GET] /workintech/animal  -> map'in value'larını List döner
    @GetMapping
    public List<Animal> findAll() {
        return new ArrayList<>(animals.values());
    }

    // [GET] /workintech/animal/{id} -> varsa value'yu döner
    @GetMapping("/{id}")
    public ResponseEntity<Animal> findById(@PathVariable Integer id) {
        Animal a = animals.get(id);
        return (a != null) ? ResponseEntity.ok(a) : ResponseEntity.notFound().build();
    }

    // [POST] /workintech/animal -> id ve name alır ve map'e ekler
    // İstek: JSON body: {"id":3,"name":"Bird"}   (veya @RequestParam ile de yazılabilirdi)
    @PostMapping
    public ResponseEntity<Animal> create(@RequestBody Animal request) {
        if (request.getId() == null || request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        animals.put(request.getId(), new Animal(request.getId(), request.getName()));
        return ResponseEntity.created(URI.create("/workintech/animal/" + request.getId()))
                .body(request);
    }

    // [PUT] /workintech/animal/{id}
    // Not: Görev tanımına göre "path id'deki kaydı, RequestBody'den aldığı id ile güncelle"
    // yani key değişimine izin veriyoruz (nadirdir ama istenmiş).
    @PutMapping("/{id}")
    public ResponseEntity<Animal> update(@PathVariable Integer id, @RequestBody Animal body) {
        if (!animals.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        if (body.getId() == null || body.getName() == null || body.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        // eski key'i sil, yeni key ile ekle
        animals.remove(id);
        animals.put(body.getId(), new Animal(body.getId(), body.getName()));
        return ResponseEntity.ok(animals.get(body.getId()));
    }

    // [DELETE] /workintech/animal/{id} -> siler
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Animal removed = animals.remove(id);
        return (removed != null) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
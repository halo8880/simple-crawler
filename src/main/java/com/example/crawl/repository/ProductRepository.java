package com.example.crawl.repository;

import com.example.crawl.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	@Query(value = "select p.url from Product p where :now - p.lastImportAt=1")
	Set<String> getAllUrls(@Param("now") LocalDateTime now);

	Optional<Product> findByUrl(String url);

//	Flux<Product> findAll();
}

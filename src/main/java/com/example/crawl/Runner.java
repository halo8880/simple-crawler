package com.example.crawl;

import com.example.crawl.service.ProductCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {
	@Autowired
	ProductCrawlerService crawlerService;

	@Override
	public void run(String... args) throws Exception {
		crawlerService.doCrawl();
	}
}

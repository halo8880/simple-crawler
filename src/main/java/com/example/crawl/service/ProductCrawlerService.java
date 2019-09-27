package com.example.crawl.service;

import com.example.crawl.dto.PriceDTO;
import com.example.crawl.entity.Product;
import com.example.crawl.repository.ProductRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class ProductCrawlerService {
	@Autowired
	private ProductRepository productRepository;


	private static String productMainSelector = ".product-info-main";
	private static String titleSelector = ".product-info-main .page-title-wrapper .page-title .base";
	private static String priceSelector = ".product-info-main .product-info-price .price-container meta";
	private static String detailsSelector = "#description .description .value > p";
	private static String moreInfoSelector = "#additional #product-attribute-specs-table tbody tr";
	private static String startSite = "http://magento-test.finology.com.my/breathe-easy-tank.html";
	private static ExecutorService executor = Executors.newFixedThreadPool(10);
	private int count = 0;

	public void doCrawl() {
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		Set<String> linksImportedLessThanOneHour = productRepository.findAll().stream()
				.filter(product -> product.getLastImportAt().plusHours(1).isAfter(now))
				.map(product -> product.getUrl())
				.collect(Collectors.toSet());
		executor.submit(() -> getPageLinks(startSite, linksImportedLessThanOneHour));
	}

	@Async
	public Future<Set<String>> getPageLinks(String url, Set<String> excludedLinks) {
		try {
			if (url.equals(startSite) || shouldCrawlUrl(url, excludedLinks)) {

				Document document = Jsoup.connect(url).
						timeout(Integer.MAX_VALUE).get();
				addLinkSync(url, excludedLinks);
				System.out.println("Requests were made: " + ++count);

				boolean isProductPage = !document.select(productMainSelector).isEmpty();
				if (isProductPage) {
					Product product = getProductFromPage(url, document);
					saveProduct(url, product);
					System.out.println("Collected product: \n");
					System.out.println(product.toString());
				}

				Elements linksOnPage = document.select("a[href]");
				for (Element page : linksOnPage) {
					executor.submit(() ->
							getPageLinks(page.attr("abs:href"), excludedLinks)
					);
				}
			}
		} catch (Exception e) {
			System.out.println("failed for " + url);
			System.out.println(e.getMessage());
		}
		return CompletableFuture.completedFuture(excludedLinks);
	}

	private Product getProductFromPage(String url, Document document) {
		String title = getTitle(document);
		PriceDTO price = getPrice(document);
		String details = getDetails(document);
		String moreInfo = getMoreInfo(document);
		Product product = new Product(title,
				Double.parseDouble(price.getPrice()), price.getPriceCurrency(),
				details,
				moreInfo,
				url,
				LocalDateTime.now(ZoneOffset.UTC)
		);
		return product;
	}

	private synchronized void saveProduct(String url, Product product) {
		productRepository.findByUrl(url).ifPresent(dbProduct ->
				product.setId(dbProduct.getId())
		);
		productRepository.save(product);
	}

	private String getTitle(Document document) {
		Elements elms = document.select(titleSelector);
		if (!CollectionUtils.isEmpty(elms)) {
			return Parser.unescapeEntities(elms.get(0).text(), true);
		} else {
			return "";
		}
	}

	private PriceDTO getPrice(Document document) {
		Elements elms = document.select(priceSelector);
		if (!CollectionUtils.isEmpty(elms) && elms.size() >= 2) {
			String price = elms.get(0).attr("content");
			String priceCurrency = elms.get(1).attr("content");
			return new PriceDTO(price, priceCurrency);
		} else {
			return new PriceDTO("", "");
		}
	}

	private String getDetails(Document document) {
		Elements elms = document.select(detailsSelector);
		StringBuilder details = new StringBuilder();
		if (!CollectionUtils.isEmpty(elms)) {
			elms.forEach(elm -> {
				details.append(elm.text())
						.append("\n");
			});
			return Parser.unescapeEntities(details.toString(), true);
		} else {
			return "";
		}
	}

	private String getMoreInfo(Document document) {
		Elements trs = document.select(moreInfoSelector);
		StringBuilder rs = new StringBuilder();
		if (!trs.isEmpty()) {
			trs.forEach(tr -> {
				String label = tr.select(".label").text();
				String data = tr.select(".data").text();
				rs.append(label).append(" : ").append(data).append("\n");
			});
			return Parser.unescapeEntities(rs.toString(), true);
		} else {
			return "";
		}
	}

	private synchronized boolean shouldCrawlUrl(String url, Set<String> links) {
		return links.contains(getUrlWithoutSharp(url));
	}

	private synchronized boolean addLinkSync(String url, Set<String> links) {
		return links.add(getUrlWithoutSharp(url));
	}

	private String getUrlWithoutSharp(String originalUrl) {
		return originalUrl.split("\\.html")[0] + ".html";
	}
}

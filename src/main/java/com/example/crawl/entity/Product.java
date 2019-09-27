package com.example.crawl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = "url"))
@Getter
@Setter
@NoArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String productName;
	private Double price;
	private String priceCurrency;
	private String details;
	private String moreInfo;
	private String url;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime lastImportAt;

	public Product(String productName, Double price,
				   String priceCurrency, String details,
				   String moreInfo, String url,
				   LocalDateTime lastImportAt) {
		this.productName = productName;
		this.price = price;
		this.priceCurrency = priceCurrency;
		this.details = details;
		this.moreInfo = moreInfo;
		this.url = url;
		this.lastImportAt = lastImportAt;
	}

	@Override
	public String toString() {
		StringBuilder rs = new StringBuilder();
		rs
				.append("Product Name: ").append(this.productName).append("\n")
				.append("Price: ").append(this.price).append(" ").append(this.priceCurrency).append("\n")
				.append("Description: ").append(this.details).append("\n")
				.append("Extra information: ").append(this.moreInfo);
		return rs.toString();
	}
}

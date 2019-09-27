package com.example.crawl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceDTO {
	private String price;
	private String priceCurrency;

	public PriceDTO(String price, String priceCurrency) {
		this.price = price;
		this.priceCurrency = priceCurrency;
	}
}

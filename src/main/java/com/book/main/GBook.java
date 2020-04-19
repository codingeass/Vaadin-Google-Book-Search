package com.book.main;




public class GBook  {
	private String title="";
	private String description="";
	private String publicationDate="";
	private String authorName="";
	private String imageUrl="";
	private Double price;
	
	public GBook() {
		super();
	}

	public GBook(String title, String description, String publicationDate, String authorName, String imageUrl, Double price) {
		super();
		this.title = title;
		this.description = description;
		this.publicationDate = publicationDate;
		this.authorName = authorName;
		this.imageUrl = imageUrl;
		this.price=price;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPublicationDate() {
		return publicationDate;
	}
	public void setPublicationDate(String publicationDate) {
		this.publicationDate = publicationDate;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
	
	
}

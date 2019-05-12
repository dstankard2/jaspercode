package net.sf.jaspercode.example1.obj;

import net.sf.jaspercode.patterns.java.handwritten.DataObject;
import net.sf.jaspercode.patterns.java.handwritten.DataObjectAttribute;

@DataObject(name = "Book", priority = 0)
public class Book {

	@DataObjectAttribute
	private Integer bookId = null;
	
	@DataObjectAttribute
	private String title = null;
	
	@DataObjectAttribute
	private String publisher = null;

	public Book() {
	}

	public Book(Integer bookId, String title, String publisher) {
		super();
		this.bookId = bookId;
		this.title = title;
		this.publisher = publisher;
	}

	public Integer getBookId() {
		return bookId;
	}

	public void setBookId(Integer bookId) {
		this.bookId = bookId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

}


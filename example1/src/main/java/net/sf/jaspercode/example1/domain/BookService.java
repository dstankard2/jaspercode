package net.sf.jaspercode.example1.domain;

import net.sf.jaspercode.example1.entity.Book;
import net.sf.jaspercode.example1.entity.DefaultDaoFactory;
import net.sf.jaspercode.example1.obj.BookData;
import net.sf.jaspercode.patterns.java.handwritten.BusinessRule;
import net.sf.jaspercode.patterns.java.handwritten.BusinessService;
import net.sf.jaspercode.patterns.java.handwritten.Dependency;

@BusinessService(group = "Domain", ref="bookService", priority=20000)
public class BookService {

	DefaultDaoFactory ds;
	
	@Dependency
	public void setDs(DefaultDaoFactory ds) {
		this.ds = ds;
	}

	@BusinessRule
	public BookData getBookData(Integer bookId) {
		BookData bookData = null;
		
		if (bookId!=null) {
			Book book = ds.getBookDao().getBook(bookId);
			if (book!=null) {
				bookData = new BookData(bookId, book.getTitle());
			}
		}
		
		return bookData;
	}
	
}


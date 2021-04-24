package net.sf.jaspercode.example1.subsystem.impl;

import java.util.List;

import net.sf.jaspercode.example1.obj.BookData;
import net.sf.jaspercode.example1.subsystem.BookSubsystem;

public class BookSubsystemImpl extends BookSubsystem {

	@Override
	public void addBookCopy(Integer bookId) {
	}

	@Override
	public List<BookData> getBookDataList(Integer bookShelfId) {
		
		return null;
	}

	@Override
	public List<BookData> getBooksByAuthor(String author) {
		//List<Book> books = ds.
		return null;
	}

}

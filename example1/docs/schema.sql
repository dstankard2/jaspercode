
drop table BookShelf;
create table example1.BookShelf (
	bookShelfId INT NOT NULL AUTO_INCREMENT,
	shelfTitle varchar(32) NOT NULL,
	PRIMARY KEY (`bookShelfId`)
) ENGINE=InnoDB;

drop table Book;
create table example1.Book (
	bookId INT NOT NULL AUTO_INCREMENT,
	title varchar(64) NOT NULL,
	author varchar(64) NOT NULL,
	PRIMARY KEY (`bookId`)
) ENGINE=InnoDB;

drop table BookCopy;
create table example1.BookCopy (
	bookCopyId INT NOT NULL AUTO_INCREMENT,
	bookId INT NOT NULL,
	bookStatus VARCHAR(16) NOT NULL,
	PRIMARY KEY (`bookCopyId`)
) ENGINE=InnoDB;

drop table BookCopyCheckout;
create table example1.BookCopyCheckout (
	bookCopyCheckoutId INT NOT NULL AUTO_INCREMENT,
	bookCopyId INT NOT NULL,
	checkoutDate DATE NOT NULL,
	dueDate DATE NOT NULL,
	checkinDate DATE NULL,
	PRIMARY KEY (`bookCopyCheckoutId`)
) ENGINE=InnoDB;


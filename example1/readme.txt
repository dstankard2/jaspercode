
JasperCode example application: Library

This application is a Librarian application, simulating a library where you can add, checkout and checkin books.

The UI consists of a portal page that allows the user to navigate between multiple pages:
	NavigationPage: Links to other pages in the UI.
	Book Shelf Display page: Browse shelves, add shelves, view shelf, add book to shelf, add copies of book.
	Book Display Page: View a book's details and check out a copy if the book is available.
	Book Check In Page: Check in a copy of a book that is checked out.

Component structure

The application consists of book data in the local MySQL database, along with functionality to insert, update and retrieve it.

Page -> Page function -> Javascript module client -> Module HTTP endpoint -> Module Service -> Domain service -> Repository -> DB



The application definition includes a Maven build, and the application can be built via Maven.

The application is backed by a MySQL database.  You should use MySQL on your local machine if you want to run the 
example application.



Example 1 Setup

cd <build directory>
cd example1
mvn clean package
java -jar target/example1-jar-with-dependencies.jar

Install MySQL


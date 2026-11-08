\# Travel Tourism



A travel and tourism booking website built as a JEE training project.

Customers can browse and search tours, see the hotels linked to each tour, and book a tour

with up to three hotels and a room type. An admin manages tours, hotels and room allotments.



\## Features



\*\*Customer\*\*

\- Register, login and logout

\- Search tours by destination

\- View a tour's details and its hotels (with rooms left)

\- Book a tour: choose up to 3 hotels, AC or Non-AC room, travel date, number of people

\- My Bookings page, with cancel

\- Booking Details: search a booking by ID



\*\*Admin\*\*

\- Add, update and delete tours

\- Add, update and delete hotels

\- Link hotels to tours with a room allotment

\- Search any booking by ID



\## Tech stack



\- Front end: HTML, CSS, Bootstrap 5, JavaScript (fetch)

\- Back end: Java Servlets on Apache Tomcat 9, JDBC

\- Database: MySQL 8



\## How to run



1\. Install a JDK, Apache Tomcat 9 and MySQL 8.

2\. Create the database and import the data:



&#x20;       mysql -u root -p -e "CREATE DATABASE travel\_tourism"

&#x20;       mysql -u root -p travel\_tourism < database/travel\_tourism.sql



3\. Open `src/main/java/utilities/DBConnection.java` and put your MySQL password in `PASSWORD`.

4\. Import the project into Eclipse (File > Import > Existing Projects into Workspace) and run `Index.html` on Tomcat 9.

5\. Open `http://localhost:8080/TRAVEL\_TOURISM/Index.html`



\## Demo accounts



\- Admin: `admin@travel.com` / `admin123`

\- Customer: register a new account from the site



\## Project structure



&#x20;   src/main/java

&#x20;     controller/   servlets (login, register, booking, admin pages)

&#x20;     dao/          database access with JDBC

&#x20;     model/        plain data classes

&#x20;     utilities/    DBConnection

&#x20;   src/main/webapp

&#x20;     Index.html, Script.js, Style.css     customer site

&#x20;     admin\*.html, admin\*.js               admin pages

&#x20;     booking-details.html, .js            booking search

&#x20;   database/travel\_tourism.sql            tables and sample data



\## Notes



Passwords are stored as plain text because this is a training project.

A real application would hash them (for example with BCrypt).s


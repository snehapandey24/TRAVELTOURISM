document.addEventListener("DOMContentLoaded", function() {

    // ---------- Elements ----------
    let tourList = document.getElementById("tourList");
    let tourSearch = document.getElementById("tourSearch");
    let noToursMessage = document.getElementById("noToursMessage");

    let tourDetails = document.getElementById("tourDetails");
    let tourName = document.getElementById("tourName");
    let tourDuration = document.getElementById("tourDuration");
    let tourPrice = document.getElementById("tourPrice");
    let tourDescription = document.getElementById("tourDescription");
    let hotelSection = document.getElementById("hotelSection");
    let hotelList = document.getElementById("hotelList");

    let bookingForm = document.getElementById("bookingForm");
    let bookingFormElement = document.getElementById("bookingFormElement");
    let bookingMessage = document.getElementById("bookingMessage");
    let customerName = document.getElementById("customerName");
    let bookingTourName = document.getElementById("bookingTourName");
    let hotelFields = document.getElementById("hotelFields");
    let hotelSelects = [
        document.getElementById("hotel1"),
        document.getElementById("hotel2"),
        document.getElementById("hotel3")
    ];
    let roomType = document.getElementById("roomType");

    let authSection = document.getElementById("authSection");
    let authTitle = document.getElementById("authTitle");
    let authMessage = document.getElementById("authMessage");
    let loginForm = document.getElementById("loginForm");
    let registerForm = document.getElementById("registerForm");
    let switchAuth = document.getElementById("switchAuth");

    let userGreeting = document.getElementById("userGreeting");
    let loginNavBtn = document.getElementById("loginNavBtn");
    let registerNavBtn = document.getElementById("registerNavBtn");
    let logoutNavBtn = document.getElementById("logoutNavBtn");
    let adminNavBtn = document.getElementById("adminNavBtn");

    let myBookingsNavBtn = document.getElementById("myBookingsNavBtn");
    let myBookingsSection = document.getElementById("myBookingsSection");
    let myBookingsBody = document.getElementById("myBookingsBody");
    let myBookingsEmpty = document.getElementById("myBookingsEmpty");
    let myBookingsMessage = document.getElementById("myBookingsMessage");

    let allTours = [];
    let tourHotels = [];
    let selectedTourId = null;
    let isLoggedIn = false;
    let currentUserName = "";

    // ---------- Helpers ----------
    function formatPrice(price) {
        return "Rs " + Number(price).toLocaleString("en-IN");
    }

    function showAlert(element, text, success) {
        element.textContent = text;
        element.className = "alert " + (success ? "alert-success" : "alert-danger");
        element.style.display = "block";
    }

    function setLoggedIn(name, role) {
        isLoggedIn = true;
        currentUserName = name;
        userGreeting.textContent = "Hi, " + name;
        userGreeting.style.display = "block";
        logoutNavBtn.style.display = "block";
        myBookingsNavBtn.style.display = "block";
        loginNavBtn.style.display = "none";
        registerNavBtn.style.display = "none";
        if (adminNavBtn) {
            adminNavBtn.style.display = role === "ADMIN" ? "block" : "none";
        }
    }

    function setLoggedOut() {
        isLoggedIn = false;
        currentUserName = "";
        userGreeting.style.display = "none";
        logoutNavBtn.style.display = "none";
        myBookingsNavBtn.style.display = "none";
        myBookingsSection.style.display = "none";
        loginNavBtn.style.display = "block";
        registerNavBtn.style.display = "block";
        if (adminNavBtn) {
            adminNavBtn.style.display = "none";
        }
    }

    function openAuth(mode, message) {
        bookingForm.style.display = "none";
        myBookingsSection.style.display = "none";
        authMessage.style.display = "none";
        if (mode === "register") {
            authTitle.textContent = "Register";
            loginForm.style.display = "none";
            registerForm.style.display = "block";
            switchAuth.textContent = "Already have an account? Login";
        } else {
            authTitle.textContent = "Login";
            loginForm.style.display = "block";
            registerForm.style.display = "none";
            switchAuth.textContent = "New here? Register";
        }
        if (message) {
            showAlert(authMessage, message, false);
        }
        authSection.style.display = "block";
        authSection.scrollIntoView({ behavior: "smooth" });
    }

    // ---------- Tours (loaded from the database) ----------
    function renderTours(tours) {
        tourList.innerHTML = "";
        noToursMessage.textContent = "No tours found.";
        noToursMessage.style.display = tours.length === 0 ? "block" : "none";

        tours.forEach(function(tour) {
            let col = document.createElement("div");
            col.className = "col-md-4";

            let card = document.createElement("div");
            card.className = "card";

            if (tour.imageUrl) {
                let img = document.createElement("img");
                img.src = tour.imageUrl;
                img.className = "card-img-top";
                img.alt = tour.destination;
                card.appendChild(img);
            }

            let body = document.createElement("div");
            body.className = "card-body";

            let title = document.createElement("h5");
            title.className = "card-title";
            title.textContent = tour.destination;

            let duration = document.createElement("p");
            duration.className = "card-text";
            duration.textContent = tour.duration;

            let price = document.createElement("p");
            price.className = "card-text";
            price.textContent = "Starting From " + formatPrice(tour.price);

            let button = document.createElement("a");
            button.href = "#";
            button.className = "btn btn-primary tour-btn";
            button.setAttribute("data-tour-id", tour.tourId);
            button.textContent = "View Tour";

            body.append(title, duration, price, button);
            card.appendChild(body);
            col.appendChild(card);
            tourList.appendChild(col);
        });
    }

    function loadTours() {
        fetch("tours")
            .then(function(response) { return response.json(); })
            .then(function(tours) {
                allTours = tours;
                renderTours(allTours);
            })
            .catch(function(error) {
                console.error("Could not load tours:", error);
                noToursMessage.textContent = "Could not load tours. Is Tomcat running?";
                noToursMessage.style.display = "block";
            });
    }

    // ---------- Hotels of the selected tour ----------
    function loadTourHotels(tourId) {
        return fetch("tour-hotels?tourId=" + encodeURIComponent(tourId))
            .then(function(response) { return response.ok ? response.json() : []; })
            .then(function(hotels) {
                tourHotels = hotels;
                hotelList.innerHTML = "";
                hotels.forEach(function(h) {
                    let li = document.createElement("li");
                    li.textContent = h.hotelName + " (" + h.allotment + " rooms left)";
                    hotelList.appendChild(li);
                });
                hotelSection.style.display = hotels.length > 0 ? "block" : "none";
            })
            .catch(function() {
                tourHotels = [];
                hotelSection.style.display = "none";
            });
    }

    function fillHotelSelects() {
        hotelSelects.forEach(function(select) {
            select.innerHTML = "";

            let none = document.createElement("option");
            none.value = "";
            none.textContent = "-- none --";
            select.appendChild(none);

            tourHotels.forEach(function(h) {
                let option = document.createElement("option");
                option.value = h.hotelId;
                option.textContent = h.hotelName;
                select.appendChild(option);
            });
        });
        hotelFields.style.display = tourHotels.length > 0 ? "block" : "none";
    }

    // Search tour
    tourSearch.addEventListener("input", function() {
        let text = tourSearch.value.trim().toLowerCase();
        let filtered = allTours.filter(function(tour) {
            return tour.destination.toLowerCase().includes(text);
        });
        renderTours(filtered);
    });

    // View Tour (one listener handles every card, including ones drawn later)
    tourList.addEventListener("click", function(event) {
        let button = event.target.closest(".tour-btn");
        if (!button) return;
        event.preventDefault();

        selectedTourId = button.getAttribute("data-tour-id");
        let tour = allTours.find(function(t) {
            return String(t.tourId) === selectedTourId;
        });
        if (!tour) return;

        tourName.textContent = tour.destination;
        tourDuration.textContent = "Duration: " + tour.duration;
        tourPrice.textContent = "Price: " + formatPrice(tour.price);
        tourDescription.textContent = tour.description;

        myBookingsSection.style.display = "none";
        bookingForm.style.display = "none";
        tourDetails.style.display = "block";
        loadTourHotels(selectedTourId);
        tourDetails.scrollIntoView({ behavior: "smooth" });
    });

    document.getElementById("closeDetails").addEventListener("click", function() {
        tourDetails.style.display = "none";
    });

    document.getElementById("exploreBtn").addEventListener("click", function(event) {
        event.preventDefault();
        document.getElementById("toursSection").scrollIntoView({ behavior: "smooth" });
    });

    // ---------- Check if already logged in (survives page refresh) ----------
    fetch("session")
        .then(function(response) {
            if (response.ok) {
                let role = response.headers.get("X-User-Role");
                return response.text().then(function(name) {
                    setLoggedIn(name, role);
                });
            }
            setLoggedOut();
        })
        .catch(function() { setLoggedOut(); });

    // ---------- Booking form ----------
    document.getElementById("bookNowBtn").addEventListener("click", function(event) {
        event.preventDefault();

        if (!isLoggedIn) {
            openAuth("login", "Please log in to book a tour.");
            return;
        }

        bookingMessage.style.display = "none";
        customerName.value = currentUserName;
        bookingTourName.value = tourName.textContent;
        fillHotelSelects();
        bookingForm.style.display = "block";
        bookingForm.scrollIntoView({ behavior: "smooth" });
    });

    document.getElementById("closeBooking").addEventListener("click", function() {
        bookingForm.style.display = "none";
    });

    bookingFormElement.addEventListener("submit", function(event) {
        event.preventDefault();

        if (!selectedTourId) {
            showAlert(bookingMessage, "Please select a tour first.", false);
            return;
        }

        let formData = new URLSearchParams();
        formData.append("tourId", selectedTourId);
        formData.append("travelDate", document.getElementById("travelDate").value);
        formData.append("numberOfPeople", document.getElementById("numberOfPeople").value);
        formData.append("roomType", roomType.value);
        hotelSelects.forEach(function(select, index) {
            if (select.value) {
                formData.append("hotelId" + (index + 1), select.value);
            }
        });

        fetch("book", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            if (response.status === 401) {
                setLoggedOut();
                openAuth("login", "Your session expired. Please log in again.");
                return;
            }
            return response.text().then(function(text) {
                showAlert(bookingMessage, text, response.ok);
                if (response.ok) {
                    bookingFormElement.reset();
                    customerName.value = currentUserName;
                    bookingTourName.value = tourName.textContent;
                    // rooms were used up, so reload the hotel list
                    loadTourHotels(selectedTourId).then(fillHotelSelects);
                }
            });
        })
        .catch(function(error) {
            console.error("Booking error:", error);
            showAlert(bookingMessage, "Could not reach the server. Is Tomcat running?", false);
        });
    });

    // ---------- Login / Register ----------
    loginNavBtn.addEventListener("click", function(event) {
        event.preventDefault();
        openAuth("login");
    });

    registerNavBtn.addEventListener("click", function(event) {
        event.preventDefault();
        openAuth("register");
    });

    switchAuth.addEventListener("click", function(event) {
        event.preventDefault();
        openAuth(loginForm.style.display === "none" ? "login" : "register");
    });

    document.getElementById("closeAuth").addEventListener("click", function() {
        authSection.style.display = "none";
    });

    loginForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let formData = new URLSearchParams();
        formData.append("email", document.getElementById("loginEmail").value);
        formData.append("password", document.getElementById("loginPassword").value);

        fetch("login", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            if (response.ok) {
                return fetch("session").then(function(r) {
                    let role = r.headers.get("X-User-Role");
                    return r.text().then(function(name) {
                        setLoggedIn(name, role);
                        loginForm.reset();
                        authSection.style.display = "none";
                    });
                });
            }
            return response.text().then(function(text) {
                showAlert(authMessage, text || "Login failed.", false);
            });
        })
        .catch(function() {
            showAlert(authMessage, "Could not reach the server.", false);
        });
    });

    registerForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let formData = new URLSearchParams();
        formData.append("name", document.getElementById("registerName").value);
        formData.append("email", document.getElementById("registerEmail").value);
        formData.append("password", document.getElementById("registerPassword").value);

        fetch("register", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            if (response.ok) {
                registerForm.reset();
                openAuth("login");
                showAlert(authMessage, "Account created! Please log in.", true);
            } else {
                return response.text().then(function(text) {
                    showAlert(authMessage, text || "Registration failed.", false);
                });
            }
        })
        .catch(function() {
            showAlert(authMessage, "Could not reach the server.", false);
        });
    });

    // ---------- My Bookings ----------
    function loadMyBookings() {
        return fetch("my-bookings")
            .then(function(response) {
                if (response.status === 401) {
                    setLoggedOut();
                    openAuth("login", "Please log in to see your bookings.");
                    return null;
                }
                return response.json();
            })
            .then(function(bookings) {
                if (bookings === null) return;

                myBookingsBody.innerHTML = "";

                bookings.forEach(function(b) {
                    let row = document.createElement("tr");

                    [b.bookingId, b.destination, b.hotels, b.roomType === "AC" ? "AC" : "Non-AC",
                     b.travelDate, b.numberOfPeople, b.bookDate || "-"].forEach(function(value) {
                        let cell = document.createElement("td");
                        cell.textContent = value;
                        row.appendChild(cell);
                    });

                    let actionCell = document.createElement("td");
                    let cancelBtn = document.createElement("button");
                    cancelBtn.className = "btn btn-sm btn-outline-danger";
                    cancelBtn.textContent = "Cancel";
                    cancelBtn.setAttribute("data-id", b.bookingId);
                    actionCell.appendChild(cancelBtn);
                    row.appendChild(actionCell);

                    myBookingsBody.appendChild(row);
                });

                myBookingsEmpty.style.display = bookings.length === 0 ? "block" : "none";
            });
    }

    myBookingsNavBtn.addEventListener("click", function(event) {
        event.preventDefault();

        loadMyBookings()
            .then(function() {
                if (!isLoggedIn) return;

                myBookingsMessage.style.display = "none";
                tourDetails.style.display = "none";
                bookingForm.style.display = "none";
                authSection.style.display = "none";
                myBookingsSection.style.display = "block";
                myBookingsSection.scrollIntoView({ behavior: "smooth" });
            })
            .catch(function(error) {
                console.error("My bookings error:", error);
            });
    });

    // Cancel button inside the My Bookings table
    myBookingsBody.addEventListener("click", function(event) {
        let button = event.target.closest("button");
        if (!button) return;

        let id = button.getAttribute("data-id");
        if (!confirm("Cancel booking #" + id + "?")) return;

        let formData = new URLSearchParams();
        formData.append("action", "cancel");
        formData.append("bookingId", id);

        fetch("my-bookings", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            if (response.status === 401) {
                setLoggedOut();
                openAuth("login", "Your session expired. Please log in again.");
                return;
            }
            return response.text().then(function(text) {
                showAlert(myBookingsMessage, text, response.ok);
                if (response.ok) {
                    loadMyBookings();
                    if (selectedTourId) loadTourHotels(selectedTourId);
                }
            });
        })
        .catch(function() {
            showAlert(myBookingsMessage, "Could not reach the server.", false);
        });
    });

    document.getElementById("closeMyBookings").addEventListener("click", function() {
        myBookingsSection.style.display = "none";
    });

    // ---------- Logout ----------
    logoutNavBtn.addEventListener("click", function(event) {
        event.preventDefault();
        fetch("session", { method: "POST" }).then(function() {
            setLoggedOut();
            bookingForm.style.display = "none";
        });
    });

    // ---------- Start ----------
    loadTours();

});
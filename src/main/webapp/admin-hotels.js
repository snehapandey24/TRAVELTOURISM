document.addEventListener("DOMContentLoaded", function() {

    let denied = document.getElementById("denied");
    let adminPanel = document.getElementById("adminPanel");
    let adminMessage = document.getElementById("adminMessage");
    let hotelForm = document.getElementById("hotelForm");
    let formTitle = document.getElementById("formTitle");
    let cancelEditBtn = document.getElementById("cancelEdit");
    let hotelTableBody = document.getElementById("hotelTableBody");
    let noHotels = document.getElementById("noHotels");

    let hotelIdField = document.getElementById("hotelId");
    let hotelNameField = document.getElementById("hotelName");
    let locationField = document.getElementById("location");
    let priceField = document.getElementById("pricePerNight");
    let descriptionField = document.getElementById("description");

    let allHotels = [];

    function showMessage(text, success) {
        adminMessage.textContent = text;
        adminMessage.className = "alert " + (success ? "alert-success" : "alert-danger");
        adminMessage.style.display = "block";
    }

    function formatPrice(price) {
        return Number(price) > 0 ? "Rs " + Number(price).toLocaleString("en-IN") : "-";
    }

    function resetForm() {
        hotelForm.reset();
        hotelIdField.value = "";
        formTitle.textContent = "Add Hotel";
        cancelEditBtn.style.display = "none";
    }

    // ---------- Only admins may use this page ----------
    fetch("session")
        .then(function(response) {
            if (!response.ok) {
                denied.textContent = "Please log in as an admin on the main site first.";
                denied.style.display = "block";
                return;
            }
            if (response.headers.get("X-User-Role") !== "ADMIN") {
                denied.textContent = "Access denied. This page is for admins only.";
                denied.style.display = "block";
                return;
            }
            adminPanel.style.display = "block";
            loadHotels();
        })
        .catch(function() {
            denied.textContent = "Could not reach the server. Is Tomcat running?";
            denied.style.display = "block";
        });

    // ---------- Load and show hotels ----------
    function loadHotels() {
        fetch("admin/hotels")
            .then(function(response) { return response.json(); })
            .then(function(hotels) {
                allHotels = hotels;
                hotelTableBody.innerHTML = "";
                noHotels.style.display = hotels.length === 0 ? "block" : "none";

                hotels.forEach(function(hotel) {
                    let row = document.createElement("tr");

                    [hotel.hotelId, hotel.hotelName, hotel.location, formatPrice(hotel.pricePerNight)].forEach(function(value) {
                        let cell = document.createElement("td");
                        cell.textContent = value;
                        row.appendChild(cell);
                    });

                    let actions = document.createElement("td");

                    let editBtn = document.createElement("button");
                    editBtn.className = "btn btn-sm btn-outline-primary me-2";
                    editBtn.textContent = "Edit";
                    editBtn.setAttribute("data-action", "edit");
                    editBtn.setAttribute("data-id", hotel.hotelId);

                    let deleteBtn = document.createElement("button");
                    deleteBtn.className = "btn btn-sm btn-outline-danger";
                    deleteBtn.textContent = "Delete";
                    deleteBtn.setAttribute("data-action", "delete");
                    deleteBtn.setAttribute("data-id", hotel.hotelId);

                    actions.append(editBtn, deleteBtn);
                    row.appendChild(actions);
                    hotelTableBody.appendChild(row);
                });
            })
            .catch(function() {
                showMessage("Could not load hotels.", false);
            });
    }

    // ---------- Send an add / update / delete request ----------
    function sendAction(formData, resetAfter) {
        fetch("admin/hotels", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            return response.text().then(function(text) {
                showMessage(text, response.ok);
                if (response.ok) {
                    if (resetAfter) resetForm();
                    loadHotels();
                }
            });
        })
        .catch(function() {
            showMessage("Could not reach the server.", false);
        });
    }

    // ---------- Save (add or update) ----------
    hotelForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let id = hotelIdField.value;
        let formData = new URLSearchParams();
        formData.append("action", id ? "update" : "add");
        if (id) formData.append("hotelId", id);
        formData.append("hotelName", hotelNameField.value.trim());
        formData.append("location", locationField.value.trim());
        formData.append("pricePerNight", priceField.value);
        formData.append("description", descriptionField.value.trim());

        sendAction(formData, true);
    });

    cancelEditBtn.addEventListener("click", resetForm);

    // ---------- Edit / Delete buttons in the table ----------
    hotelTableBody.addEventListener("click", function(event) {
        let button = event.target.closest("button");
        if (!button) return;

        let id = button.getAttribute("data-id");
        let action = button.getAttribute("data-action");

        if (action === "edit") {
            let hotel = allHotels.find(function(h) { return String(h.hotelId) === id; });
            if (!hotel) return;

            hotelIdField.value = hotel.hotelId;
            hotelNameField.value = hotel.hotelName;
            locationField.value = hotel.location || "";
            priceField.value = Number(hotel.pricePerNight) > 0 ? hotel.pricePerNight : "";
            descriptionField.value = hotel.description || "";

            formTitle.textContent = "Update Hotel #" + hotel.hotelId;
            cancelEditBtn.style.display = "inline-block";
            hotelForm.scrollIntoView({ behavior: "smooth" });

        } else if (action === "delete") {
            if (!confirm("Delete this hotel?")) return;

            let formData = new URLSearchParams();
            formData.append("action", "delete");
            formData.append("hotelId", id);
            sendAction(formData, false);
        }
    });

});
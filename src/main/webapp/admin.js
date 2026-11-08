document.addEventListener("DOMContentLoaded", function() {

    let denied = document.getElementById("denied");
    let adminPanel = document.getElementById("adminPanel");
    let adminMessage = document.getElementById("adminMessage");
    let tourForm = document.getElementById("tourForm");
    let formTitle = document.getElementById("formTitle");
    let cancelEditBtn = document.getElementById("cancelEdit");
    let tourTableBody = document.getElementById("tourTableBody");

    let tourIdField = document.getElementById("tourId");
    let destinationField = document.getElementById("destination");
    let durationField = document.getElementById("duration");
    let priceField = document.getElementById("price");
    let imageUrlField = document.getElementById("imageUrl");
    let descriptionField = document.getElementById("description");

    let allTours = [];

    function showMessage(text, success) {
        adminMessage.textContent = text;
        adminMessage.className = "alert " + (success ? "alert-success" : "alert-danger");
        adminMessage.style.display = "block";
    }

    function formatPrice(price) {
        return "Rs " + Number(price).toLocaleString("en-IN");
    }

    function resetForm() {
        tourForm.reset();
        tourIdField.value = "";
        formTitle.textContent = "Add Tour";
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
            loadTours();
        })
        .catch(function() {
            denied.textContent = "Could not reach the server. Is Tomcat running?";
            denied.style.display = "block";
        });

    // ---------- Load and show tours ----------
    function loadTours() {
        fetch("tours")
            .then(function(response) { return response.json(); })
            .then(function(tours) {
                allTours = tours;
                tourTableBody.innerHTML = "";

                tours.forEach(function(tour) {
                    let row = document.createElement("tr");

                    [tour.tourId, tour.destination, tour.duration, formatPrice(tour.price)].forEach(function(value) {
                        let cell = document.createElement("td");
                        cell.textContent = value;
                        row.appendChild(cell);
                    });

                    let actions = document.createElement("td");

                    let editBtn = document.createElement("button");
                    editBtn.className = "btn btn-sm btn-outline-primary me-2";
                    editBtn.textContent = "Edit";
                    editBtn.setAttribute("data-action", "edit");
                    editBtn.setAttribute("data-id", tour.tourId);

                    let deleteBtn = document.createElement("button");
                    deleteBtn.className = "btn btn-sm btn-outline-danger";
                    deleteBtn.textContent = "Delete";
                    deleteBtn.setAttribute("data-action", "delete");
                    deleteBtn.setAttribute("data-id", tour.tourId);

                    actions.append(editBtn, deleteBtn);
                    row.appendChild(actions);
                    tourTableBody.appendChild(row);
                });
            })
            .catch(function() {
                showMessage("Could not load tours.", false);
            });
    }

    // ---------- Send an add / update / delete request ----------
    function sendAction(formData, resetAfter) {
        fetch("admin/tours", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            return response.text().then(function(text) {
                showMessage(text, response.ok);
                if (response.ok) {
                    if (resetAfter) resetForm();
                    loadTours();
                }
            });
        })
        .catch(function() {
            showMessage("Could not reach the server.", false);
        });
    }

    // ---------- Save (add or update) ----------
    tourForm.addEventListener("submit", function(event) {
        event.preventDefault();

        let id = tourIdField.value;
        let formData = new URLSearchParams();
        formData.append("action", id ? "update" : "add");
        if (id) formData.append("tourId", id);
        formData.append("destination", destinationField.value.trim());
        formData.append("duration", durationField.value.trim());
        formData.append("price", priceField.value);
        formData.append("imageUrl", imageUrlField.value.trim());
        formData.append("description", descriptionField.value.trim());

        sendAction(formData, true);
    });

    cancelEditBtn.addEventListener("click", resetForm);

    // ---------- Edit / Delete buttons in the table ----------
    tourTableBody.addEventListener("click", function(event) {
        let button = event.target.closest("button");
        if (!button) return;

        let id = button.getAttribute("data-id");
        let action = button.getAttribute("data-action");

        if (action === "edit") {
            let tour = allTours.find(function(t) { return String(t.tourId) === id; });
            if (!tour) return;

            tourIdField.value = tour.tourId;
            destinationField.value = tour.destination;
            durationField.value = tour.duration;
            priceField.value = tour.price;
            imageUrlField.value = tour.imageUrl || "";
            descriptionField.value = tour.description || "";

            formTitle.textContent = "Update Tour #" + tour.tourId;
            cancelEditBtn.style.display = "inline-block";
            tourForm.scrollIntoView({ behavior: "smooth" });

        } else if (action === "delete") {
            if (!confirm("Delete this tour?")) return;

            let formData = new URLSearchParams();
            formData.append("action", "delete");
            formData.append("tourId", id);
            sendAction(formData, false);
        }
    });

});
 
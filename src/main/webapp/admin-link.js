document.addEventListener("DOMContentLoaded", function() {

    let denied = document.getElementById("denied");
    let adminPanel = document.getElementById("adminPanel");
    let adminMessage = document.getElementById("adminMessage");
    let linkForm = document.getElementById("linkForm");
    let tourSelect = document.getElementById("tourSelect");
    let hotelSelect = document.getElementById("hotelSelect");
    let allotmentField = document.getElementById("allotment");
    let linkTableBody = document.getElementById("linkTableBody");
    let noLinks = document.getElementById("noLinks");

    function showMessage(text, success) {
        adminMessage.textContent = text;
        adminMessage.className = "alert " + (success ? "alert-success" : "alert-danger");
        adminMessage.style.display = "block";
    }

    function fillSelect(select, items, valueKey, labelKey, emptyText) {
        select.innerHTML = "";
        if (items.length === 0) {
            let option = document.createElement("option");
            option.value = "";
            option.textContent = emptyText;
            select.appendChild(option);
            return;
        }
        items.forEach(function(item) {
            let option = document.createElement("option");
            option.value = item[valueKey];
            option.textContent = item[labelKey] + " (#" + item[valueKey] + ")";
            select.appendChild(option);
        });
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
            loadOptions();
            loadLinks();
        })
        .catch(function() {
            denied.textContent = "Could not reach the server. Is Tomcat running?";
            denied.style.display = "block";
        });

    // ---------- Fill the Tour and Hotel dropdowns ----------
    function loadOptions() {
        fetch("tours")
            .then(function(response) { return response.json(); })
            .then(function(tours) {
                fillSelect(tourSelect, tours, "tourId", "destination", "No tours yet");
            });

        fetch("admin/hotels")
            .then(function(response) { return response.json(); })
            .then(function(hotels) {
                fillSelect(hotelSelect, hotels, "hotelId", "hotelName", "No hotels yet - add one first");
            });
    }

    // ---------- Show existing links ----------
    function loadLinks() {
        fetch("admin/links")
            .then(function(response) { return response.json(); })
            .then(function(links) {
                linkTableBody.innerHTML = "";
                noLinks.style.display = links.length === 0 ? "block" : "none";

                links.forEach(function(link) {
                    let row = document.createElement("tr");

                    [link.destination, link.hotelName, link.allotment].forEach(function(value) {
                        let cell = document.createElement("td");
                        cell.textContent = value;
                        row.appendChild(cell);
                    });

                    let actions = document.createElement("td");

                    let editBtn = document.createElement("button");
                    editBtn.className = "btn btn-sm btn-outline-primary me-2";
                    editBtn.textContent = "Edit";
                    editBtn.setAttribute("data-action", "edit");
                    editBtn.setAttribute("data-tour", link.tourId);
                    editBtn.setAttribute("data-hotel", link.hotelId);
                    editBtn.setAttribute("data-allotment", link.allotment);

                    let unlinkBtn = document.createElement("button");
                    unlinkBtn.className = "btn btn-sm btn-outline-danger";
                    unlinkBtn.textContent = "Unlink";
                    unlinkBtn.setAttribute("data-action", "unlink");
                    unlinkBtn.setAttribute("data-tour", link.tourId);
                    unlinkBtn.setAttribute("data-hotel", link.hotelId);

                    actions.append(editBtn, unlinkBtn);
                    row.appendChild(actions);
                    linkTableBody.appendChild(row);
                });
            })
            .catch(function() {
                showMessage("Could not load links.", false);
            });
    }

    function sendAction(formData) {
        fetch("admin/links", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        })
        .then(function(response) {
            return response.text().then(function(text) {
                showMessage(text, response.ok);
                if (response.ok) loadLinks();
            });
        })
        .catch(function() {
            showMessage("Could not reach the server.", false);
        });
    }

    // ---------- Allot ----------
    linkForm.addEventListener("submit", function(event) {
        event.preventDefault();

        if (!tourSelect.value || !hotelSelect.value) {
            showMessage("Please choose a tour and a hotel.", false);
            return;
        }

        let formData = new URLSearchParams();
        formData.append("action", "link");
        formData.append("tourId", tourSelect.value);
        formData.append("hotelId", hotelSelect.value);
        formData.append("allotment", allotmentField.value);
        sendAction(formData);
    });

    // ---------- Edit / Unlink buttons in the table ----------
    linkTableBody.addEventListener("click", function(event) {
        let button = event.target.closest("button");
        if (!button) return;

        let action = button.getAttribute("data-action");

        if (action === "edit") {
            tourSelect.value = button.getAttribute("data-tour");
            hotelSelect.value = button.getAttribute("data-hotel");
            allotmentField.value = button.getAttribute("data-allotment");
            linkForm.scrollIntoView({ behavior: "smooth" });

        } else if (action === "unlink") {
            if (!confirm("Remove this link?")) return;

            let formData = new URLSearchParams();
            formData.append("action", "unlink");
            formData.append("tourId", button.getAttribute("data-tour"));
            formData.append("hotelId", button.getAttribute("data-hotel"));
            sendAction(formData);
        }
    });

});

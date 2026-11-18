document.addEventListener("DOMContentLoaded", function() {

    let notice = document.getElementById("notice");
    let searchPanel = document.getElementById("searchPanel");
    let searchForm = document.getElementById("searchForm");
    let bookIdField = document.getElementById("bookId");
    let resultCard = document.getElementById("resultCard");
    let resultBody = document.getElementById("resultBody");

    function showNotice(text, success) {
        notice.textContent = text;
        notice.className = "alert " + (success ? "alert-success" : "alert-danger");
        notice.style.display = "block";
    }

   
    fetch("session")
        .then(function(response) {
            if (response.ok) {
                searchPanel.style.display = "block";
            } else {
                showNotice("Please log in on the main site first.", false);
            }
        })
        .catch(function() {
            showNotice("Could not reach the server. Is Tomcat running?", false);
        });

    searchForm.addEventListener("submit", function(event) {
        event.preventDefault();

        notice.style.display = "none";
        resultCard.style.display = "none";

        fetch("booking-details?bookId=" + encodeURIComponent(bookIdField.value))
            .then(function(response) {
                if (response.status === 401) {
                    searchPanel.style.display = "none";
                    showNotice("Your session expired. Please log in again.", false);
                    return null;
                }
                if (response.status === 404) {
                    showNotice("No booking found with that ID.", false);
                    return null;
                }
                if (!response.ok) {
                    showNotice("Could not search for that booking.", false);
                    return null;
                }
                return response.json();
            })
            .then(function(b) {
                if (!b) return;

                resultBody.innerHTML = "";
                let row = document.createElement("tr");
                [b.bookingId, b.destination, b.hotels, b.roomType === "AC" ? "AC" : "Non-AC",
                 b.travelDate, b.numberOfPeople, b.bookDate || "-"].forEach(function(value) {
                    let cell = document.createElement("td");
                    cell.textContent = value;
                    row.appendChild(cell);
                });
                resultBody.appendChild(row);
                resultCard.style.display = "block";
            })
            .catch(function() {
                showNotice("Could not reach the server.", false);
            });
    });

});
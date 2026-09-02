
const token= localStorage.getItem("token");

if (!token) {
    window.location.href= "login.html";
} else {
    loadUrls()
}

document.getElementById("logout-button").addEventListener("click", function () {
    localStorage.removeItem("token");
    window.location.href= "login.html";
})

document.getElementById("short-url-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const originalUrl= document.getElementById("original-url").value;
    const token= localStorage.getItem("token");

    fetch("http://localhost:8080/api/urls", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer "+ token
        },
        body: JSON.stringify({originalUrl: originalUrl})
    })
        .then(function (response) {
            if (response.ok) {
                response.json().then(function (data) {
                    loadUrls();
                });
            }
            else
                alert("Failed to shorten URL");
        });
});

function loadUrls() {
    const token= localStorage.getItem("token");

    fetch("http://localhost:8080/api/urls", {
        method: "GET",
        headers: {
            "Authorization": "Bearer "+ token
        }
    })
        .then(function (response) {
            response.json().then( function (data) {
                const urlList= document.getElementById("url-list");
                urlList.innerHTML= "";
                data.forEach(function (url) {
                    let linkHtml;
                    if (url.active) {
                        linkHtml = "<a href='" + url.shortUrl + "'>" + url.shortUrl + "</a>";
                    } else {
                        linkHtml = url.shortUrl;
                    }
                    urlList.innerHTML += "<p>" + linkHtml + "<span class='button-group'> <button onclick='deactivateUrl(" + url.id + ")'>Deactivate</button> <button onclick='viewAnalytics(\"" + url.shortCode + "\")'>Analytics</button> <button class='delete-button' onclick='deleteUrl(" + url.id + ")'>Delete</button></span></p>";
                });
            });
        });
}

function deleteUrl(id) {
    const token= localStorage.getItem("token");

    fetch("http://localhost:8080/api/urls/"+ id, {
        method: "DELETE",
        headers: {
            "Authorization": "Bearer "+ token
        }
    })
        .then(function (response) {
            if (response.ok)
                loadUrls()
            else
                alert("Failed to delete URL");
        })
}

function deactivateUrl(id) {
    const token= localStorage.getItem("token");

    fetch("http://localhost:8080/api/urls/" + id + "/deactivate", {
        method: "PATCH",
        headers: {
            "Authorization": "Bearer "+ token
        }
    })
        .then(function (response) {
            if (response.ok)
                loadUrls()
            else
                alert("Failed to deactivate URL");
        })
}

function viewAnalytics(shortCode) {
    const token = localStorage.getItem("token");
    const analyticsSummary = document.getElementById("analytics-summary");
    const analyticsHistory = document.getElementById("analytics-history");
    analyticsSummary.innerHTML = "";
    analyticsHistory.innerHTML = "";

    fetch("http://localhost:8080/api/analytics/" + shortCode + "/total", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    })
        .then(function (response) {
            if (response.ok) {
                response.json().then(function (total) {
                    analyticsSummary.innerHTML += "<p>Total clicks: " + total + "</p>";
                });
            } else {
                alert("Failed to load total clicks");
            }
        });

    fetch("http://localhost:8080/api/analytics/" + shortCode + "/by-country", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    })
        .then(function (response) {
            if (response.ok) {
                response.json().then(function (data) {
                    data.forEach(function (entry) {
                        analyticsSummary.innerHTML += "<p>" + entry.country + ": " + entry.total + "</p>";
                    });
                });
            } else {
                alert("Failed to load country breakdown");
            }
        });

    fetch("http://localhost:8080/api/analytics/" + shortCode + "/by-device", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    })
        .then(function (response) {
            if (response.ok) {
                response.json().then(function (data) {
                    data.forEach(function (entry) {
                        analyticsSummary.innerHTML += "<p>" + entry.deviceType + " (" + entry.browser + "): " + entry.total + "</p>";
                    });
                });
            } else {
                alert("Failed to load device breakdown");
            }
        });

    fetch("http://localhost:8080/api/analytics/" + shortCode + "/history", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    })
        .then(function (response) {
            if (response.ok) {
                response.json().then(function (data) {
                    data.forEach(function (entry) {
                        analyticsHistory.innerHTML += "<p>" + entry.clickedAt + " - " + entry.country + " - " + entry.deviceType + " (" + entry.browser + ")</p>";
                    });
                });
            } else {
                alert("Failed to load history");
            }
        });
}
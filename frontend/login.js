document.getElementById("register-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const username= document.getElementById("register-username").value;
    const email= document.getElementById("register-email").value;
    const password= document.getElementById("register-password").value;

    fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({username: username, email: email, password: password})
    })
        .then(function (response) {
            if (response.ok)
                alert("Register Successfully");
            else
                alert("Registration failed");
        });
});


document.getElementById("login-form").addEventListener("submit", function (event) {
    event.preventDefault();

    const email= document.getElementById("login-email").value;
    const password= document.getElementById("login-password").value;

    fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({email: email, password: password})
    })
        .then(function (response) {
            if (response.ok){
                response.json().then(function (data) {
                    localStorage.setItem("token", data.token);
                    window.location.href = "dashboard.html";
                });
            }
            else
                alert("Login failed");
        })
});
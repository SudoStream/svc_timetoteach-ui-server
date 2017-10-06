function onSignIn(googleUser) {
    // Useful data for your client-side scripts:
    var profile = googleUser.getBasicProfile();
    console.log("ID: " + profile.getId()); // Don't send this directly to your server!
    console.log('Full Name: ' + profile.getName());
    console.log('Given Name: ' + profile.getGivenName());
    console.log('Family Name: ' + profile.getFamilyName());
    console.log("Image URL: " + profile.getImageUrl());
    console.log("Email: " + profile.getEmail());

    // The ID token you need to pass to your backend:
    var id_token = googleUser.getAuthResponse().id_token;
    console.log("ID Token: " + id_token);

    var xhr = new XMLHttpRequest();

    xhr.open('POST', '/tokensignin');
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.setRequestHeader('X-Requested-With', 'Accept');
    xhr.onload = function () {
        console.log('Signed in as: ' + xhr.responseText);
    };
    xhr.onloadend = function () {
        if (xhr.status === 404) {
            if (confirm("\nIt looks like you have not yet signed up to Time To Teach.\n\n" +
                    "Would you like to sign up now?\n\n")) {
                window.location.href = "/signup";
            }
        } else {
            console.log("reply status :- " + xhr.status);
            window.setTimeout(goToApp, 500);
        }

    };
    xhr.send('idtoken=' + id_token);

}


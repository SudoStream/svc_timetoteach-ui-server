(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) return;
    js = d.createElement(s);
    js.id = id;
    js.src = 'https://connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.11&appId=377986715954577';
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

// This is called with the results from from FB.getLoginStatus().
function statusChangeCallback(response) {
    console.log('Facebook statusChangeCallback');
    console.log(response);
    // The response object is returned with a status field that lets the
    // app know the current login status of the person.
    // Full docs on the response object can be found in the documentation
    // for FB.getLoginStatus().
    if (response.status === 'connected') {
        console.log("FB Token = " + response.authResponse.accessToken);
        // Logged into your app and Facebook.

        FB.api('/me?fields=id,name,picture,first_name,last_name,age_range,verified,email', function (innerResponse) {
            console.log('FB ID:: ' + innerResponse.id);
            console.log('FB Full Name: ' + innerResponse.first_name + " " + innerResponse.last_name);
            console.log('FB Given Name: ' + innerResponse.first_name);
            console.log('FB Family Name: ' + innerResponse.last_name);
            console.log('FB Image URL: ' + innerResponse.picture.data.url);
            console.log('FB Email: ' + innerResponse.email);

            var xhr = new XMLHttpRequest();

            xhr.open('POST', '/facebooktokensignin');
            xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            xhr.setRequestHeader('X-Requested-With', 'Accept');
            xhr.onload = function () {
                console.log('Signed in as: ' + xhr.responseText);
            };
            xhr.onloadend = function () {
                if (xhr.status === 404) {
                    console.log("reply status :- " + xhr.status);
                    window.location.href = "/signupsteptwo";
                } else {
                    console.log("reply status :- " + xhr.status);
                    window.setTimeout(goToApp, 500);
                }

            };
            xhr.send(
                'idtoken=' + response.authResponse.accessToken + '&' +
                'userId=' + innerResponse.id + '&' +
                'userEmail=' + innerResponse.email + '&' +
                'userUri=' + encodeURIComponent(innerResponse.picture.data.url) + '&' +
                'userGivenName=' + innerResponse.first_name + '&' +
                'userFamilyName=' + innerResponse.last_name
            );
        });

    }
    else {
        console.log("The person is not logged into your app or we are unable to tell.");
    }
}

// This function is called when someone finishes with the Login
// Button.  See the onlogin handler attached to it in the sample
// code below.
function checkLoginState() {
    FB.getLoginStatus(function (response) {
        statusChangeCallback(response);
    });
}

window.fbAsyncInit = function () {
    FB.init({
        appId: '377986715954577',
        cookie: true,  // enable cookies to allow the server to access
                       // the session
        xfbml: true,  // parse social plugins on this page
        version: 'v2.8' // use graph api version 2.8
    });

    // Now that we've initialized the JavaScript SDK, we call
    // FB.getLoginStatus().  This function gets the state of the
    // person visiting this page and can return one of three states to
    // the callback you provide.  They can be:
    //
    // 1. Logged into your app ('connected')
    // 2. Logged into Facebook, but not your app ('not_authorized')
    // 3. Not logged into Facebook and can't tell if they are logged into
    //    your app or not.
    //
    // These three cases are handled in the callback function.

    // FB.getLoginStatus(function (response) {
    //     statusChangeCallback(response);
    // });

};


// Here we run a very simple test of the Graph API after login is
// successful.  See statusChangeCallback() for when this call is made.
function testAPI() {
    console.log('Welcome!  Fetching your information.... ');
    FB.api('/me?fields=id,name,picture,first_name,last_name,age_range,verified,email', function (response) {
        console.log('FB ID:: ' + response.id);
        console.log('FB Full Name: ' + response.name);
        console.log('FB Given Name: ' + response.first_name);
        console.log('FB Family Name: ' + response.last_name);
        console.log('FB Image URL: ' + response.picture.data.url);
        console.log('FB Email: ' + response.email);


    });
}

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

    FB.getLoginStatus(function (response) {
        statusChangeCallback(response);
    });

};

(function(d, s, id){
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) {return;}
    js = d.createElement(s); js.id = id;
    js.src = "https://connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

// This is called with the results from from FB.getLoginStatus().
function statusChangeCallback(response) {
    console.log('statusChangeCallback');
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
            console.log('FB Full Name: ' + innerResponse.name);
            console.log('FB Given Name: ' + innerResponse.first_name);
            console.log('FB Family Name: ' + innerResponse.last_name);
            console.log('FB Image URL: ' + innerResponse.picture.data.url);
            console.log('FB Email: ' + innerResponse.email);
        });

    }
    else {
        console.log("The person is not logged into your app or we are unable to tell.");
    }
}

function logoutFacebook() {
    console.log('FACEBOOK: About to try and log out! SOO CLOSE NOW! ONE');
    FB.logout(function() {
        FB.Auth.setAuthResponse(null, 'unknown');
        // Person is now logged out
        console.log('FACEBOOK: OOOOOOOOBHHHHHHHHHH  YYYEAAHH!!!');
    });
    console.log('FACEBOOK: Did you see logged out message?')
}

function onLoadForSignout() {
    console.log('On Load for Signed out called.');
    gapi.load('auth2', function() {
        gapi.auth2.init();
    });
    console.log('GAPI should be loaded');
}

function signOut() {
    console.log('Signed out called.');
    onLoadForSignout();
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut();

    logoutFacebook();

    andThenGotoSignout();
}

function andThenGotoSignout() {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/signout');
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.setRequestHeader('X-Requested-With', 'Accept');
    xhr.send();

    console.log('User signed out.');
    goToLoggedOut();
}




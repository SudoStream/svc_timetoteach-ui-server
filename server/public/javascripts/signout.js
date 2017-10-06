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
    auth2.signOut().then(function () {

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/signout');
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.setRequestHeader('X-Requested-With', 'Accept');
        xhr.send();

        console.log('User signed out.');
        goToLoggedOut()
    });
}


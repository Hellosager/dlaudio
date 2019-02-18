window.addEventListener("beforeunload", function (e) {

	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", "/leave");
	xhttp.send();
});

var progressNumber = 0;
updateProgress();

function updateProgress() {
	if(progressNumber < [[${maxProgress}]]){
		var xhttp = new XMLHttpRequest(); // create xhttpRequest
		xhttp.open("GET", "/retrieveProgress/" + [[${pid}]], false); // specify GET-Method, path and set it to synchron Request
		xhttp.send(); // send request

		var progressResponse = JSON.parse(xhttp.responseText); // parse JSON response into JS object
		progressNumber = progressResponse.progress;
		document.getElementById("progress-bar").setAttribute("value",
			progressNumber); // use the parsed object to get the progress, the JSON delivered
		if([[${type}]] === 'single'){
			document.getElementById("showProgress").innerHTML = progressNumber + '%'; 
		}else if([[${type}]] === 'multi' || [[${type}]] === 'singleVideo'){
			document.getElementById("showProgress").innerHTML = 'Processed ' + progressNumber + " from " + [[${maxProgress}]] + ' links';
		}
		setTimeout("updateProgress()", 350); // one request per second
	}else{
		window.location = "/download/[[${pid}]]";
	}
}
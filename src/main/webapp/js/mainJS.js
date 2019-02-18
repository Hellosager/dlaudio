var fileSelect = document.getElementById('textFile');
var label = document.getElementById('fileInputLabel');
var labelVal = label.innerHTML;

fileSelect.addEventListener('change', function(e) {
	var fileName = e.target.value.split('\\').pop();
	if(fileName)
		label.querySelector('span').innerHTML = fileName;
	else
		label.innerHTML = labelVal; 
	
});
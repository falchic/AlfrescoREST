jQuery(document).ready(function($) {
	$("#doc-form").submit(function(event) {
		event.preventDefault();
		postCreateDoc();
	});
	$("#folder-form").submit(function(event) {
		event.preventDefault();
		postCreateFolder();
	});
});

function postCreateDoc() {
	var ciaone;
	var message = {}
	message["user"] = "admin";
	message["password"] = "alfresco";
	var request = {}
	request["destination"] = $("#destination").val();
	request["title"] = $("#title").val();
	request["description"] = $("#description").val();
	request["title"] = $("#title").val();
	message["request"] = request;
	var formData = new FormData();
    formData.append("message", new Blob([JSON.stringify(message)], {
           type : "application/json"
    }));
    formData.append("file", $("#inputFile")[0].files[0]);
	
	$.ajax({
		type : "POST",
		url : "request/createDoc",
		contentType: false,
		processData: false,
		data : formData,
		success : function(data) {
			console.log("SUCCESS: ", data);
			display(data);
		},
		error : function(e) {
			console.log("ERROR: ", e);
			display(e);
		},
		done : function(e) {
			console.log("DONE");
		}
	});
}

function postCreateFolder() {
	var message = {}
	message["user"] = "admin";
	message["password"] = "alfresco";
	var request = {}
	request["destination"] = $("#destination").val();
	request["name"] = $("#name").val();
	message["request"] = request;
	
	$.ajax({
		type : "POST",
		url : "request/createFolder",
		contentType : "application/json",
		data : JSON.stringify(message),
		success : function(data) {
			console.log("SUCCESS: ", data);
			display(data);
		},
		error : function(e) {
			console.log("ERROR: ", e);
			display(e);
		},
		done : function(e) {
			console.log("DONE");
		}
	});
}

function display(data) {
	var json = "<h4>Response</h4><pre>"
			+ JSON.stringify(data, null, 4) + "</pre>";
	$('#feedback').html(json);
}
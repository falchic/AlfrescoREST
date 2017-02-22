<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>AlfrescoREST</title>
        <link type="text/css" href="<c:url value='/resources/css/bootstrap.min.css' />" rel="stylesheet" />
        <link type="text/css" href="<c:url value='/resources/css/custom.css' />" rel="stylesheet" />
    </head>
    <body>
		<div class="jumbotron text-center">
			<h1>AlfrescoREST</h1>
		  	<p>Select a service:</p> 
		</div>
		<div class="container">
			<div class="col-md-6 text-right">
				<div class="row form-group extra-padding">
					<button type="button" class="btn btn-primary btn-lg">Create document</button>
				</div>
				<div class="row form-group extra-padding">
					<button type="button" class="btn btn-primary btn-lg">Get document by ID or path</button>
				</div>
			</div>
			<div class="col-md-6 text-left">
				<div class="row form-group extra-padding">
					<button type="button" class="btn btn-primary btn-lg">Create folder</button>
				</div>
				<div class="row form-group extra-padding">
					<button type="button" class="btn btn-primary btn-lg">Get documents in a folder</button>
				</div>
			</div>
		</div>
    </body>
</html>

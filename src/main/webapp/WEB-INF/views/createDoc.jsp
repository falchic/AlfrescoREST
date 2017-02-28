<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>AlfrescoREST</title>
        <link type="text/css" href="<c:url value='/resources/css/bootstrap.min.css' />" rel="stylesheet" />
        <link type="text/css" href="<c:url value='/resources/css/flat-ui.min.css' />" rel="stylesheet" />
        <link type="text/css" href="<c:url value='/resources/css/custom.css' />" rel="stylesheet" />
        <script src="<c:url value="/resources/js/jquery.min.js" />"></script>
    	<script src="<c:url value="/resources/js/flat-ui.min.js" />"></script>
    </head>
    <body>
    	<!-- Static navbar -->
	    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
	      <div class="container">
	        <div class="navbar-header">
	          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
	            <span class="sr-only">Toggle navigation</span>
	          </button>
	          <c:url value="/" var="homeUrl"/> 
	          <a class="navbar-brand" href="${homeUrl}">AlfrescoREST</a>
	        </div>
	        <div class="navbar-collapse collapse">
	          <ul class="nav navbar-nav">
	            <li class="dropdown">
	              <a href="" class="dropdown-toggle" data-toggle="dropdown">Services<b class="caret"></b></a>
	              <ul class="dropdown-menu">
	              	<c:url value="/createDoc" var="createDocUrl"/>
	                <li><a href="${createDocUrl}">Create document</a></li>
	                <li><a href="#">Create folder</a></li>
	                <li><a href="#">Get document by ID or path</a></li>
	                <li><a href="#">Get documents in a folder</a></li>
	                <li><a href="#">Remove documents in a folder</a></li>
	                <li><a href="#">Remove folder</a></li>
	              </ul>
	            </li>
	          </ul>
	          <ul class="nav navbar-nav navbar-right">
	            <li><a href="">Login</a></li>
	          </ul>
	        </div><!--/.nav-collapse -->
	      </div>
	    </div>
	
	
	    <div class="container">
	    	<div class="row text-center">
	    		<h4>Create a new document</h4>
	    		<div id="content" class="col-md-8 col-md-offset-2">
	    			<c:url value="/request/createDoc" var="createDocUrl"/>
	    			<form class="form-horizontal" method="POST" action="${createDocUrl}" enctype="multipart/form-data">
	    				<div class="form-group">
			              <label for="inputPath" class="col-lg-2 control-label">Destination</label>
			              <div class="col-lg-8">
			                	<input type="text" name="destination" class="form-control" id="inputPath" placeholder="Enter a path">
			              </div>
			            </div>
			            <div class="form-group">
			              <label for="inputTitle" class="col-lg-2 control-label">Title</label>
			              <div class="col-lg-8">
			                	<input type="text" name= "title" class="form-control" id="inputTitle" placeholder="Enter a title">
			              </div>
			            </div>
			            <div class="form-group">
			              <label for="inputDescription" class="col-lg-2 control-label">Description</label>
			              <div class="col-lg-8">
			                	<input type="text" name="description" class="form-control" id="inputDescription" placeholder="Enter a description">
			              </div>
			            </div>
			            <div class="form-group">
			            	<label for="inputFile" class="col-lg-2 control-label">File</label>
			            	<div class="col-lg-8">
				            	<input type="file" name="file" id="inputFile">
				            </div>
			            </div>
			            <div class="form-group">
			              <div class="col-lg-8 col-md-offset-2">
			                <button type="submit" class="btn btn-primary">Submit</button>
			              </div>
			            </div>
	    			</form>
	    		</div>
	    	</div>
	    </div>

</body>
</html>
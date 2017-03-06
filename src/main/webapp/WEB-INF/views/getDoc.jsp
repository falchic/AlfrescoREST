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
    	<script src="<c:url value="/resources/js/alfresco.js" />"></script>
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
	                <c:url value="/createFolder" var="createFolderUrl"/>
	                <li><a href="${createFolderUrl}">Create folder</a></li>
	                <c:url value="/getDoc" var="getDocUrl"/>
	                <li><a href="${getDocUrl}">Get document by ID or path</a></li>
	                <li><a href="#">Get documents in a folder</a></li>
	                <li><a href="#">Remove documents in a folder</a></li>
	                <c:url value="/removeFolder" var="removeFolderUrl"/>
	                <li><a href="${removeFolderUrl}">Remove folder</a></li>
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
	    		<h4>Get a document</h4>
	    		<div id="content" class="col-md-8 col-md-offset-2">
	    			<form class="form-horizontal" name="form" id="getdoc-form">
	    				<div class="form-group">
			              <label for="destination" class="col-lg-2 control-label">Destination</label>
			              <div class="col-lg-8">
			                	<input type="text" class="form-control" id="destination" placeholder="Enter a path">
			              </div>
			            </div>
			            <div class="form-group">
			              <label for="name" class="col-lg-2 control-label">ID</label>
			              <div class="col-lg-8">
			                	<input type="text" class="form-control" id="uuid" placeholder="Enter ID">
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
	    	<div id="feedback"></div>
	    </div>

</body>
</html>
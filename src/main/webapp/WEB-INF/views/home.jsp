<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>AlfrescoREST</title>
        <link type="text/css" href="<c:url value='/resources/css/bootstrap.min.css' />" rel="stylesheet" />
        <link type="text/css" href="<c:url value='/resources/css/simple-sidebar.css' />" rel="stylesheet" />
        
    </head>
    <body>
		<div id="wrapper">

        <!-- Sidebar -->
        <div id="sidebar-wrapper">
            <ul class="sidebar-nav">
                <li class="sidebar-brand">
                    <a href="#">
                        AlfrescoREST
                    </a>
                </li>
                <li>
                    <a href="#">Create document</a>
                </li>
                <li>
                    <a href="#">Create folder</a>
                </li>
                <li>
                    <a href="#">Get document by ID or path</a>
                </li>
                <li>
                    <a href="#">Get documents in a folder</a>
                </li>
                <li>
                    <a href="#">Remove documents in a folder</a>
                </li>
                <li>
                    <a href="#">Remove folder</a>
                </li>
            </ul>
        </div>
        <!-- /#sidebar-wrapper -->

        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-12">
                        <h1>AlfrescoREST</h1>
                        <p>Select a service here or in the sidebar.</p>
                    </div>
                </div>
                <div class="row text-center">
	                <div class="col-md-2 ">
	                	<button type="button" class="btn btn-success btn-circle btn-xl"><i class="glyphicon glyphicon-file"></i></button>
	                </div>
					 <div class="col-md-2 ">
	                	<button type="button" class="btn btn-success btn-circle btn-xl"><i class="glyphicon glyphicon-folder-open"></i></button>
	                </div>
                	 <div class="col-md-2 ">
	                	<button type="button" class="btn btn-info btn-circle btn-xl"><i class="glyphicon glyphicon-download"></i></button>
	                </div>
	                 <div class="col-md-2 ">
	                	<button type="button" class="btn btn-info btn-circle btn-xl"><i class="glyphicon glyphicon-th-list"></i></button>
	                </div>
	                 <div class="col-md-2 ">
	                	<button type="button" class="btn btn-danger btn-circle btn-xl"><i class="glyphicon glyphicon-trash"></i></button>
	                </div>
	                 <div class="col-md-2 ">
	                	<button type="button" class="btn btn-danger btn-circle btn-xl"><i class="glyphicon glyphicon-remove-circle"></i></button>
	                </div>
                </div>
                <div class="row text-center">
				  <div class="col-md-2 label-service">Create document</div>
				  <div class="col-md-2 label-service">Create folder</div>
				  <div class="col-md-2 label-service">Get document by ID or path</div>
				  <div class="col-md-2 label-service">Get documents in a folder</div>
				  <div class="col-md-2 label-service">Remove documents in a folder</div>
				  <div class="col-md-2 label-service">Remove folder</div>
				</div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->
    </body>
</html>

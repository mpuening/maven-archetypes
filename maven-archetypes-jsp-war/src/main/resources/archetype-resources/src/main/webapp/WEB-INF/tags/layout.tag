<%@tag description="Page Layout" pageEncoding="UTF-8"%>
<%@attribute name="header" fragment="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>App Name</title>
<link rel="stylesheet"
	href="<c:url value="/webjars/bootstrap/4.4.1/css/bootstrap.css"/>" />
<link rel="stylesheet" href="<c:url value="/resources/css/style.css"/>" />

<script src="<c:url value="/webjars/jquery/3.4.1/jquery.min.js"/>"></script>
<script
	src="<c:url value="/webjars/bootstrap/4.4.1/js/bootstrap.min.js"/>"></script>
</head>
<body>
	<div id="header">
		<jsp:invoke fragment="header" />
	</div>

	<div class="container-fluid">
		<div class="row">
			<div class="col-md-12">
				<br />
				<div id="body">
					<jsp:doBody />
				</div>
			</div>
		</div>
	</div>
</body>
</html>
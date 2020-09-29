<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<layout:layout>
	<jsp:attribute name="header">
      <jsp:include page="/WEB-INF/views/layout/header.jsp" /> 
    </jsp:attribute>
	<jsp:body>
		<div class="row">
			<div class="col-sm-6 offset-sm-3 text-center">
				<h1 class="display-4">Page Not Found</h1>
				<div>Uh oh... that page does not exist...</div>
			</div>
		</div>
	</jsp:body>
</layout:layout>
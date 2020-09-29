<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<layout:layout>
	<jsp:attribute name="header">
      <jsp:include page="/WEB-INF/views/layout/header.jsp" /> 
    </jsp:attribute>
	<jsp:body>
		<a href="<c:url value="/hello"/>">Say Hello</a>
	</jsp:body>
</layout:layout>
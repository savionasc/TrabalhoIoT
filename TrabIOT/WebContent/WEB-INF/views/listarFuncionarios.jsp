<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Insert title here</title>
</head>
<body>
	<c:if test="${funcDesocupados != null}">
		<h3>Desocupados</h3>
		<c:forEach var="t" items="${funcDesocupados}">
			<li>Funcionário Desocupado:${t.nome}</li>
			<c:if test="${t.filaQuartos != null}">
				<c:forEach var="x" items="${t.filaQuartos}">
					<li>Chamada em espera: ${x}</li>
				</c:forEach>
			</c:if>
		</c:forEach>
	</c:if>

	<c:if test="${funcOcupados != null}">
		<h3>Ocupados</h3>
		<c:forEach var="t" items="${funcOcupados}">
			<h5>- ${t.nome}</h5>
			<li>Quarto em atendimento: ${t.quarto}</li>
			<c:if test="${t.filaQuartos != null}">
				<c:forEach var="x" items="${t.filaQuartos}">
					<li>Chamada em espera: ${x}</li>
				</c:forEach>
			</c:if>
		</c:forEach>
	</c:if>

	<c:if test="${filaEspera != null}">
		<h3>Fila de espera</h3>
		<c:forEach var="t" items="${filaEspera}">
			<li>Chamada em espera: ${t}</li>
		</c:forEach>
	</c:if>
	
</body>
</html>